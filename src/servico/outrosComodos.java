package servico;


import javax.swing.*;

public class outrosComodos {
    public static void calc(JComboBox comodos, JTextField largura, JTextField comprimento, JTextField altura, JTextField resultado) {
        String comodoSelecionado = (String) comodos.getSelectedItem();
        double larguraValor = Double.parseDouble(largura.getText());
        double comprimentoValor = Double.parseDouble(comprimento.getText());
        double alturaValor = Double.parseDouble(altura.getText());
        resultado.setText(String.format("%.2f"));
        switch(comodoSelecionado) {
            case "Sala":
                // Cálculo específico para sala
                break;
            case "Quarto":
                // Cálculo específico para quarto
                break;
            case "Cozinha":
                // Cálculo específico para cozinha
                break;
            case "Banheiro":
                // Cálculo específico para banheiro
                break;
            case "Outros":
                // Cálculo específico para outros comodos
                break;
            default:
                JOptionPane.showMessageDialog(null, "Comodo não reconhecido.");
                break;
        }

    }
}
