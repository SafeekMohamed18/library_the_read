package lms;

import lms.ui.LibraryFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Font;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                configureLookAndFeel();
                Library library = new Library();
                SampleData.populate(library);
                new LibraryFrame(library).setVisible(true);
            } catch (RuntimeException ex) {
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
                javax.swing.JOptionPane.showMessageDialog(null,
                        "The library application could not start.\n" + ex.getMessage(),
                        "Library Management System", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void configureLookAndFeel() {
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Table.rowHeight", 29);
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (Exception ex) {
                    throw new IllegalStateException("Could not initialize the Swing look and feel.", ex);
                }
                break;
            }
        }
    }
}
