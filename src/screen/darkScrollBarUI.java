package screen;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class darkScrollBarUI extends BasicScrollBarUI {
    private static final Color TRACK_COLOR = new Color(17, 24, 39);
    private static final Color THUMB_COLOR = new Color(71, 85, 105);
    private static final Color THUMB_HOVER_COLOR = new Color(20, 184, 166);

    private darkScrollBarUI() {
    }

    public static void install(JScrollPane scrollPane) {
        scrollPane.setBackground(new Color(31, 41, 55));
        scrollPane.getViewport().setBackground(new Color(31, 41, 55));
        install(scrollPane.getVerticalScrollBar());
        install(scrollPane.getHorizontalScrollBar());
    }

    public static void install(JComboBox<?> comboBox) {
        comboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
                SwingUtilities.invokeLater(() -> installPopupScrollBar(comboBox));
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent event) {
            }
        });
    }

    private static void install(JScrollBar scrollBar) {
        if (scrollBar == null || scrollBar.getUI() instanceof darkScrollBarUI) {
            return;
        }
        scrollBar.setUI(new darkScrollBarUI());
        scrollBar.setOpaque(false);
    }

    private static void installPopupScrollBar(JComboBox<?> comboBox) {
        for (Window window : Window.getWindows()) {
            installPopupScrollBar(window, comboBox);
        }
    }

    private static void installPopupScrollBar(Container container, JComboBox<?> comboBox) {
        for (Component component : container.getComponents()) {
            if (component instanceof JList
                    && ((JList<?>) component).getModel() == comboBox.getModel()) {
                JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(
                        JScrollPane.class, component);
                if (scrollPane != null) {
                    install(scrollPane);
                }
            }
            if (component instanceof Container) {
                installPopupScrollBar((Container) component, comboBox);
            }
        }
    }

    @Override
    protected void paintTrack(Graphics graphics, JComponent component, Rectangle bounds) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(TRACK_COLOR);
        g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
        g2.dispose();
    }

    @Override
    protected void paintThumb(Graphics graphics, JComponent component, Rectangle bounds) {
        if (bounds.isEmpty() || scrollbar.getOrientation() == JScrollBar.VERTICAL && bounds.height < 4
                || scrollbar.getOrientation() == JScrollBar.HORIZONTAL && bounds.width < 4) {
            return;
        }

        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isThumbRollover() || isDragging ? THUMB_HOVER_COLOR : THUMB_COLOR);
        g2.fillRoundRect(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4, 8, 8);
        g2.dispose();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createInvisibleButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createInvisibleButton();
    }

    @Override
    protected Dimension getMinimumThumbSize() {
        return scrollbar.getOrientation() == JScrollBar.VERTICAL
                ? new Dimension(10, 24)
                : new Dimension(24, 10);
    }

    @Override
    public Dimension getPreferredSize(JComponent component) {
        return scrollbar.getOrientation() == JScrollBar.VERTICAL
                ? new Dimension(12, 0)
                : new Dimension(0, 12);
    }

    private JButton createInvisibleButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        button.setFocusable(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }

}
