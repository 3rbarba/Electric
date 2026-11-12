using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Net;
using System.Text;
using Autodesk.AutoCAD.ApplicationServices;
using Autodesk.AutoCAD.DatabaseServices;
using Autodesk.AutoCAD.EditorInput;
using Autodesk.AutoCAD.Geometry;
using Autodesk.AutoCAD.Runtime;

namespace EletricAutoCAD
{
    public sealed class MedirComodoCommand
    {
        private const string Endpoint = "http://127.0.0.1:47651/api/autocad/measurement";

        [CommandMethod("APPMEDIR")]
        public void MedirComodo()
        {
            Document document = Application.DocumentManager.MdiActiveDocument;
            if (document == null)
            {
                return;
            }

            Editor editor = document.Editor;
            PromptKeywordOptions methodOptions = new PromptKeywordOptions(
                "\nMétodo de medição [Pontos/Polilinha] <Pontos>: ");
            methodOptions.Keywords.Add("Pontos");
            methodOptions.Keywords.Add("Polilinha");
            methodOptions.AllowNone = true;
            PromptResult method = editor.GetKeywords(methodOptions);
            if (method.Status != PromptStatus.OK && method.Status != PromptStatus.None)
            {
                return;
            }

            UnitsValue drawingUnits = document.Database.Insunits;
            double meterScale;
            string unitDescription;
            if (!TryChooseMeterScale(editor, drawingUnits, out meterScale, out unitDescription))
            {
                return;
            }

            double areaSquareMeters;
            double perimeterMeters;
            if (method.Status == PromptStatus.None || method.StringResult == "Pontos")
            {
                if (!TryMeasurePoints(editor, meterScale, out areaSquareMeters, out perimeterMeters))
                {
                    return;
                }
            }
            else
            {
                if (!TryMeasurePolyline(document, editor, meterScale,
                        out areaSquareMeters, out perimeterMeters))
                {
                    return;
                }
            }

            try
            {
                SendMeasurement(areaSquareMeters, perimeterMeters);
                editor.WriteMessage(
                    "\nÁrea: {0:N4} m² | Perímetro: {1:N4} m. "
                    + "Unidade de origem: {2}. Medidas enviadas ao ELETRIC.",
                    areaSquareMeters, perimeterMeters, unitDescription);
            }
            catch (WebException exception)
            {
                string detail = exception.Response == null
                    ? exception.Message
                    : ReadResponse(exception.Response);
                editor.WriteMessage(
                    "\nNão foi possível enviar as medidas ao ELETRIC. "
                    + "Abra o aplicativo e confirme que a API local está ativa. " + detail);
            }
            catch (IOException exception)
            {
                editor.WriteMessage("\nErro ao enviar as medidas ao ELETRIC: " + exception.Message);
            }
        }

        private static bool TryChooseMeterScale(
            Editor editor, UnitsValue drawingUnits,
            out double meterScale, out string unitDescription)
        {
            meterScale = 0;
            unitDescription = string.Empty;
            PromptKeywordOptions options = new PromptKeywordOptions(
                "\nUnidade das coordenadas [Desenho/Metros/Centimetros/Milimetros/Pes/Polegadas] "
                + "<Desenho> (INSUNITS atual: " + drawingUnits + "): ");
            options.Keywords.Add("Desenho");
            options.Keywords.Add("Metros");
            options.Keywords.Add("Centimetros");
            options.Keywords.Add("Milimetros");
            options.Keywords.Add("Pes");
            options.Keywords.Add("Polegadas");
            options.AllowNone = true;
            PromptResult result = editor.GetKeywords(options);
            if (result.Status != PromptStatus.OK && result.Status != PromptStatus.None)
            {
                return false;
            }

            if (result.Status == PromptStatus.None || result.StringResult == "Desenho")
            {
                try
                {
                    meterScale = MeterScale(drawingUnits);
                }
                catch (InvalidOperationException exception)
                {
                    editor.WriteMessage("\n" + exception.Message);
                    return false;
                }
                unitDescription = "INSUNITS " + drawingUnits;
                return true;
            }

            switch (result.StringResult)
            {
                case "Metros": meterScale = 1.0; unitDescription = "metros"; return true;
                case "Centimetros": meterScale = 0.01; unitDescription = "centímetros"; return true;
                case "Milimetros": meterScale = 0.001; unitDescription = "milímetros"; return true;
                case "Pes": meterScale = 0.3048; unitDescription = "pés"; return true;
                case "Polegadas": meterScale = 0.0254; unitDescription = "polegadas"; return true;
                default:
                    editor.WriteMessage("\nUnidade de coordenadas inválida.");
                    return false;
            }
        }

