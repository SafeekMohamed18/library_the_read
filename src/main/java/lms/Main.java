package lms;

import lms.repository.CsvLibraryStore;
import lms.service.LibraryService;
import lms.ui.LmsFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Path;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                configureDefaults();
                Path dataDirectory = Path.of(System.getProperty("user.home"), ".the-read-library");
                LibraryService service = new LibraryService(new CsvLibraryStore(dataDirectory));
                new LmsFrame(service).setVisible(true);
            } catch (IOException | RuntimeException ex) {
                UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
                javax.swing.JOptionPane.showMessageDialog(null,
                        "The library data could not be loaded.\n" + ex.getMessage(),
                        "The Read - Startup Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void configureDefaults() {
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Table.rowHeight", 30);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
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
