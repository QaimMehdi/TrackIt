package com.trackit;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.trackit.ui.MainWindow;

import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        // Set up modern look and feel
        FlatRobotoFont.install();
        FlatLightLaf.setup();
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        
        // Enable window decorations
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // Custom UI tweaks
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.trackInsets", new Insets(2, 4, 2, 4));
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("TitlePane.centerTitle", true);

        // Set application icon
        try {
            Image icon = ImageIO.read(Main.class.getResourceAsStream("/trackit.png"));
            if (icon != null) {
                UIManager.put("Frame.icon", new ImageIcon(icon));
            }
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }

        // Launch application
        SwingUtilities.invokeLater(() -> {
            try {
                MainWindow mainWindow = new MainWindow();
                mainWindow.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error starting application: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
} 