package screen;

import servico.QuadroCargasService;
import servico.comodos;
import servico.demandaEquip;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Base64;
import java.util.TreeSet;

public class circuitosScreen {
    private static final Color PAINEL = new Color(32, 53, 61);
    private static final Color TEXTO = new Color(238, 241, 243);
    private static final String[] COLUNAS = {
            "Nº Circuito", "Descrição", "Tipo", "Tensão (V)", "Pot. Inst. (W)",
            "Ip (A)", "Ic (A)", "Iz (A)", "Disj (A)", "S (mm²)", "DR", "Fase"
    };
    private static final String[] METODOS = {"A1", "A2", "B1", "B2", "C", "D"};

    private JPanel main;
    private JPanel panelQuadro;
    private JPanel panelConfiguracao;
    private JPanel panelSeletores;
    private JPanel panelAcoes;
    private JPanel panelRodape;
    private JScrollPane scrollTabela;
    private final DefaultTableModel equipamentos;
    private final DefaultTableModel modeloTabela;
    private JTable tabela;
    private JComboBox<Integer> comboTensao;
    private JComboBox<QuadroCargasService.SistemaFases> comboSistema;
    private JComboBox<String> comboMetodo;
    private JTextField campoFca;
    private JTextField campoFct;
    private JButton btnAplicarSelecionados;
    private JButton btnAplicarTodos;
    private JButton btnAnterior;
    private JButton btnExportar;
    private JButton btnImprimir;
    private JLabel legenda;
    private final Map<String, QuadroCargasService.ParametrosCircuito> parametrosPorCircuito;
    private QuadroCargasService.Resultado resultado;

    public circuitosScreen(JFrame frame, JPanel painelAnterior, DefaultTableModel equipamentos,
                           Map<String, QuadroCargasService.ParametrosCircuito> parametrosPorCircuito) {
        this(frame, painelAnterior, equipamentos, parametrosPorCircuito,
                QuadroCargasService.SistemaFases.MONOFASICO, 127);
    }

