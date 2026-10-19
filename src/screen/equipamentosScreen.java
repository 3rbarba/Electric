package screen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;
import servico.QuadroCargasService;
import static servico.demandaEquip.calcEquip;
import static servico.demandaEquip.removerEquipamentosSelecionados;

public class equipamentosScreen {
    private JPanel main;
    private JPanel panelForm;
    private JScrollPane scrollPaneEquipamentos;
    private JComboBox<String> comboTipoEquipamento;
    private JTextField txtNomeEquipamento;
    private JTextField txtPotenciaTotal;
    private JButton btnAdicionarEquipamento;
    private JButton btnAnterior;
    private JButton btnRemoverEquipamento;
    private JButton btnProximo;
    private JTable tabelaEquipamentos;
    private JLabel PotenciaTXT;
    private JLabel NomeTXT;
    public final DefaultTableModel modeloEquipamentos;
    private final JFrame framePrincipal;
    private final JPanel painelAnterior;
    private final Map<String, QuadroCargasService.ParametrosCircuito> parametrosPorCircuito = new HashMap<>();

    public equipamentosScreen(JFrame frame, JPanel painelAnterior) {
        this.framePrincipal = frame;
        this.painelAnterior = painelAnterior;
        if (main == null) {
            criarInterfaceSemFormCompiler();
        }

        modeloEquipamentos = new DefaultTableModel(
                new Object[]{"Equipamento / subtotal", "Potência instalada / corrigida (W/VA)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaEquipamentos.setModel(modeloEquipamentos);
        prepararTabelaEquipamentos();
        txtNomeEquipamento.setCaretColor(txtNomeEquipamento.getForeground());
        txtPotenciaTotal.setCaretColor(txtPotenciaTotal.getForeground());
        atualizarRotuloPotencia();
        comboTipoEquipamento.addActionListener(e -> atualizarRotuloPotencia());
        DarkScrollBarUI.install(scrollPaneEquipamentos);
        DarkScrollBarUI.install(comboTipoEquipamento);
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
        panelForm = new JPanel(new java.awt.GridBagLayout());
        panelForm.setBackground(painel);
        panelForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new java.awt.Color(70, 91, 99)),
                "EQUIPAMENTOS ESPECIAIS", 0, 0, null, texto));

        comboTipoEquipamento = new JComboBox<>(new String[]{
                "Chuveiros(tab7)", "Aquecedores de agua(tab7)", "Ferro elétrico(tab7)",
                "Aquecedor central(tab8)", "Secadora de roupa(tab9)", "Forno elétrico(tab9)",
                "Lava roupas(tab9)", "Lava louças(tab9)", "Micro-ondas(tab9)",
                "Fogões elétricos(tab10)", "Ar-condicionado(tab11)", "Bombas(tab16)",
                "Hidromassagem(tab16)"
        });
        txtNomeEquipamento = new JTextField(12);
        txtPotenciaTotal = new JTextField(8);
        btnAdicionarEquipamento = new JButton("Adicionar");
        btnAnterior = new JButton("Anterior");
        btnRemoverEquipamento = new JButton("Remover selecionado");
        btnProximo = new JButton("Próximo");
        PotenciaTXT = new JLabel("Potência (W):");
        NomeTXT = new JLabel("Nome (opcional):");
        tabelaEquipamentos = new JTable();
        scrollPaneEquipamentos = new JScrollPane(tabelaEquipamentos);

        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.insets = new java.awt.Insets(4, 6, 4, 6);
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.gridy = 0;
        c.gridx = 0;
        panelForm.add(new JLabel("Equipamentos:"), c);
        c.gridx = 1;
        panelForm.add(NomeTXT, c);
        c.gridx = 2;
        panelForm.add(PotenciaTXT, c);
        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 2;
        panelForm.add(comboTipoEquipamento, c);
        c.gridx = 1;
        c.weightx = 1;
        panelForm.add(txtNomeEquipamento, c);
        c.gridx = 2;
        panelForm.add(txtPotenciaTotal, c);
        c.gridx = 3;
        c.weightx = 0;
        panelForm.add(btnAdicionarEquipamento, c);

