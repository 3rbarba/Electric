import javax.swing.*;
import screen.mainScreen;

public class Main {
    public static void main(String[] args) {
        mainScreen mainScreen = new mainScreen();
        SwingUtilities.invokeLater(mainScreen::run);
    }
}