        private static bool TryMeasurePolyline(
            Document document, Editor editor, double meterScale,
            out double areaSquareMeters, out double perimeterMeters)
        {
            areaSquareMeters = 0;
            perimeterMeters = 0;
            PromptEntityOptions options = new PromptEntityOptions(
                "\nSelecione uma polilinha fechada do cômodo: ");
            options.SetRejectMessage("\nO objeto precisa ser uma polilinha.");
            options.AddAllowedClass(typeof(Polyline), exactMatch: false);
            PromptEntityResult selection = editor.GetEntity(options);
            if (selection.Status != PromptStatus.OK)
            {
                return false;
            }

            using (Transaction transaction = document.Database.TransactionManager.StartTransaction())
            {
                Polyline? polyline = transaction.GetObject(
                    selection.ObjectId, OpenMode.ForRead) as Polyline;
                if (polyline == null)
                {
                    editor.WriteMessage("\nO objeto selecionado não é uma polilinha.");
                    return false;
                }
                if (!polyline.Closed)
                {
                    editor.WriteMessage(
                        "\nA polilinha está aberta. Use o método Pontos para contornos não fechados.");
                    return false;
                }

                areaSquareMeters = polyline.Area * meterScale * meterScale;
                perimeterMeters = polyline.Length * meterScale;
                transaction.Commit();
            }
            return true;
        }

        private static bool TryMeasurePoints(
            Editor editor, double meterScale,
            out double areaSquareMeters, out double perimeterMeters)
        {
            areaSquareMeters = 0;
            perimeterMeters = 0;
            List<Point3d> points = new List<Point3d>();
            using (MeasurementPreview preview = new MeasurementPreview(meterScale))
            {
                while (true)
                {
                    string prompt = points.Count == 0
                        ? "\nIndique o primeiro ponto do contorno: "
                        : "\nIndique o próximo ponto (Enter fecha o contorno): ";
                    PromptPointOptions options = new PromptPointOptions(prompt)
                    {
                        AllowNone = points.Count >= 3
                    };
                    PromptPointResult result = editor.GetPoint(options);
                    if (result.Status == PromptStatus.None)
                    {
                        if (points.Count < 3)
                        {
                            editor.WriteMessage(
                                "\nInforme pelo menos três pontos para medir a área do cômodo.");
                            return false;
                        }
                        preview.CloseContour(points[points.Count - 1], points[0]);
                        break;
                    }
                    if (result.Status != PromptStatus.OK)
                    {
                        return false;
                    }
                    preview.AddPoint(result.Value, points.Count == 0
                        ? (Point3d?)null
                        : points[points.Count - 1]);
                    points.Add(result.Value);
                }
            }

            double areaDrawingUnits;
            double perimeterDrawingUnits;
            if (!TryCalculatePolygon(points, out areaDrawingUnits, out perimeterDrawingUnits))
            {
                editor.WriteMessage(
                    "\nOs pontos não formam uma área válida. Indique pelo menos três pontos distintos.");
                return false;
            }

            areaSquareMeters = areaDrawingUnits * meterScale * meterScale;
            perimeterMeters = perimeterDrawingUnits * meterScale;
            return true;
        }

        private sealed class MeasurementPreview : IDisposable
        {
            private readonly Autodesk.AutoCAD.GraphicsInterface.TransientManager manager;
            private readonly IntegerCollection viewports;
            private readonly List<Entity> entities = new List<Entity>();
            private readonly double markerRadius;

            public MeasurementPreview(double meterScale)
            {
                manager = Autodesk.AutoCAD.GraphicsInterface.TransientManager.CurrentTransientManager;
                viewports = new IntegerCollection();
                markerRadius = 0.05 / meterScale;
            }