        JPanel botoes = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 2));
        botoes.setOpaque(false);
        botoes.add(btnAnterior);
        botoes.add(btnRemoverEquipamento);
        botoes.add(btnProximo);
        main.add(panelForm, java.awt.BorderLayout.NORTH);
        main.add(scrollPaneEquipamentos, java.awt.BorderLayout.CENTER);
        main.add(botoes, java.awt.BorderLayout.SOUTH);

        for (JLabel label : new JLabel[]{PotenciaTXT, NomeTXT}) {
            label.setForeground(texto);
        }
        btnAdicionarEquipamento.setBackground(destaque);
        btnAdicionarEquipamento.setForeground(texto);
        btnProximo.setBackground(destaque);
        btnAnterior.setBackground(new java.awt.Color(91, 101, 105));
        btnRemoverEquipamento.setBackground(new java.awt.Color(91, 101, 105));
        for (JButton button : new JButton[]{btnAdicionarEquipamento, btnAnterior,
                btnRemoverEquipamento, btnProximo}) {
            button.setForeground(texto);
            button.setFont(button.getFont().deriveFont(java.awt.Font.BOLD, 13f));
            button.setFocusPainted(false);
        }
        for (JComponent component : new JComponent[]{comboTipoEquipamento,
                txtNomeEquipamento, txtPotenciaTotal}) {
            component.setBackground(fundo);
            component.setForeground(texto);
        }
        scrollPaneEquipamentos.setBackground(painel);
    }
    public JPanel getMain() {
        return main;
    }

    private void prepararTabelaEquipamentos() {
        tabelaEquipamentos.setBackground(new java.awt.Color(32, 53, 61));
        tabelaEquipamentos.setForeground(new java.awt.Color(238, 241, 243));
        tabelaEquipamentos.setGridColor(new java.awt.Color(70, 91, 99));
        tabelaEquipamentos.setSelectionBackground(new java.awt.Color(17, 135, 143));
        tabelaEquipamentos.setSelectionForeground(new java.awt.Color(238, 241, 243));
        tabelaEquipamentos.setFillsViewportHeight(true);
        tabelaEquipamentos.getTableHeader().setBackground(new java.awt.Color(17, 135, 143));
        tabelaEquipamentos.getTableHeader().setForeground(new java.awt.Color(238, 241, 243));
        tabelaEquipamentos.getTableHeader().setReorderingAllowed(false);
        tabelaEquipamentos.getColumnModel().getColumn(0).setPreferredWidth(430);
        tabelaEquipamentos.getColumnModel().getColumn(1).setPreferredWidth(240);
        javax.swing.table.DefaultTableCellRenderer renderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                                    boolean focused, int row, int column) {
                java.awt.Component component = super.getTableCellRendererComponent(
                        table, value, selected, focused, row, column);
                Object descricao = table.getValueAt(row, 0);
                boolean resumo = descricao != null && (descricao.toString().startsWith("Subtotal")
                        || descricao.toString().startsWith("TOTAL"));
                if (!selected) {
                    component.setBackground(resumo ? new java.awt.Color(17, 76, 82)
                            : new java.awt.Color(32, 53, 61));
                    component.setForeground(new java.awt.Color(238, 241, 243));
                }
                component.setFont(component.getFont().deriveFont(resumo
                        ? java.awt.Font.BOLD : java.awt.Font.PLAIN));
                return component;
            }
        };
        for (int column = 0; column < tabelaEquipamentos.getColumnCount(); column++) {
            tabelaEquipamentos.getColumnModel().getColumn(column).setCellRenderer(renderer);
        }
        scrollPaneEquipamentos.setViewportView(tabelaEquipamentos);
    }

    private void atualizarRotuloPotencia() {
        boolean arCondicionado = comboTipoEquipamento.getSelectedItem() != null
                && comboTipoEquipamento.getSelectedItem().toString().endsWith("(tab11)");
        PotenciaTXT.setText(arCondicionado ? "Capacidade (BTU):" : "Potência (W):");
    }

    private void initListeners() {
        btnAdicionarEquipamento.addActionListener(e -> {
            String potStr = txtPotenciaTotal.getText().replace(",", ".");

            calcEquip(comboTipoEquipamento, modeloEquipamentos, txtNomeEquipamento, txtPotenciaTotal, potStr);
        });

        btnAnterior.addActionListener(e -> {
            framePrincipal.setContentPane(painelAnterior);
            framePrincipal.revalidate();
            framePrincipal.repaint();
        });

        btnRemoverEquipamento.addActionListener(e ->
                removerEquipamentosSelecionados(tabelaEquipamentos, modeloEquipamentos));

        btnProximo.addActionListener(e -> {
            if (modeloEquipamentos.getRowCount() == 0) {
                JOptionPane.showMessageDialog(main, "Adicione ao menos um equipamento antes de continuar.");
                return;
            }
            circuitosScreen proximaTela = new circuitosScreen(
                    framePrincipal, main, modeloEquipamentos, parametrosPorCircuito);
            framePrincipal.setContentPane(proximaTela.getMain());
            framePrincipal.revalidate();
            framePrincipal.repaint();
        });
    }
}