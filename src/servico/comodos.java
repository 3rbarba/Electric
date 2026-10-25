package servico;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Locale;

import static java.lang.System.out;

public class comodos {
    public static HashMap<String, Integer> comodoTMDVA = new HashMap<>();
    public static HashMap<String, Integer> comodoQtdTomadas = new HashMap<>();
    public static HashMap<String, Integer> comodoILUVA = new HashMap<>();
    public static HashMap<String, Double> comodoPerimetro = new HashMap<>();
    public static HashMap<String, Double> comodoArea = new HashMap<>();
    public static HashMap<String, String> comodoTipo = new HashMap<>();

    public static long somarTomadas() {
        return comodoTMDVA.values().stream().mapToLong(Integer::longValue).sum();
    }

    public static int somarQuantidadeTomadas() {
        return comodoQtdTomadas.values().stream().mapToInt(Integer::intValue).sum();
    }

    public static long somarIluminacao() {
        return comodoILUVA.values().stream().mapToLong(Integer::longValue).sum();
    }

    public static boolean calcComodos(DefaultTableModel model) {

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "A lista de cômodos está vazia! Adicione cômodos antes de calcular.");
            return false;
        }
        comodoTMDVA.clear();
        comodoQtdTomadas.clear();
        comodoILUVA.clear();

        for (int i = 0; i < model.getRowCount(); i++) {
            String nomeOriginal = (String) model.getValueAt(i, 0);
            String tipoComodo = comodoTipo.getOrDefault(nomeOriginal, inferirTipo(nomeOriginal))
                    .toLowerCase(Locale.ROOT);

            double perimetro = comodoPerimetro.getOrDefault(nomeOriginal, 0.0);
            double area = comodoArea.getOrDefault(nomeOriginal, 0.0);

            switch (tipoComodo) {
                //Quarto Sala
                case "sala":
                case "quarto":
                    comodoTMDVA.put(nomeOriginal, calcSalaQuarto(area, perimetro, tipoComodo));
                    comodoQtdTomadas.put(nomeOriginal, calcQuantidadeSalaQuarto(area, perimetro));
                    comodoILUVA.put(nomeOriginal, calcIlum(area));
                    break;
                //areas molhadas
                case "cozinha":
                case "banheiro":
                case "lavanderia":
                case "área de serviço":
                case "suíte":
                    comodoTMDVA.put(nomeOriginal, calcAreaMolhada(perimetro, tipoComodo));
                    comodoQtdTomadas.put(nomeOriginal, calcQuantidadeAreaMolhada(perimetro, tipoComodo));
                    comodoILUVA.put(nomeOriginal, calcIlum(area));
                    break;
                //outros comodos
                case "varanda":
                case "corredor":
                case "hall":
                case "outros":
                    comodoTMDVA.put(nomeOriginal, calcOutrosComodos(area, perimetro, tipoComodo));
                    comodoQtdTomadas.put(nomeOriginal, calcQuantidadeOutrosComodos(area, perimetro, tipoComodo));
                    comodoILUVA.put(nomeOriginal, calcIlum(area));
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Cômodo não reconhecido: " + nomeOriginal);
                    comodoTMDVA.clear();
                    comodoQtdTomadas.clear();
                    comodoILUVA.clear();
                    return false;
            }
        }
        return true;
    }

    private static String inferirTipo(String nomeComodo) {
        String nome = nomeComodo.toLowerCase(Locale.ROOT);
        if (nome.startsWith("área de serviço")) return "área de serviço";
        if (nome.startsWith("lavanderia")) return "lavanderia";
        if (nome.startsWith("banheiro")) return "banheiro";
        if (nome.startsWith("cozinha")) return "cozinha";
        if (nome.startsWith("suíte") || nome.startsWith("suite")) return "suíte";
        if (nome.startsWith("sala")) return "sala";
        if (nome.startsWith("quarto")) return "quarto";
        if (nome.startsWith("varanda")) return "varanda";
        if (nome.startsWith("corredor")) return "corredor";
        if (nome.startsWith("hall")) return "hall";
        if (nome.startsWith("outros")) return "outros";
        return nome;
    }

    public static int calcAreaMolhada(double perimetro, String comodo) {
        int quantidadeTMDS = 1;

        if (!comodo.contains("banheiro")) {
            quantidadeTMDS = (int) Math.ceil(perimetro / 3.5);
        } else {
            return 600;
        }
        int potenciaTMDS;
        if (quantidadeTMDS > 6) {
            potenciaTMDS = 1200 + ((quantidadeTMDS - 2) * 100);
        } else {
            potenciaTMDS = 1800 + ((quantidadeTMDS - 3) * 100);
        }
        return potenciaTMDS;
    }

    public static int calcQuantidadeAreaMolhada(double perimetro, String comodo) {
        if (comodo.contains("banheiro")) {
            return 1;
        }
        return (int) Math.ceil(perimetro / 3.5);
    }

    public static int calcOutrosComodos(double area, double perimetro, String comodo) {
        int quantidadeTMDS = 1;
        int potenciaTotalVA = 100;
        if (!comodo.contains("varanda")) {
            if (area >= 6) {
                quantidadeTMDS = (int) Math.ceil(perimetro / 5);
                potenciaTotalVA = quantidadeTMDS * 100;
            }
        }
        return potenciaTotalVA;
    }

    public static int calcQuantidadeOutrosComodos(double area, double perimetro, String comodo) {
        int quantidadeTMDS = 1;
        if (!comodo.contains("varanda")) {
            if (area >= 6) {
                quantidadeTMDS = (int) Math.ceil(perimetro / 5);
            }
        }
        return quantidadeTMDS;
    }

    public static int calcSalaQuarto(double area, double perimetro, String comodo) {
        int quantidadeTMDS = 1;
        if (area >= 5) {
            quantidadeTMDS = (int) Math.ceil(perimetro / 5);
        }
        int potenciaTotalVA = quantidadeTMDS * 100;
        return potenciaTotalVA;
    }

    public static int calcQuantidadeSalaQuarto(double area, double perimetro) {
        int quantidadeTMDS = 1;
        if (area >= 5) {
            quantidadeTMDS = (int) Math.ceil(perimetro / 5);
        }
        return quantidadeTMDS;
    }

    public static int calcIlum(double area) {
        int quantidadeILUVA = 1;
        int potenciaTotalILUVA = 100;
        if (area >= 10) {
            area = area - 6;
            potenciaTotalILUVA = (int) (Math.floor(area / 4) * 60) + 100;
        }
        return potenciaTotalILUVA;
    }
}