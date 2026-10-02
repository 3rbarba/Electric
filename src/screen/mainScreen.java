package screen;

import javax.swing.*;

import static servico.outrosComodos.calc;

public class mainScreen {
    private JPanel textDimPredial;
    private JPanel main;
    private JComboBox Comodos;
    private JButton Add;
    private JTextField area;
    private JTextField perimetro;
    private JLabel TextArea;
    private JLabel TextPer;


    private void createUIComponents() {
        textDimPredial = new JPanel();
        main = new JPanel();
        Add = new JButton();
        Comodos = new JComboBox();
        Add = new JButton("Adicionar");
        area = new JTextField();
        perimetro = new JTextField();
    }
    ImageIcon icon = new ImageIcon("src/icons/main.png");
    ImageIcon icon1;
    ImageIcon icon2;

    public void run() {
    JFrame frame = new JFrame("Dimensionamento Elétrico");
    frame.setContentPane(this.main);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
    frame.setLocationRelativeTo(null);
    frame.setIconImage(icon.getImage());
    botao();
    }

    public void botao() {
        Add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                calc(Comodos, area, perimetro, null, null);
            }
        });
    }
}