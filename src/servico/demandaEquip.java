package servico;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.table.DefaultTableModel;
import static servico.comodos.somarIluminacao;
import static servico.comodos.somarTomadas;

public class demandaEquip {
    private static final Map<DefaultTableModel, Map<Integer, ResumoTabela>> resumosPorTabela =
            new WeakHashMap<>();
    private static final Map<DefaultTableModel, List<CargaBase>> cargasBasePorTabela =
            new WeakHashMap<>();

    private static final class CargaBase {
        private final String nome;
        private final long potenciaInstalada;
        private final int potenciaCorrigida;

        private CargaBase(String nome, long potenciaInstalada, int potenciaCorrigida) {
            this.nome = nome;
            this.potenciaInstalada = potenciaInstalada;
            this.potenciaCorrigida = potenciaCorrigida;
        }
    }

    private static final class EquipamentoAdicionado {
        private final String nome;
        private final long potenciaInstalada;
        private int indiceNaLista;
        private int potenciaCorrigida;

        private EquipamentoAdicionado(String nome, long potenciaInstalada, int indiceNaLista) {
            this.nome = nome;
            this.potenciaInstalada = potenciaInstalada;
            this.indiceNaLista = indiceNaLista;
        }
    }

    public static final class CargaEspecial {
        private final String nome;
        private final int tabela;
        private final long potenciaInstalada;
        private final int indiceNaLista;
        private final int potenciaCorrigida;

        private CargaEspecial(String nome, int tabela, long potenciaInstalada,
                              int indiceNaLista, int potenciaCorrigida) {
            this.nome = nome;
            this.tabela = tabela;
            this.potenciaInstalada = potenciaInstalada;
            this.indiceNaLista = indiceNaLista;
            this.potenciaCorrigida = potenciaCorrigida;
        }

        public String getNome() {
            return nome;
        }

        public int getTabela() {
            return tabela;
        }

        public long getPotenciaInstalada() {
            return potenciaInstalada;
        }

        public int getPotenciaCorrigida() {
            return potenciaCorrigida;
        }
    }

    private static final class ResumoTabela {
        private final List<EquipamentoAdicionado> equipamentos = new ArrayList<>();
    }
    // Tabela 6: Tugs e iluminação
    public static double demandaTab6(int cargaInst) {
        if (cargaInst <= 1000) return 0.86;
        else if (cargaInst <= 2000) return 0.75;
        else if (cargaInst <= 3000) return 0.66;
        else if (cargaInst <= 4000) {return 0.59;}
        else if (cargaInst <= 5000) {return 0.52;}
        else if (cargaInst <= 6000) {return 0.45;}
        else if (cargaInst <= 7000) {return 0.40;}
        else if (cargaInst <= 8000) {return 0.35;}
        else if (cargaInst <= 9000) {return 0.31;}
        else if (cargaInst <= 10000) {return 0.27;}
        else {return 0.24;}
    }

    // Tabela 7: Chuveiros, Torneiras, Aquecedores de Passagem e Ferros Elétricos
    public static double demandaTab7(int qtdAparelhos) {
        if (qtdAparelhos <= 0) return 0.0;
        if (qtdAparelhos == 1) return 1.00;
        if (qtdAparelhos == 2) return 1.00;
        if (qtdAparelhos == 3) return 0.84;
        if (qtdAparelhos == 4) return 0.76;
        if (qtdAparelhos == 5) return 0.70;
        if (qtdAparelhos == 6) return 0.65;
        if (qtdAparelhos == 7) return 0.60;
        if (qtdAparelhos == 8) return 0.57;
        if (qtdAparelhos == 9) return 0.54;
        if (qtdAparelhos == 10) return 0.52;
        if (qtdAparelhos == 11) return 0.49;
        if (qtdAparelhos == 12) return 0.48;
        if (qtdAparelhos == 13) return 0.46;
        if (qtdAparelhos == 14) return 0.45;
        if (qtdAparelhos == 15) return 0.44;
        if (qtdAparelhos == 16) return 0.43;
        if (qtdAparelhos == 17) return 0.42;
        if (qtdAparelhos == 18) return 0.41;
        if (qtdAparelhos >= 19 && qtdAparelhos <= 20) return 0.40;
        if (qtdAparelhos == 21) return 0.39;
        if (qtdAparelhos >= 22 && qtdAparelhos <= 23) return 0.39;
        return 0.38;
    }
    // Tabela 8: Aquecedor central ou boiler
    public static double demandaTab8(int qtdAparelhos) {
        if (qtdAparelhos == 1) return 1.00;
        if (qtdAparelhos == 2) return 0.72;
        return 0.64;
    }