            public void AddPoint(Point3d point, Point3d? previous)
            {
                Circle marker = new Circle(point, Vector3d.ZAxis, markerRadius)
                {
                    Color = Autodesk.AutoCAD.Colors.Color.FromColorIndex(
                        Autodesk.AutoCAD.Colors.ColorMethod.ByAci, 2)
                };
                Add(marker);

                if (previous.HasValue)
                {
                    Line segment = new Line(previous.Value, point)
                    {
                        Color = Autodesk.AutoCAD.Colors.Color.FromColorIndex(
                            Autodesk.AutoCAD.Colors.ColorMethod.ByAci, 1)
                    };
                    Add(segment);
                }
            }

            public void CloseContour(Point3d last, Point3d first)
            {
                Line closingSegment = new Line(last, first)
                {
                    Color = Autodesk.AutoCAD.Colors.Color.FromColorIndex(
                        Autodesk.AutoCAD.Colors.ColorMethod.ByAci, 3)
                };
                Add(closingSegment);
            }

            private void Add(Entity entity)
            {
                try
                {
                    if (!manager.AddTransient(
                            entity,
                            Autodesk.AutoCAD.GraphicsInterface.TransientDrawingMode.DirectTopmost,
                            128, viewports))
                    {
                        throw new InvalidOperationException(
                            "O AutoCAD não conseguiu desenhar a prévia dos pontos.");
                    }
                    entities.Add(entity);
                }
                catch
                {
                    entity.Dispose();
                    throw;
                }
            }

            public void Dispose()
            {
                for (int i = entities.Count - 1; i >= 0; i--)
                {
                    manager.EraseTransient(entities[i], viewports);
                    entities[i].Dispose();
                }
                entities.Clear();
            }
        }

        private static bool TryCalculatePolygon(
            IList<Point3d> points, out double area, out double perimeter)
        {
            Vector3d areaVector = new Vector3d(0, 0, 0);
            perimeter = 0;
            for (int i = 0; i < points.Count; i++)
            {
                Point3d current = points[i];
                Point3d next = points[(i + 1) % points.Count];
                areaVector += current.GetAsVector().CrossProduct(next.GetAsVector());
                perimeter += current.DistanceTo(next);
            }

            area = areaVector.Length * 0.5;
            return area > 0 && !double.IsInfinity(area) && !double.IsNaN(area)
                && perimeter > 0 && !double.IsInfinity(perimeter) && !double.IsNaN(perimeter);
        }

        private static double MeterScale(UnitsValue units)
        {
            switch (units)
            {
                case UnitsValue.Inches: return 0.0254;
                case UnitsValue.Feet: return 0.3048;
                case UnitsValue.Yards: return 0.9144;
                case UnitsValue.Miles: return 1609.344;
                case UnitsValue.Millimeters: return 0.001;
                case UnitsValue.Centimeters: return 0.01;
                case UnitsValue.Decimeters: return 0.1;
                case UnitsValue.Meters: return 1.0;
                case UnitsValue.Dekameters: return 10.0;
                case UnitsValue.Hectometers: return 100.0;
                case UnitsValue.Kilometers: return 1000.0;
                case UnitsValue.USSurveyFeet: return 1200.0 / 3937.0;
                default:
                    throw new InvalidOperationException(
                        "A unidade do desenho não é reconhecida. Defina INSUNITS como metros, "
                        + "centímetros, milímetros, pés, polegadas ou outra unidade compatível.");
            }
        }

        private static void SendMeasurement(double area, double perimeter)
        {
            string payload = "area=" + Uri.EscapeDataString(
                    area.ToString("R", CultureInfo.InvariantCulture))
                + "&perimetro=" + Uri.EscapeDataString(
                    perimeter.ToString("R", CultureInfo.InvariantCulture));
            byte[] data = Encoding.UTF8.GetBytes(payload);
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(Endpoint);
            request.Method = "POST";
            request.ContentType = "application/x-www-form-urlencoded; charset=UTF-8";
            request.ContentLength = data.Length;
            request.Timeout = 3000;
            request.ReadWriteTimeout = 3000;
            using (Stream stream = request.GetRequestStream())
            {
                stream.Write(data, 0, data.Length);
            }
            using (HttpWebResponse response = (HttpWebResponse)request.GetResponse())
            {
                if (response.StatusCode != HttpStatusCode.OK)
                {
                    throw new WebException("O ELETRIC respondeu com " + response.StatusCode + ".");
                }
            }
        }

        private static string ReadResponse(WebResponse response)
        {
            using (response)
            using (StreamReader reader = new StreamReader(response.GetResponseStream()))
            {
                return reader.ReadToEnd();
            }
        }
    }
}
