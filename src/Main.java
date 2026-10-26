import gui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * MEMBER 3 - Entry point. Starts the GUI on the Swing event thread.
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to the default look and feel
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}