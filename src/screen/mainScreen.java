package screen;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import servico.Importar;
import static servico.comodos.*;
import static servico.demandaEquip.*;

public class mainScreen {
    private JPanel textDimPredial;
    private JPanel main;
    private JScrollPane scrollPaneComodos;
    private JComboBox<String> Comodos;
    private JButton Add;
    private JButton importarComodos;
    private JButton Remove;
    private JTextField area;
    private JTextField perimetro;
    private JLabel TextArea;
    private JLabel TextPer;
    private JTable comodosTable;
    private JButton ButtonCalc;
    private JTextField nomeComodo;
    private JLabel ComodoTXT;
    private JLabel NomeTXT;
    private JLabel resumoTomadas;
    private JLabel resumoIluminacao;
    private JLabel resumoGeral;

    private final DefaultTableModel tableModelComodos;
    private final ImageIcon icon;
    private Importar.ProjetoImportado projetoImportado;

    public mainScreen() {
        if (main == null) {
            criarInterfaceSemFormCompiler();
        }
        tableModelComodos = new DefaultTableModel(
                new Object[]{"Cômodo", "Área (m²)", "Perímetro (m)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        comodosTable.setModel(tableModelComodos);
        prepararTabelaComodos();
        prepararResumoComodos();
        area.setCaretColor(area.getForeground());
        perimetro.setCaretColor(perimetro.getForeground());
        nomeComodo.setCaretColor(nomeComodo.getForeground());
        DarkScrollBarUI.install(scrollPaneComodos);
        DarkScrollBarUI.install(Comodos);

        URL iconUrl = mainScreen.class.getResource("/icons/main.png");
        icon = iconUrl == null ? null : new ImageIcon(iconUrl);

        initListeners();
    }

    private void criarInterfaceSemFormCompiler() {
        java.awt.Color fundo = new java.awt.Color(17, 34, 42);
        java.awt.Color painel = new java.awt.Color(32, 53, 61);
        java.awt.Color texto = new java.awt.Color(238, 241, 243);
        java.awt.Color destaque = new java.awt.Color(17, 135, 143);

        main = new JPanel(new java.awt.BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(fundo);
        textDimPredial = new JPanel(new java.awt.BorderLayout(8, 10));
        textDimPredial.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new java.awt.Color(70, 91, 99)),
                "DIMENSIONAMENTO PREDIAL", 0, 0, null, texto));
        textDimPredial.setBackground(painel);

        Comodos = new JComboBox<>(new String[]{
                "Sala", "Quarto", "Cozinha", "Banheiro", "Lavanderia",
                "Área de Serviço", "Suíte", "Corredor", "Varanda", "Hall", "Outros"
        });
        nomeComodo = new JTextField(12);
        area = new JTextField(8);
        perimetro = new JTextField(8);
        Add = new JButton("Adicionar");
        importarComodos = new JButton("Importar TXT/CSV");
        Remove = new JButton("Remover");
        ButtonCalc = new JButton("Próximo");
        scrollPaneComodos = new JScrollPane();
        comodosTable = new JTable();
        resumoTomadas = new JLabel();
        resumoIluminacao = new JLabel();
        resumoGeral = new JLabel();
        ComodoTXT = new JLabel("CÔMODO:");
        NomeTXT = new JLabel("NOME (opcional):");
        TextArea = new JLabel("ÁREA (m²):");
        TextPer = new JLabel("PERÍMETRO (m):");

        JPanel campos = new JPanel(new java.awt.GridBagLayout());
        campos.setOpaque(false);
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.insets = new java.awt.Insets(3, 5, 3, 5);
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.gridy = 0;
        adicionarCampo(c, campos, 0, ComodoTXT);
        adicionarCampo(c, campos, 1, NomeTXT);
        adicionarCampo(c, campos, 2, TextArea);
        adicionarCampo(c, campos, 3, TextPer);
        c.gridy = 1;
        adicionarCampo(c, campos, 0, Comodos);
        adicionarCampo(c, campos, 1, nomeComodo);
        adicionarCampo(c, campos, 2, area);
        adicionarCampo(c, campos, 3, perimetro);
        c.gridx = 4;
        c.weightx = 0;
        campos.add(Add, c);
        c.gridx = 5;
        campos.add(importarComodos, c);
        textDimPredial.add(campos, java.awt.BorderLayout.NORTH);

        scrollPaneComodos.setViewportView(comodosTable);
        textDimPredial.add(scrollPaneComodos, java.awt.BorderLayout.CENTER);

        JPanel rodape = new JPanel(new java.awt.BorderLayout(8, 4));
        rodape.setOpaque(false);
        JPanel totais = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 14, 2));
        totais.setOpaque(false);
        totais.add(resumoTomadas);
        totais.add(resumoIluminacao);
        totais.add(resumoGeral);
        JPanel botoes = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 2));
        botoes.setOpaque(false);
        botoes.add(Remove);
        botoes.add(importarComodos);
        botoes.add(ButtonCalc);
        rodape.add(totais, java.awt.BorderLayout.CENTER);
        rodape.add(botoes, java.awt.BorderLayout.EAST);
        textDimPredial.add(rodape, java.awt.BorderLayout.SOUTH);
        main.add(textDimPredial, java.awt.BorderLayout.CENTER);

        for (JLabel label : new JLabel[]{ComodoTXT, NomeTXT, TextArea, TextPer,
                resumoTomadas, resumoIluminacao, resumoGeral}) {
            label.setForeground(texto);
        }
        for (JButton button : new JButton[]{Add, ButtonCalc, importarComodos}) {
            button.setBackground(destaque);
            button.setForeground(texto);
            button.setFont(button.getFont().deriveFont(java.awt.Font.BOLD, 13f));
            button.setFocusPainted(false);
        }
        Remove.setBackground(new java.awt.Color(91, 101, 105));
        Remove.setForeground(texto);
        Remove.setFont(Remove.getFont().deriveFont(java.awt.Font.BOLD, 13f));
        Remove.setFocusPainted(false);
        for (JComponent component : new JComponent[]{Comodos, nomeComodo, area, perimetro}) {
            component.setBackground(fundo);
            component.setForeground(texto);
        }
        scrollPaneComodos.setBackground(painel);
        comodosTable.setBackground(painel);
        comodosTable.setForeground(texto);
    }

    private static void adicionarCampo(java.awt.GridBagConstraints c, JPanel painel,
                                      int coluna, java.awt.Component componente) {
        c.gridx = coluna;
        c.weightx = componente instanceof JTextField || componente instanceof JComboBox ? 1 : 0;
        painel.add(componente, c);
    }

    private void importarComodosCsv() {
        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle("Importar memorial TXT ou cômodos CSV");
        seletor.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Memorial ou planilha (*.txt, *.csv)", "txt", "csv"));
        if (seletor.showOpenDialog(main) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            Importar.ProjetoImportado importado = Importar.lerProjeto(
                    seletor.getSelectedFile().toPath(), obterTiposValidos());
            if (importado.getComodos().isEmpty()) {
                throw new IllegalArgumentException("O arquivo não contém cômodos para importar.");
            }
            if (importado.isMemorial()) {
                limparComodos();
            }
            for (Importar.ComodoImportado comodo : importado.getComodos()) {
                adicionarComodoImportado(comodo);
            }
            projetoImportado = importado.isMemorial() ? importado : null;
            ButtonCalc.setEnabled(true);
            atualizarResumoComodos();
            String mensagem = importado.isMemorial()
                    ? importado.getComodos().size() + " cômodo(s) e "
                    + importado.getEquipamentos().size() + " equipamento(s) importados."
                    : importado.getComodos().size() + " cômodo(s) importados.";
            if (importado.isFormatoAntigo()) {
                mensagem += "\nMemorial antigo: FCA, FCT e método por circuito não estavam salvos; "
                        + "serão usados os valores padrão.";
            }
            JOptionPane.showMessageDialog(main, mensagem,
                    "Importação concluída", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(main, ex.getMessage(),
                    "Não foi possível importar os cômodos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparComodos() {
        tableModelComodos.setRowCount(0);
        comodoArea.clear();
        comodoPerimetro.clear();
        comodoTipo.clear();
        comodoTMDVA.clear();
        comodoILUVA.clear();
    }

    private List<String> obterTiposValidos() {
        List<String> tipos = new ArrayList<>();
        for (int i = 0; i < Comodos.getItemCount(); i++) {
            tipos.add(Comodos.getItemAt(i));
        }
        return tipos;
    }

    private void adicionarComodoImportado(Importar.ComodoImportado importado) {
        String nomeBase = importado.getNome().isEmpty() ? importado.getTipo() : importado.getNome();
        if (!normalizar(nomeBase).startsWith(normalizar(importado.getTipo()))) {
            nomeBase = importado.getTipo() + " " + nomeBase;
        }
        String nomeFinal = nomeBase;
        Set<String> nomesExistentes = new HashSet<>(comodoArea.keySet());
        int numero = 1;
        while (nomesExistentes.contains(nomeFinal)) {
            nomeFinal = nomeBase + " " + numero++;
        }

        comodoPerimetro.put(nomeFinal, importado.getPerimetro());
        comodoArea.put(nomeFinal, importado.getArea());
        comodoTipo.put(nomeFinal, importado.getTipo());
        comodoTMDVA.remove(nomeFinal);
        comodoILUVA.remove(nomeFinal);
        tableModelComodos.addRow(new Object[]{nomeFinal, importado.getArea(), importado.getPerimetro()});
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]", "");
    }

    private void prepararTabelaComodos() {
        comodosTable.setBackground(new java.awt.Color(32, 53, 61));
        comodosTable.setForeground(new java.awt.Color(238, 241, 243));
        comodosTable.setGridColor(new java.awt.Color(70, 91, 99));
        comodosTable.setSelectionBackground(new java.awt.Color(17, 135, 143));
        comodosTable.setSelectionForeground(new java.awt.Color(238, 241, 243));
        comodosTable.setFillsViewportHeight(true);
        comodosTable.getTableHeader().setBackground(new java.awt.Color(17, 135, 143));
        comodosTable.getTableHeader().setForeground(new java.awt.Color(238, 241, 243));
        comodosTable.getTableHeader().setReorderingAllowed(false);
        scrollPaneComodos.setViewportView(comodosTable);
    }

    public void run() {
        JFrame frame = new JFrame("Dimensionamento Elétrico");
        frame.setContentPane(main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 550);
        frame.setLocationRelativeTo(null);
        frame.setIconImage(icon.getImage());
        frame.setVisible(true);

    }

    private void initListeners() {
        // Botão de próximo
        if (ButtonCalc != null) {
            ButtonCalc.addActionListener(e -> {
                if (!calcComodos(tableModelComodos)) {
                    return;
                }
                atualizarResumoComodos();
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(main);
                equipamentosScreen eqScreen = new equipamentosScreen(frame, main, projetoImportado);
                int potTotal = (int) PotenciaTugIlu();
                double potCorr = potTotal * demandaTab6(potTotal);
                itemText("Tomada e Iluminação", 1, potTotal, (int) potCorr, eqScreen.modeloEquipamentos);

                if (main != null) {
                    frame.setContentPane(eqScreen.getMain());
                    frame.revalidate();
                    frame.repaint();
                } else {
                    JOptionPane.showMessageDialog(null, "Erro: O painel principal não foi inicializado.");
                }
            });
        }

        // Ação do Botão Adicionar
        if (Add != null) {
            Add.addActionListener(e -> {
                String tipoSelecionado = (String) Comodos.getSelectedItem();
                String nomePersonalizado = nomeComodo.getText().trim();
                String comodoSelecionado = nomePersonalizado.isEmpty()
                        ? tipoSelecionado
                        : tipoSelecionado + " " + nomePersonalizado;
                java.lang.String areaText = area.getText().replace(",", ".");
                java.lang.String perimetroText = perimetro.getText().replace(",", ".");

                if (areaText.isEmpty()) {
                    areaText = "0";
                }
                if (perimetroText.isEmpty()) {
                    perimetroText = "0";
                }

                try {
                    double areaValue = Double.parseDouble(areaText);
                    double perimetroValue = Double.parseDouble(perimetroText);

                    if (!Double.isFinite(areaValue) || !Double.isFinite(perimetroValue)
                            || areaValue < 0 || perimetroValue < 0) {
                        JOptionPane.showMessageDialog(null, "Adicione apenas números positivos.");
                        return;
                    }

                    if (comodoPerimetro.containsKey(comodoSelecionado)) {
                        int numeroComodo = 0;
                        String novoNomeComodo;
                        do {
                            numeroComodo++;
                            novoNomeComodo = comodoSelecionado + " " + numeroComodo;
                        } while (comodoPerimetro.containsKey(novoNomeComodo));
                        comodoSelecionado = novoNomeComodo;
                    }

                    comodoPerimetro.put(comodoSelecionado, perimetroValue);
                    comodoArea.put(comodoSelecionado, areaValue);
                    comodoTipo.put(comodoSelecionado, tipoSelecionado);
                    comodoTMDVA.remove(comodoSelecionado);
                    comodoILUVA.remove(comodoSelecionado);
                    tableModelComodos.addRow(new Object[]{comodoSelecionado, areaValue, perimetroValue});
                    atualizarResumoComodos();

                    ButtonCalc.setEnabled(true);
                    area.setText("");
                    perimetro.setText("");
                    nomeComodo.setText("");
                    area.requestFocus();

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Digite um número válido.");
                }
            });
        }
        if (importarComodos != null) {
            importarComodos.addActionListener(e -> importarComodosCsv());
        }
        // Botão remover
        if (Remove != null) {
            Remove.addActionListener(e -> {
                int[] selectedIndices = comodosTable.getSelectedRows();

                if (selectedIndices.length > 0) {
                    for (int i = selectedIndices.length - 1; i >= 0; i--) {
                        int indexToRemove = selectedIndices[i];
                        String comodoSelecionado = (String) tableModelComodos.getValueAt(indexToRemove, 0);
                        comodoPerimetro.remove(comodoSelecionado);
                        comodoArea.remove(comodoSelecionado);
                        comodoTipo.remove(comodoSelecionado);
                        comodoTMDVA.remove(comodoSelecionado);
                        comodoILUVA.remove(comodoSelecionado);
                        tableModelComodos.removeRow(indexToRemove);
                    }
                    if (tableModelComodos.getRowCount() <= 0) {
                        ButtonCalc.setEnabled(false);
                    }
                    atualizarResumoComodos();

                } else {
                    JOptionPane.showMessageDialog(main, "Selecione ao menos um item para remover.");
                }
            });
        }
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_ENTER) {
                    Add.doClick();
                    e.consume();
                }
                if (keyCode == KeyEvent.VK_DELETE) {
                    Remove.doClick();
                    e.consume();
                }
            };
        };
        area.addKeyListener(enterListener);
        perimetro.addKeyListener(enterListener);
        nomeComodo.addKeyListener(enterListener);
        comodosTable.addKeyListener(enterListener);
        Comodos.addKeyListener(enterListener);
    }

    private void prepararResumoComodos() {
        for (JLabel label : new JLabel[]{resumoTomadas, resumoIluminacao, resumoGeral}) {
            label.setForeground(new java.awt.Color(238, 241, 243));
            label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        }
        atualizarResumoComodos();
    }

    private void atualizarResumoComodos() {
        if (tableModelComodos.getRowCount() == 0) {
            comodoTMDVA.clear();
            comodoILUVA.clear();
        } else if (!calcComodos(tableModelComodos)) {
            return;
        }
        resumoTomadas.setText(String.format(Locale.forLanguageTag("pt-BR"),
                "Total de tomadas: %d VA", somarTomadas()));
        resumoIluminacao.setText(String.format(Locale.forLanguageTag("pt-BR"),
                "Total de iluminação: %d VA", somarIluminacao()));
        resumoGeral.setText(String.format(Locale.forLanguageTag("pt-BR"),
                "Total geral: %d VA", somarTomadas() + somarIluminacao()));
    }
}