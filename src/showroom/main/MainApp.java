package showroom.main;

import showroom.gui.ShowroomGUI;

import javax.swing.SwingUtilities;

/**
 * Entry point for the Car Showroom Inventory Management System.
 */
public class MainApp {
    public static void main(String[] args) {
        // Use SwingUtilities.invokeLater for thread safety
        SwingUtilities.invokeLater(() -> new ShowroomGUI());
    }
}