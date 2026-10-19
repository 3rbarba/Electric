import javax.swing.*;
import screen.mainScreen;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new mainScreen().run());
    }
}