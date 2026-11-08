package servico;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class exportadorExcel {
    private exportadorExcel() {
    }

    public static void exportar(Path arquivo, Map<String, List<List<Object>>> planilhas) throws IOException {
        try (OutputStream arquivoSaida = Files.newOutputStream(arquivo);
             ZipOutputStream zip = new ZipOutputStream(arquivoSaida, StandardCharsets.UTF_8)) {
            escreverEntrada(zip, "[Content_Types].xml", montarTiposConteudo(planilhas.size()));
            escreverEntrada(zip, "_rels/.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                            + "</Relationships>");
            escreverEntrada(zip, "xl/workbook.xml", montarWorkbook(planilhas));
            escreverEntrada(zip, "xl/_rels/workbook.xml.rels", montarRelacionamentos(planilhas.size()));

            int numeroPlanilha = 1;
            for (List<List<Object>> linhas : planilhas.values()) {
                escreverEntrada(zip, "xl/worksheets/sheet" + numeroPlanilha + ".xml",
                        montarPlanilha(linhas));
                numeroPlanilha++;
            }
        }
    }

    private static String montarTiposConteudo(int quantidadePlanilhas) {
        StringBuilder xml = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                        + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                        + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                        + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                        + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>");
        for (int i = 1; i <= quantidadePlanilhas; i++) {
            xml.append("<Override PartName=\"/xl/worksheets/sheet").append(i)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        return xml.append("</Types>").toString();
    }

    private static String montarWorkbook(Map<String, List<List<Object>>> planilhas) {
        StringBuilder xml = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                        + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                        + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets>");
        int i = 1;
        for (String nome : planilhas.keySet()) {
            xml.append("<sheet name=\"").append(escaparXml(nome)).append("\" sheetId=\"")
                    .append(i).append("\" r:id=\"rId").append(i).append("\"/>");
            i++;
        }
        return xml.append("</sheets></workbook>").toString();
    }

    private static String montarRelacionamentos(int quantidadePlanilhas) {
        StringBuilder xml = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                        + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 1; i <= quantidadePlanilhas; i++) {
            xml.append("<Relationship Id=\"rId").append(i)
                    .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
                    .append(i).append(".xml\"/>");
        }
        return xml.append("</Relationships>").toString();
    }

    private static String montarPlanilha(List<List<Object>> linhas) {
        StringBuilder xml = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                        + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        for (int linha = 0; linha < linhas.size(); linha++) {
            xml.append("<row r=\"").append(linha + 1).append("\">");
            List<Object> valores = linhas.get(linha);
            for (int coluna = 0; coluna < valores.size(); coluna++) {
                Object valor = valores.get(coluna);
                if (valor == null) {
                    continue;
                }
                String referencia = referenciaCelula(coluna + 1, linha + 1);
                if (valor instanceof Number) {
                    xml.append("<c r=\"").append(referencia).append("\"><v>")
                            .append(valor).append("</v></c>");
                } else {
                    xml.append("<c r=\"").append(referencia)
                            .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                            .append(escaparXml(valor.toString())).append("</t></is></c>");
                }
            }
            xml.append("</row>");
        }
        return xml.append("</sheetData></worksheet>").toString();
    }

    private static String referenciaCelula(int coluna, int linha) {
        StringBuilder referencia = new StringBuilder();
        while (coluna > 0) {
            int resto = (coluna - 1) % 26;
            referencia.insert(0, (char) ('A' + resto));
            coluna = (coluna - 1) / 26;
        }
        return referencia.append(linha).toString();
    }

    private static String escaparXml(String valor) {
        StringBuilder escapado = new StringBuilder();
        for (int i = 0; i < valor.length(); i++) {
            char atual = valor.charAt(i);
            if (atual == '&') escapado.append("&amp;");
            else if (atual == '<') escapado.append("&lt;");
            else if (atual == '>') escapado.append("&gt;");
            else if (atual == '"') escapado.append("&quot;");
            else if (atual == '\'') escapado.append("&apos;");
            else if (atual == '\t' || atual == '\n' || atual == '\r' || atual >= 0x20) {
                escapado.append(atual);
            }
        }
        return escapado.toString();
    }

    private static void escreverEntrada(ZipOutputStream zip, String nome, String conteudo) throws IOException {
        zip.putNextEntry(new ZipEntry(nome));
        zip.write(conteudo.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