    public circuitosScreen(JFrame frame, JPanel painelAnterior, DefaultTableModel equipamentos,
                           Map<String, QuadroCargasService.ParametrosCircuito> parametrosPorCircuito,
                           QuadroCargasService.SistemaFases sistemaInicial, int tensaoInicial) {
        this.equipamentos = equipamentos;
        this.parametrosPorCircuito = parametrosPorCircuito;
        if (main == null) {
            criarInterfaceSemFormCompiler();
        }
        modeloTabela = new DefaultTableModel(COLUNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabela.setModel(modeloTabela);
        comboTensao.setModel(new DefaultComboBoxModel<>(new Integer[]{127, 220}));
        comboTensao.setSelectedItem(tensaoInicial);
        comboSistema.setModel(new DefaultComboBoxModel<>(QuadroCargasService.SistemaFases.values()));
        comboSistema.setSelectedItem(sistemaInicial);
        comboMetodo.setModel(new DefaultComboBoxModel<>(METODOS));
        comboMetodo.setSelectedItem("B1");
        prepararControle(comboTensao);
        prepararControle(comboSistema);
        prepararControle(comboMetodo);
        prepararCampo(campoFca);
        prepararCampo(campoFct);
        prepararBotao(btnAplicarSelecionados, new Color(17, 135, 143));
        prepararBotao(btnAplicarTodos, new Color(17, 135, 143));
        prepararBotao(btnAnterior, new Color(91, 101, 105));
        prepararBotao(btnExportar, new Color(17, 135, 143));
        prepararBotao(btnImprimir, new Color(17, 135, 143));
        legenda.setForeground(TEXTO);
        legenda.setFont(new Font("Dialog", Font.PLAIN, 11));
        comboSistema.setToolTipText("Monofásico/bifásico: 2 condutores carregados; trifásico: 3.");
        comboTensao.setToolTipText("Tensão nominal dos circuitos terminais.");
        comboMetodo.setToolTipText("Método de instalação usado para consultar a ampacidade do condutor.");
        tabela.setBackground(PAINEL);
        tabela.setForeground(TEXTO);
        tabela.setGridColor(new Color(70, 91, 99));
        tabela.setSelectionBackground(new Color(17, 135, 143));
        tabela.setSelectionForeground(TEXTO);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tabela.getTableHeader().setBackground(new Color(17, 135, 143));
        tabela.getTableHeader().setForeground(TEXTO);
        tabela.getTableHeader().setReorderingAllowed(false);
        int[] larguras = {90, 260, 90, 75, 120, 70, 70, 80, 75, 80, 60, 70};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        scrollTabela.getViewport().setBackground(PAINEL);
        scrollTabela.setBorder(BorderFactory.createLineBorder(new Color(70, 91, 99)));
        DarkScrollBarUI.install(scrollTabela);
        panelQuadro.setBackground(PAINEL);
        panelConfiguracao.setOpaque(false);
        panelSeletores.setOpaque(false);
        panelAcoes.setOpaque(false);
        panelRodape.setOpaque(false);

        comboTensao.addActionListener(e -> recalcular());
        comboSistema.addActionListener(e -> recalcular());
        btnAnterior.addActionListener(e -> {
            frame.setContentPane(painelAnterior);
            frame.revalidate();
            frame.repaint();
        });
        btnExportar.addActionListener(this::exportarMemorial);
        btnImprimir.addActionListener(e -> imprimirTabela());
        btnAplicarSelecionados.addActionListener(e -> aplicarParametros(false));
        btnAplicarTodos.addActionListener(e -> aplicarParametros(true));
        recalcular();
    }

    private void criarInterfaceSemFormCompiler() {
        main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(14, 14, 12, 14));
        main.setBackground(new Color(17, 34, 42));
        panelQuadro = new JPanel(new BorderLayout(8, 8));
        panelQuadro.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 91, 99)),
                "QUADRO DE CARGAS E DIVISÃO DE CIRCUITOS", 0, 0, null, TEXTO));
        panelQuadro.setBackground(PAINEL);

        panelConfiguracao = new JPanel(new java.awt.GridLayout(2, 1, 2, 2));
        panelConfiguracao.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 91, 99)),
                "CONFIGURAÇÃO DOS CIRCUITOS", 0, 0, null, TEXTO));
        panelConfiguracao.setOpaque(false);
        panelSeletores = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 7, 3));
        panelSeletores.setOpaque(false);
        comboSistema = new JComboBox<>();
        comboTensao = new JComboBox<>();
        comboMetodo = new JComboBox<>();
        campoFca = new JTextField("1,00", 5);
        campoFct = new JTextField("1,00", 5);
        panelSeletores.add(rotulo("Sistema:"));
        panelSeletores.add(comboSistema);
        panelSeletores.add(rotulo("Tensão (V):"));
        panelSeletores.add(comboTensao);
        panelSeletores.add(rotulo("Método:"));
        panelSeletores.add(comboMetodo);
        panelSeletores.add(rotulo("FCA:"));
        panelSeletores.add(campoFca);
        panelSeletores.add(rotulo("FCT:"));
        panelSeletores.add(campoFct);

        panelAcoes = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 7, 3));
        panelAcoes.setOpaque(false);
        btnAplicarSelecionados = new JButton("Aplicar Selecionados");
        btnAplicarTodos = new JButton("Aplicar para Todos");
        panelAcoes.add(btnAplicarSelecionados);
        panelAcoes.add(btnAplicarTodos);
        panelConfiguracao.add(panelSeletores);
        panelConfiguracao.add(panelAcoes);
        panelQuadro.add(panelConfiguracao, java.awt.BorderLayout.NORTH);

        tabela = new JTable();
        scrollTabela = new JScrollPane(tabela);
        panelQuadro.add(scrollTabela, java.awt.BorderLayout.CENTER);

        panelRodape = new JPanel(new java.awt.BorderLayout(4, 4));
        panelRodape.setOpaque(false);
        legenda = new JLabel("<html>Ip = Corrente de projeto | Ic = Corrente corrigida | "
                + "Iz = Corrente máxima do cabo<br>Disj = Disjuntor | S (mm²) = Seção do cabo | "
                + "Pot. Inst. = Potência instalada</html>");
        legenda.setForeground(TEXTO);
        panelRodape.add(legenda, java.awt.BorderLayout.NORTH);
        JPanel botoes = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 2));
        botoes.setOpaque(false);
        btnAnterior = new JButton("Anterior");
        btnExportar = new JButton("Exportar memorial (TXT)");
        btnImprimir = new JButton("Imprimir");
        botoes.add(btnAnterior);
        botoes.add(btnExportar);
        botoes.add(btnImprimir);
        panelRodape.add(botoes, java.awt.BorderLayout.SOUTH);
        panelQuadro.add(panelRodape, java.awt.BorderLayout.SOUTH);
        main.add(panelQuadro, java.awt.BorderLayout.CENTER);
    }

    private static JLabel rotulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(TEXTO);
        return label;
    }

    public JPanel getMain() {
        return main;
    }

    private void recalcular() {
        Integer tensaoSelecionada = (Integer) comboTensao.getSelectedItem();
        QuadroCargasService.SistemaFases sistema =
                (QuadroCargasService.SistemaFases) comboSistema.getSelectedItem();
        if (tensaoSelecionada == null || sistema == null) {
            throw new IllegalStateException("Selecione sistema e tensão antes de calcular o quadro de cargas.");
        }
        List<demandaEquip.CargaEspecial> cargas = demandaEquip.obterCargasEspeciais(equipamentos);
        resultado = QuadroCargasService.calcular(
                tensaoSelecionada, sistema, cargas, parametrosPorCircuito);

        modeloTabela.setRowCount(0);
        for (QuadroCargasService.Circuito circuito : resultado.getCircuitos()) {
            modeloTabela.addRow(circuito.toTableRow());
        }
    }

    private void aplicarParametros(boolean todos) {
        try {
            String metodo = (String) comboMetodo.getSelectedItem();
            if (metodo == null) {
                throw new IllegalArgumentException("Selecione um método de instalação.");
            }
            QuadroCargasService.SistemaFases sistema =
                    (QuadroCargasService.SistemaFases) comboSistema.getSelectedItem();
            int condutores = sistema == QuadroCargasService.SistemaFases.TRIFASICO ? 3 : 2;
            QuadroCargasService.ParametrosCircuito parametros =
                    new QuadroCargasService.ParametrosCircuito(
                            metodo, condutores, parseFator(campoFca.getText()), parseFator(campoFct.getText()));

            if (todos) {
                for (QuadroCargasService.Circuito circuito : resultado.getCircuitos()) {
                    parametrosPorCircuito.put(circuito.getChaveConfiguracao(), parametros);
                }
            } else {
                int[] selecionadas = tabela.getSelectedRows();
                if (selecionadas.length == 0) {
                    JOptionPane.showMessageDialog(main, "Selecione ao menos um circuito na tabela.");
                    return;
                }
                for (int linha : selecionadas) {
                    int indiceModelo = tabela.convertRowIndexToModel(linha);
                    QuadroCargasService.Circuito circuito = resultado.getCircuitos().get(indiceModelo);
                    parametrosPorCircuito.put(circuito.getChaveConfiguracao(), parametros);
                }
            }
            recalcular();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(main, ex.getMessage(),
                    "Configuração inválida", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarMemorial(ActionEvent evento) {
        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle("Salvar memorial de cálculo");
        seletor.setSelectedFile(new java.io.File("memorial_quadro_de_cargas.txt"));
        if (seletor.showSaveDialog(main) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.write(seletor.getSelectedFile().toPath(),
                    montarMemorial().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(main, "Memorial exportado com sucesso.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(main, "Não foi possível exportar o memorial: " + ex.getMessage(),
                    "Erro ao exportar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String montarMemorial() {
        StringBuilder texto = montarBlocoImportacao();
        texto.append("MEMORIAL - QUADRO DE CARGAS E DIVISÃO DE CIRCUITOS\n\n");
        texto.append("Sistema: ").append(comboSistema.getSelectedItem())
                .append(" | Tensão nominal: ").append(comboTensao.getSelectedItem()).append(" V\n")
                .append("Ampacidade Iz: referência da Tabela 36 (cobre/PVC 70 °C); validar edição vigente da NBR 5410 e instalação real.\n\n");

        texto.append("CÔMODOS, ÁREAS E CARGAS\n");
        texto.append("Cômodo\tTipo\tÁrea (m²)\tPerímetro (m)\tTomadas (VA)\tIluminação (VA)\n");
        for (String nome : new TreeSet<>(comodos.comodoArea.keySet())) {
            texto.append(nome).append('\t')
                    .append(comodos.comodoTipo.getOrDefault(nome, "")).append('\t')
                    .append(formatar(comodos.comodoArea.getOrDefault(nome, 0.0))).append('\t')
                    .append(formatar(comodos.comodoPerimetro.getOrDefault(nome, 0.0))).append('\t')
                    .append(comodos.comodoTMDVA.getOrDefault(nome, 0)).append('\t')
                    .append(comodos.comodoILUVA.getOrDefault(nome, 0)).append('\n');
        }
        texto.append("TOTAL TOMADAS (VA)\t").append(comodos.somarTomadas()).append('\n')
                .append("TOTAL ILUMINAÇÃO (VA)\t").append(comodos.somarIluminacao()).append('\n')
                .append("TOTAL CÔMODOS (VA)\t")
                .append(comodos.somarTomadas() + comodos.somarIluminacao()).append("\n\n");

        texto.append("EQUIPAMENTOS ESPECIAIS (TUE)\n");
        texto.append("Equipamento\tPotência instalada (W/VA)\tPotência considerada na demanda (W/VA)\n");
        List<demandaEquip.CargaEspecial> cargas = demandaEquip.obterCargasEspeciais(equipamentos);
        for (demandaEquip.CargaEspecial carga : cargas) {
            texto.append(carga.getNome()).append('\t')
                    .append(carga.getPotenciaInstalada()).append('\t')
                    .append(carga.getPotenciaCorrigida()).append('\n');
        }
        texto.append('\n').append(String.join("\t", COLUNAS)).append('\n');
        for (int linha = 0; linha < modeloTabela.getRowCount(); linha++) {
            for (int coluna = 0; coluna < modeloTabela.getColumnCount(); coluna++) {
                if (coluna > 0) texto.append('\t');
                texto.append(modeloTabela.getValueAt(linha, coluna));
            }
            texto.append('\n');
        }

        if (!resultado.getAvisos().isEmpty()) {
            texto.append("\nAVISOS DE DIMENSIONAMENTO\n");
            for (String mensagem : resultado.getAvisos()) {
                texto.append("- ").append(mensagem).append('\n');
            }
        }
        return texto.toString();
    }

    private StringBuilder montarBlocoImportacao() {
        StringBuilder bloco = new StringBuilder("# DADOS_ELETRIC;1\n");
        bloco.append("SISTEMA;")
                .append(((QuadroCargasService.SistemaFases) comboSistema.getSelectedItem()).name())
                .append(';').append(comboTensao.getSelectedItem()).append('\n');
        for (String nome : new TreeSet<>(comodos.comodoArea.keySet())) {
            bloco.append("COMODO;").append(codificar(nome)).append(';')
                    .append(codificar(comodos.comodoTipo.getOrDefault(nome, ""))).append(';')
                    .append(String.format(Locale.ROOT, "%.10f",
                            comodos.comodoArea.getOrDefault(nome, 0.0))).append(';')
                    .append(String.format(Locale.ROOT, "%.10f",
                            comodos.comodoPerimetro.getOrDefault(nome, 0.0))).append('\n');
        }
        for (demandaEquip.CargaEspecial carga : demandaEquip.obterCargasEspeciais(equipamentos)) {
            bloco.append("EQUIPAMENTO;").append(carga.getTabela()).append(';')
                    .append(codificar(carga.getNome())).append(';')
                    .append(carga.getPotenciaInstalada()).append('\n');
        }
        for (QuadroCargasService.Circuito circuito : resultado.getCircuitos()) {
            QuadroCargasService.ParametrosCircuito parametros = circuito.getParametros();
            bloco.append("CIRCUITO;").append(codificar(circuito.getChaveConfiguracao())).append(';')
                    .append(parametros.getMetodoInstalacao()).append(';')
                    .append(parametros.getCondutoresCarregados()).append(';')
                    .append(String.format(Locale.ROOT, "%.10f", parametros.getFca())).append(';')
                    .append(String.format(Locale.ROOT, "%.10f", parametros.getFct())).append('\n');
        }
        return bloco.append("# FIM_DADOS_ELETRIC\n\n");
    }

    private static String codificar(String valor) {
        return Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }

    private void imprimirTabela() {
        try {
            JEditorPane memorial = new JEditorPane("text/html", montarMemorialHtml());
            memorial.setEditable(false);

            // Define o nome do trabalho (JobName) através dos atributos de impressão
            PrintRequestAttributeSet atributos = new HashPrintRequestAttributeSet();

            atributos.add(new JobName("Memorial de Cálculo - Quadro de Cargas", Locale.forLanguageTag("pt-BR")));

            // Executa a impressão passando os atributos corretos
            boolean impresso = memorial.print(
                    new MessageFormat("Memorial - Quadro de Cargas"),
                    new MessageFormat(""),
                    false,
                    null,
                    atributos,
                    true
            );

            if (impresso) {
                JOptionPane.showMessageDialog(main, "Memorial completo enviado para impressão.");
            }
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(main, "Não foi possível imprimir o memorial: " + ex.getMessage(),
                    "Erro de impressão", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String montarMemorialHtml() {
        StringBuilder html = new StringBuilder(
                "<html><head><meta charset='UTF-8'><style>"
                        + "body{font-family:Arial,sans-serif;font-size:9pt;color:#17242a}"
                        + "h1{font-size:16pt}h2{font-size:12pt;margin-top:16px}"
                        + "table{border-collapse:collapse;width:100%;margin-bottom:10px}"
                        + "th,td{border:1px solid #53666d;padding:4px;text-align:left}"
                        + "th{background:#d9e5e8}td.num{text-align:right}"
                        + "</style></head><body>");
        html.append("<h1>Memorial - Dimensionamento Elétrico</h1>")
                .append("<p>Sistema: ").append(htmlEscape(String.valueOf(comboSistema.getSelectedItem())))
                .append(" | Tensão nominal: ").append(htmlEscape(String.valueOf(comboTensao.getSelectedItem())))
                .append(" V<br>Ampacidade de referência: Tabela 36 (cobre/PVC 70 °C).</p>");

        html.append("<h2>Cômodos, Áreas e Cargas</h2>")
                .append("<table><thead><tr>")
                .append("<th>Cômodo</th><th>Tipo</th><th>Área (m²)</th><th>Perímetro (m)</th>")
                .append("<th>Tomadas (VA)</th><th>Iluminação (VA)</th>")
                .append("</tr></thead><tbody>");
        for (String nome : new TreeSet<>(comodos.comodoArea.keySet())) {
            html.append("<tr><td>").append(htmlEscape(nome)).append("</td><td>")
                    .append(htmlEscape(comodos.comodoTipo.getOrDefault(nome, ""))).append("</td><td class='num'>")
                    .append(formatar(comodos.comodoArea.getOrDefault(nome, 0.0))).append("</td><td class='num'>")
                    .append(formatar(comodos.comodoPerimetro.getOrDefault(nome, 0.0))).append("</td><td class='num'>")
                    .append(comodos.comodoTMDVA.getOrDefault(nome, 0)).append("</td><td class='num'>")
                    .append(comodos.comodoILUVA.getOrDefault(nome, 0)).append("</td></tr>");
        }
        html.append("<tr><th colspan='4'>Totais dos cômodos</th><th>")
                .append(comodos.somarTomadas()).append(" VA</th><th>")
                .append(comodos.somarIluminacao()).append(" VA</th></tr>")
                .append("<tr><th colspan='5'>Total geral de tomadas e iluminação</th><th>")
                .append(comodos.somarTomadas() + comodos.somarIluminacao()).append(" VA</th></tr>")
                .append("</tbody></table>");

        html.append("<h2>Equipamentos Especiais (TUE)</h2>")
                .append("<table><thead><tr>")
                .append("<th>Equipamento</th><th>Pot. Inst. (W/VA)</th><th>Pot. Corrigida (W/VA)</th>")
                .append("</tr></thead><tbody>");
        List<demandaEquip.CargaEspecial> cargas = demandaEquip.obterCargasEspeciais(equipamentos);
        for (demandaEquip.CargaEspecial carga : cargas) {
            html.append("<tr><td>").append(htmlEscape(carga.getNome())).append("</td><td class='num'>")
                    .append(carga.getPotenciaInstalada()).append("</td><td class='num'>")
                    .append(carga.getPotenciaCorrigida()).append("</td></tr>");
        }
        if (cargas.isEmpty()) {
            html.append("<tr><td colspan='3'>Nenhum equipamento especial cadastrado.</td></tr>");
        }
        html.append("</tbody></table>");

        html.append("<h2>Divisão de Circuitos e Quadro de Cargas</h2>")
                .append("<table><thead><tr>");
        for (String coluna : COLUNAS) {
            html.append("<th>").append(htmlEscape(coluna)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        html.append("</tr></thead><tbody>");
        for (int linha = 0; linha < modeloTabela.getRowCount(); linha++) {
            html.append("<tr>");
            for (int coluna = 0; coluna < modeloTabela.getColumnCount(); coluna++) {
                html.append("<td>").append(htmlEscape(String.valueOf(modeloTabela.getValueAt(linha, coluna))))
                        .append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody></table>");

        html.append("<h2>Resumo</h2><table><tbody>")
                .append("<tr><th>Potência instalada total</th><td>")
                .append(formatar(resultado.getPotenciaInstalada() / 1000.0)).append(" kW</td></tr>")
                .append("<tr><th>Demanda total calculada</th><td>")
                .append(formatar(resultado.getDemanda() / 1000.0)).append(" kVA</td></tr>")
                .append("</tbody></table>");

        html.append("<h2>Legenda da divisão de circuitos</h2><table><thead><tr>")
                .append("<th>Sigla</th><th>Significado</th></tr></thead><tbody>")
                .append("<tr><td>Ip</td><td>Corrente de projeto</td></tr>")
                .append("<tr><td>Ic</td><td>Corrente corrigida (Ip ÷ FCA ÷ FCT)</td></tr>")
                .append("<tr><td>Iz</td><td>Capacidade de condução de corrente do condutor</td></tr>")
                .append("<tr><td>Disj</td><td>Corrente nominal do disjuntor</td></tr>")
                .append("<tr><td>S (mm²)</td><td>Seção nominal do condutor</td></tr>")
                .append("<tr><td>DR</td><td>Dispositivo diferencial residual requerido</td></tr>")
                .append("<tr><td>Pot. Inst.</td><td>Potência instalada no circuito</td></tr>")
                .append("<tr><td>TUG / TUE</td><td>Tomada de uso geral / tomada de uso específico</td></tr>")
                .append("</tbody></table>");

        List<String> avisos = resultado.getAvisos();
        if (!avisos.isEmpty()) {
            html.append("<h2>Avisos de dimensionamento</h2><ul>");
            for (String aviso : avisos) {
                html.append("<li>").append(htmlEscape(aviso)).append("</li>");
            }
            html.append("</ul>");
        }
        return html.append("</body></html>").toString();
    }

    private static String htmlEscape(String valor) {
        return valor.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static void prepararCampo(JTextField campo) {
        campo.setBackground(PAINEL);
        campo.setForeground(TEXTO);
        campo.setCaretColor(TEXTO);
    }

    private static double parseFator(String valor) {
        try {
            return Double.parseDouble(valor.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("FCA e FCT devem ser números; exemplo: 0,90.");
        }
    }

    private static String formatar(double valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.2f", valor);
    }

    private static void prepararControle(JComponent componente) {
        componente.setBackground(PAINEL);
        componente.setForeground(TEXTO);
    }

    private static void prepararBotao(JButton botao, Color fundo) {
        botao.setBackground(fundo);
        botao.setForeground(TEXTO);
        botao.setFont(new Font("Dialog", Font.BOLD, 12));
        botao.setFocusPainted(false);
    }
}