    // Tabela 9: Secadora, Forno, Máquina de Lavar, Lava-louça, Micro-ondas
    public static double demandaTab9(int qtdAparelhos) {
        if (qtdAparelhos <= 0) return 0.0;
        if (qtdAparelhos == 1) return 1.00;
        if (qtdAparelhos >= 2 && qtdAparelhos <= 4) return 0.70;
        if (qtdAparelhos >= 5 && qtdAparelhos <= 6) return 0.60;
        return 0.50;
    }
    // Tabela 10: Fogões elétricos
    public static double demandaTab10(int qtdAparelhos) {
        if (qtdAparelhos == 1) return 1.00;
        if (qtdAparelhos == 2) return 0.60;
        if (qtdAparelhos == 3) return 0.48;
        if (qtdAparelhos == 4) return 0.40;
        if (qtdAparelhos == 5) return 0.37;
        if (qtdAparelhos == 6) return 0.35;
        if (qtdAparelhos == 7) return 0.33;
        if (qtdAparelhos == 8) return 0.32;
        if (qtdAparelhos == 9) return 0.31;
        if (qtdAparelhos == 10 || qtdAparelhos == 11) return 0.30;
        if (qtdAparelhos >= 12 && qtdAparelhos <= 15) return 0.28;
        return 0.26;
    }
    // Tabela 11: Condicionadores de ar
    public static double demandaTab11(int capacidadeBTU) {
        if (capacidadeBTU == 7500) return 1100;
        if (capacidadeBTU == 9000) return 1550;;
        if (capacidadeBTU == 10000) return 1650;
        if (capacidadeBTU == 12000) return 1900;
        if (capacidadeBTU == 15000) return 2100;;
        if (capacidadeBTU == 18000) return 2860;
        if (capacidadeBTU == 21000) return 3080;
        if (capacidadeBTU == 30000) return 4000;
        if (capacidadeBTU == 41000) return 5500;
        if (capacidadeBTU == 60000) return 9000;
        return 0;
    }


    // Tabela 12: Condicionadores de Ar (Apenas para mais que 10 aparelhos)
    public static double demandaTab12(int qtdAparelhos) {
        if (qtdAparelhos <= 10) return 1.00;
        if (qtdAparelhos <= 20) return 0.90;
        if (qtdAparelhos <= 30) return 0.82;
        if (qtdAparelhos <= 40) return 0.80;
        if (qtdAparelhos <= 50) return 0.77;
        if (qtdAparelhos <= 75) return 0.75;
        if (qtdAparelhos <= 100) return 0.75;
        return 0.75;
    }

    // Tabela 16: Bombas e Hidromassagem
    public static double demandaTab16(int qtdAparelhos) {
        if (qtdAparelhos <= 0) return 0.0;
        if (qtdAparelhos == 1) return 1.00;
        if (qtdAparelhos == 2) return 0.56;
        if (qtdAparelhos == 3) return 0.47;
        return 0.39;
    }
    public static long PotenciaTugIlu(){
        long tmds = somarTomadas();
        long ilum = somarIluminacao();
        return tmds + ilum;
    }
    public static void itemText(String item, int qtdAparelhos, int potInstalada, int potCorrigida,
                                DefaultTableModel modeloEquipamentos) {
        List<CargaBase> cargasBase = cargasBasePorTabela.computeIfAbsent(
                modeloEquipamentos, key -> new ArrayList<CargaBase>());
        cargasBase.add(new CargaBase(item, potInstalada, potCorrigida));
        reconstruirTabelaEquipamentos(modeloEquipamentos);
    }

    public static void importarCarga(DefaultTableModel modeloEquipamentos, int tabela,
                                     String nome, long potenciaInstalada) {
        if (potenciaInstalada <= 0) {
            throw new IllegalArgumentException("A potência do equipamento deve ser maior que zero.");
        }
        if (tabela != 7 && tabela != 8 && tabela != 9 && tabela != 10 && tabela != 11 && tabela != 16) {
            throw new IllegalArgumentException("Tabela de demanda não suportada: " + tabela);
        }
        Map<Integer, ResumoTabela> resumos = resumosPorTabela.computeIfAbsent(
                modeloEquipamentos, key -> new HashMap<Integer, ResumoTabela>());
        ResumoTabela resumo = resumos.computeIfAbsent(tabela, key -> new ResumoTabela());
        String nomeUnico = nomeUnico(nome, resumos);
        resumo.equipamentos.add(new EquipamentoAdicionado(nomeUnico, potenciaInstalada, -1));
        reconstruirTabelaEquipamentos(modeloEquipamentos);
    }

    public static void calcEquip(JComboBox<String> selecEquip, DefaultTableModel modeloEquipamentos,
                                 JTextField txtNomeEquipamento, JTextField txtPotenciaTotal, String potStr) {
        if (selecEquip.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, "Selecione um tipo de equipamento.");
            return;
        }

        if (potStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Preencha a potência ou capacidade do equipamento.");
            return;
        }

        try {
            int potencia = Integer.parseInt(potStr);
            if (potencia <= 0) {
                JOptionPane.showMessageDialog(null, "A potência deve ser maior que zero.");
                return;
            }

            String equipamento = selecEquip.getSelectedItem().toString();
            int tabela = tabelaDoEquipamento(equipamento);
            long potenciaPorUnidade = tabela == 11 ? (long) demandaTab11(potencia) : potencia;
            if (potenciaPorUnidade <= 0) {
                JOptionPane.showMessageDialog(null, "Capacidade BTU não reconhecida para a Tabela 11.");
                return;
            }

            Map<Integer, ResumoTabela> resumos = resumosPorTabela.computeIfAbsent(
                    modeloEquipamentos, key -> new HashMap<Integer, ResumoTabela>());
            ResumoTabela resumo = resumos.computeIfAbsent(tabela, key -> new ResumoTabela());
            String nomeBase = nomeDoEquipamento(equipamento);
            String nomePersonalizado = txtNomeEquipamento.getText().trim();
            if (!nomePersonalizado.isEmpty()) {
                nomeBase += " " + nomePersonalizado;
            }
            String nomeFinal = nomeUnico(nomeBase, resumos);
            resumo.equipamentos.add(new EquipamentoAdicionado(
                    nomeFinal, potenciaPorUnidade, -1));
            reconstruirTabelaEquipamentos(modeloEquipamentos);

            txtNomeEquipamento.setText("");
            txtPotenciaTotal.setText("");
            txtNomeEquipamento.requestFocus();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Insira valores numéricos válidos.");
        }
    }

    public static List<CargaEspecial> obterCargasEspeciais(DefaultTableModel modeloEquipamentos) {
        List<CargaEspecial> cargas = new ArrayList<>();
        Map<Integer, ResumoTabela> resumos = resumosPorTabela.get(modeloEquipamentos);
        if (resumos == null) {
            return cargas;
        }
        for (Map.Entry<Integer, ResumoTabela> entrada : resumos.entrySet()) {
            ResumoTabela resumo = entrada.getValue();
            for (EquipamentoAdicionado item : resumo.equipamentos) {
                cargas.add(new CargaEspecial(item.nome, entrada.getKey(), item.potenciaInstalada,
                        item.indiceNaLista, item.potenciaCorrigida));
            }
        }
        cargas.sort(Comparator.comparingInt(carga -> carga.indiceNaLista));
        return cargas;
    }

    public static void removerEquipamentosSelecionados(JTable tabelaEquipamentos,
                                                        DefaultTableModel modeloEquipamentos) {
        int[] indicesSelecionados = tabelaEquipamentos.getSelectedRows();
        if (indicesSelecionados.length == 0) {
            JOptionPane.showMessageDialog(tabelaEquipamentos, "Selecione ao menos um item para remover.");
            return;
        }

        Map<Integer, ResumoTabela> resumos = resumosPorTabela.get(modeloEquipamentos);
        if (resumos == null) {
            JOptionPane.showMessageDialog(tabelaEquipamentos, "Não há equipamentos removíveis selecionados.");
            return;
        }
        for (int i = indicesSelecionados.length - 1; i >= 0; i--) {
            int indiceRemovido = indicesSelecionados[i];
            Integer tabelaSelecionada = resumos.entrySet().stream()
                    .filter(entrada -> entrada.getValue().equipamentos.stream()
                            .anyMatch(item -> item.indiceNaLista == indiceRemovido))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            boolean removivel = tabelaSelecionada != null;
            if (!removivel) {
                JOptionPane.showMessageDialog(tabelaEquipamentos,
                        "Selecione uma linha de equipamento para remover; os subtotais são calculados automaticamente.");
                continue;
            }
            ResumoTabela resumo = resumos.get(tabelaSelecionada);
            resumo.equipamentos.removeIf(item -> item.indiceNaLista == indiceRemovido);
            if (resumo.equipamentos.isEmpty()) {
                resumos.remove(tabelaSelecionada);
            }
        }

        reconstruirTabelaEquipamentos(modeloEquipamentos);
    }

    private static void atualizarResumoTabela(int tabela, ResumoTabela resumo) {
        int quantidadeTotal = resumo.equipamentos.size();
        long potenciaInstaladaTotal = resumo.equipamentos.stream().mapToLong(item -> item.potenciaInstalada).sum();
        double fatorDemanda = fatorDaTabela(tabela, quantidadeTotal);
        int potenciaCorrigidaTotal = (int) Math.round(potenciaInstaladaTotal * fatorDemanda);
        int potenciaCorrigidaDistribuida = 0;

        for (int i = 0; i < resumo.equipamentos.size(); i++) {
            EquipamentoAdicionado item = resumo.equipamentos.get(i);
            int potenciaCorrigidaItem;
            if (i == resumo.equipamentos.size() - 1) {
                potenciaCorrigidaItem = potenciaCorrigidaTotal - potenciaCorrigidaDistribuida;
            } else {
                potenciaCorrigidaItem = (int) Math.floor(item.potenciaInstalada * fatorDemanda);
                potenciaCorrigidaDistribuida += potenciaCorrigidaItem;
            }
            item.potenciaCorrigida = potenciaCorrigidaItem;
        }
    }

    private static void reconstruirTabelaEquipamentos(DefaultTableModel modeloEquipamentos) {
        modeloEquipamentos.setRowCount(0);
        long totalCorrigido = 0;

        List<CargaBase> cargasBase = cargasBasePorTabela.get(modeloEquipamentos);
        if (cargasBase != null && !cargasBase.isEmpty()) {
            long potenciaCorrigida = 0;
            for (CargaBase carga : cargasBase) {
                modeloEquipamentos.addRow(new Object[]{carga.nome, carga.potenciaInstalada});
                potenciaCorrigida += carga.potenciaCorrigida;
            }
            modeloEquipamentos.addRow(new Object[]{"Subtotal - tomadas e iluminação (potência corrigida)",
                    potenciaCorrigida});
            totalCorrigido += potenciaCorrigida;
        }

        Map<Integer, ResumoTabela> resumos = resumosPorTabela.get(modeloEquipamentos);
        if (resumos != null) {
            List<Integer> tabelas = new ArrayList<>(resumos.keySet());
            tabelas.sort(Integer::compareTo);
            for (Integer tabela : tabelas) {
                ResumoTabela resumo = resumos.get(tabela);
                atualizarResumoTabela(tabela, resumo);
                long potenciaCorrigida = 0;
                for (EquipamentoAdicionado item : resumo.equipamentos) {
                    item.indiceNaLista = modeloEquipamentos.getRowCount();
                    modeloEquipamentos.addRow(new Object[]{item.nome, item.potenciaInstalada});
                    potenciaCorrigida += item.potenciaCorrigida;
                }
                modeloEquipamentos.addRow(new Object[]{
                        "Subtotal - " + nomeGrupo(tabela) + " (potência corrigida)", potenciaCorrigida
                });
                totalCorrigido += potenciaCorrigida;
            }
        }

        modeloEquipamentos.addRow(new Object[]{"TOTAL - potência corrigida", totalCorrigido});
    }

    private static String nomeGrupo(int tabela) {
        switch (tabela) {
            case 7: return "Aquecimento instantâneo e ferro";
            case 8: return "Aquecimento central";
            case 9: return "Eletrodomésticos";
            case 10: return "Fogões elétricos";
            case 11: return "Ar-condicionado";
            case 16: return "Bombas e hidromassagem";
            default: return "Equipamentos";
        }
    }

    private static String nomeUnico(String nomeBase, Map<Integer, ResumoTabela> resumos) {
        if (!nomeExiste(nomeBase, resumos)) {
            return nomeBase;
        }
        int numero = 1;
        String nomeFinal;
        do {
            nomeFinal = nomeBase + " " + numero++;
        } while (nomeExiste(nomeFinal, resumos));
        return nomeFinal;
    }

    private static boolean nomeExiste(String nome, Map<Integer, ResumoTabela> resumos) {
        for (ResumoTabela resumo : resumos.values()) {
            for (EquipamentoAdicionado item : resumo.equipamentos) {
                if (item.nome.equalsIgnoreCase(nome)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int tabelaDoEquipamento(String equipamento) {
        if (equipamento.endsWith("(tab7)")) return 7;
        if (equipamento.endsWith("(tab8)")) return 8;
        if (equipamento.endsWith("(tab9)")) return 9;
        if (equipamento.endsWith("(tab10)")) return 10;
        if (equipamento.endsWith("(tab11)")) return 11;
        if (equipamento.endsWith("(tab16)")) return 16;
        throw new IllegalArgumentException("Tabela não reconhecida para o equipamento: " + equipamento);
    }

    private static String nomeDoEquipamento(String equipamento) {
        return equipamento.replaceFirst("\\(tab\\d+\\)$", "").trim();
    }

    private static double fatorDaTabela(int tabela, int quantidade) {
        switch (tabela) {
            case 7: return demandaTab7(quantidade);
            case 8: return demandaTab8(quantidade);
            case 9: return demandaTab9(quantidade);
            case 10: return demandaTab10(quantidade);
            case 11: return demandaTab12(quantidade);
            case 16: return demandaTab16(quantidade);
            default: throw new IllegalArgumentException("Tabela de demanda não suportada: " + tabela);
        }
    }
}