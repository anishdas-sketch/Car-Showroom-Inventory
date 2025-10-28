package showroom.gui;

import showroom.model.*;
import showroom.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main application window and user interface.
 * Implements a *manual* dark theme with RED buttons (BLACK text).
 * JFileChooser is now configured with specific text color overrides.
 */
public class ShowroomGUI extends JFrame {
    private InventoryService service;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContainer;

    // --- UI Constants ---
    private static final Color PRIMARY_BG = new Color(30, 30, 30); // Dark Gray BG
    // Final: Red button color with Black text as requested
    private static final Color BUTTON_COLOR = new Color(220, 53, 69); // Strong Red
    private static final Color BUTTON_TEXT_COLOR = Color.BLACK; // Black text

    private static final Color TEXT_COLOR = new Color(240, 240, 240); // Off-White Text for application labels etc.
    private static final Color ACCENT_COLOR = new Color(255, 193, 7); // Amber/Gold Accent
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 32);

    public ShowroomGUI() {
        service = new InventoryService();
        setTitle("Car Showroom Inventory & Sales Management");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        mainContainer = new JPanel(cardLayout);
        add(mainContainer);

        setupLookAndFeel();

        mainContainer.add(createMainMenuPanel(), "Main");
        mainContainer.add(createAddUpdatePanel(true), "Add");
        mainContainer.add(createFilterInventoryPanel(), "Filter");
        mainContainer.add(createSalesLogPanel(), "SalesLog");
        mainContainer.add(createReportsPanel(), "Reports");

        showMainMenu();
        setVisible(true);
    }

    // --- UI Setup & Helpers ---

    private void setupLookAndFeel() {
        try {
            // Use the OS's native look and feel.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set System Look and Feel.");
        }

        // --- Global Settings (Minimal, mostly for dark theme elements) ---
        UIManager.put("TextField.background", PRIMARY_BG.brighter());
        UIManager.put("TextField.foreground", TEXT_COLOR);
        UIManager.put("TextField.caretForeground", TEXT_COLOR); // Cursor color
        UIManager.put("ComboBox.background", PRIMARY_BG.brighter());
        UIManager.put("ComboBox.foreground", TEXT_COLOR); // Text inside combobox

        // Set background for JOptionPane dialogs (Uses custom panel now)
        UIManager.put("Panel.background", PRIMARY_BG);
        UIManager.put("OptionPane.background", PRIMARY_BG);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);


        // --- JFileChooser specific overrides ---
        // As requested: "File name:" and "Files of type:" labels to white
        UIManager.put("FileChooser.fileNameLabel.foreground", Color.WHITE);
        UIManager.put("FileChooser.filesOfTypeLabel.foreground", Color.WHITE);
        
        // As requested: Text inside the "Files of type" dropdown (e.g., "All Files", "Image Files") to BLACK
        UIManager.put("ComboBox.selectionForeground", Color.BLACK); // When an item is selected in the dropdown
        UIManager.put("ComboBox.foreground", Color.BLACK); // Default text color for items in dropdown
        // The popup list itself:
        UIManager.put("ComboBox.listBackground", Color.WHITE);
        UIManager.put("ComboBox.listForeground", Color.BLACK); // Text for the list items
        
        // Ensure the JFileChooser's main background remains native (often white/light gray)
        // UIManager.put("FileChooser.background", Color.WHITE); // This might force it if native isn't white
        // UIManager.put("FileChooser.viewButtonPanel.background", Color.WHITE);
        // UIManager.put("FileChooser.listViewBackground", Color.WHITE);
        // UIManager.put("FileChooser.detailsViewBackground", Color.WHITE);
        // UIManager.put("FileChooser.readOnly", Boolean.TRUE); // Example of another setting

        // Set the frame's content pane background
        getContentPane().setBackground(PRIMARY_BG);
    }

    /**
     * Creates a button with the app's custom RED theme and BLACK text.
     */
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BUTTON_COLOR); // RED
        btn.setForeground(BUTTON_TEXT_COLOR); // BLACK
        btn.setFont(BUTTON_FONT);
        btn.setFocusPainted(false);
        btn.setOpaque(true); // Ensure background color shows
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_COLOR.darker(), 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        return btn;
    }

    private JButton createMenuButton(String text) {
        JButton btn = createStyledButton(text); // Uses RED button, BLACK text
        btn.setPreferredSize(new Dimension(280, 60));
        return btn;
    }

    private JPanel createHeaderPanel(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_BG.darker()); // Manual Style
        header.setBorder(new EmptyBorder(15, 15, 15, 15));

        JButton backBtn = new JButton("<- Main Menu");
        // Style the back button manually (different style)
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        backBtn.setBackground(PRIMARY_BG.darker());
        backBtn.setForeground(ACCENT_COLOR); // Gold accent
        backBtn.setBorder(BorderFactory.createEmptyBorder());
        backBtn.setOpaque(false); // Make transparent
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        // Add hover effect
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backBtn.setForeground(ACCENT_COLOR.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backBtn.setForeground(ACCENT_COLOR);
            }
        });
        backBtn.addActionListener(e -> showMainMenu());

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR); // Manual Style

        header.add(backBtn, BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);

        return header;
    }

    private void showMainMenu() {
        cardLayout.show(mainContainer, "Main");
    }

    private Component getComponentByName(Container parent, String name) {
        for (Component comp : parent.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(name)) {
                return comp;
            }
        }
        return null;
    }

    // --- Main Menu Screen ---

    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_BG); // Manual Style

        JLabel title = new JLabel("Showroom Inventory Management", JLabel.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_COLOR); // Manual Style
        title.setBorder(new EmptyBorder(50, 0, 50, 0));

        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 30, 30));
        buttonPanel.setBackground(PRIMARY_BG); // Manual Style
        buttonPanel.setBorder(new EmptyBorder(50, 100, 50, 100));

        // Create buttons using our manual RED styler
        JButton addModelBtn = createMenuButton("Add New Model");
        addModelBtn.addActionListener(e -> {
            Component addPanel = getComponentByName(mainContainer, "Add");
            if (addPanel != null) mainContainer.remove(addPanel);
            mainContainer.add(createAddUpdatePanel(true), "Add");
            cardLayout.show(mainContainer, "Add");
        });

        JButton updateModelBtn = createMenuButton("Update Model");
        updateModelBtn.addActionListener(e -> showSelectionPanel("Update"));

        JButton removeModelBtn = createMenuButton("Remove Model");
        removeModelBtn.addActionListener(e -> showSelectionPanel("Remove"));

        JButton viewModelBtn = createMenuButton("View Inventory & Search");
        viewModelBtn.addActionListener(e -> {
            Component filterPanel = getComponentByName(mainContainer, "Filter");
            if (filterPanel != null) mainContainer.remove(filterPanel);
            mainContainer.add(createFilterInventoryPanel(), "Filter");
            cardLayout.show(mainContainer, "Filter");
        });

        JButton sellCarBtn = createMenuButton("Sell Car");
        sellCarBtn.addActionListener(e -> showSelectionPanel("Sell"));

        JButton reportsBtn = createMenuButton("Sales & Reports");
        reportsBtn.addActionListener(e -> {
            Component reportsPanel = getComponentByName(mainContainer, "Reports");
            if (reportsPanel != null) mainContainer.remove(reportsPanel);
            mainContainer.add(createReportsPanel(), "Reports");
            cardLayout.show(mainContainer, "Reports");
        });

        buttonPanel.add(addModelBtn);
        buttonPanel.add(updateModelBtn);
        buttonPanel.add(removeModelBtn);
        buttonPanel.add(viewModelBtn);
        buttonPanel.add(sellCarBtn);
        buttonPanel.add(reportsBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);

        return panel;
    }

    // --- Dynamic Selection Panel (for Update, Remove, Sell) ---

    private void showSelectionPanel(String operation) {
        Component existingPanel = getComponentByName(mainContainer, "Selection");
        if (existingPanel != null) mainContainer.remove(existingPanel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("Selection");
        panel.setBackground(PRIMARY_BG); // Manual Style

        panel.add(createHeaderPanel("Select Model to " + operation), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(4, 1, 0, 20));
        content.setBackground(PRIMARY_BG); // Manual Style
        content.setBorder(new EmptyBorder(50, 100, 50, 100));

        JLabel brandLabel = new JLabel("Select Brand:");
        brandLabel.setForeground(TEXT_COLOR); // Manual Style

        JComboBox<String> brandCombo = new JComboBox<>(service.getAllBrands().toArray(new String[0]));
        // ComboBox styling is handled by UIManager

        JLabel modelLabel = new JLabel("Select Model:");
        modelLabel.setForeground(TEXT_COLOR); // Manual Style

        JComboBox<String> modelCombo = new JComboBox<>();
        // ComboBox styling is handled by UIManager

        JButton proceedBtn = createStyledButton("Proceed to " + operation); // Manual Style (RED, Black Text)

        updateModelCombo(brandCombo, modelCombo);

        brandCombo.addActionListener(e -> updateModelCombo(brandCombo, modelCombo));

        proceedBtn.addActionListener(e -> {
            String brand = (String) brandCombo.getSelectedItem();
            String model = (String) modelCombo.getSelectedItem();

            if (brand == null || model == null || brand.isEmpty() || model.isEmpty()) {
                showErrorDialog("Please select a Brand and Model.");
                return;
            }

            CarModel car = service.getCarModel(brand, model);
            if (car == null) {
                showErrorDialog("Model not found in inventory.");
                return;
            }

            switch (operation) {
                case "Update":
                    showUpdateScreen(car);
                    break;
                case "Remove":
                    showRemoveScreen(car);
                    break;
                case "Sell":
                    showSellScreen(car);
                    break;
            }
        });

        content.add(brandLabel);
        content.add(brandCombo);
        content.add(modelLabel);
        content.add(modelCombo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(PRIMARY_BG); // Manual Style
        btnPanel.add(proceedBtn);

        panel.add(content, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        mainContainer.add(panel, "Selection");
        cardLayout.show(mainContainer, "Selection");
    }

    private void updateModelCombo(JComboBox<String> brandCombo, JComboBox<String> modelCombo) {
        modelCombo.removeAllItems();
        String selectedBrand = (String) brandCombo.getSelectedItem();
        if (selectedBrand != null) {
            List<CarModel> models = service.getModelsByBrand(selectedBrand);
            for (CarModel model : models) {
                modelCombo.addItem(model.getModel());
            }
        }
    }

    // --- Add/Update Screen (JFileChooser RE-ADDED with PREVIEW) ---

    private JPanel createAddUpdatePanel(boolean isAdd) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("AddUpdate");
        panel.setBackground(PRIMARY_BG); // Manual Style

        JPanel headerPanel = createHeaderPanel((isAdd ? "Add New" : "Update Existing") + " Model");
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new SpringLayout());
        formPanel.setBackground(PRIMARY_BG); // Manual Style
        formPanel.setBorder(new EmptyBorder(40, 100, 40, 100));

        JTextField brandField = new JTextField(20);
        JTextField modelField = new JTextField(20);
        JTextField priceField = new JTextField(20);
        JTextField quantityField = new JTextField(20);
        JTextField imagePathField = new JTextField(20);

        // Text field styling is handled by UIManager

        // --- Browse Button RE-ADDED ---
        JButton fileChooserBtn = createStyledButton("Browse..."); // Manual Style (RED, Black Text)
        fileChooserBtn.setFont(new Font("SansSerif", Font.PLAIN, 14)); // Override font

        fileChooserBtn.addActionListener(e -> {
            // Create a NEW JFileChooser. It will use the System Look and Feel
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png"));

            // --- NEW: Add Image Preview Accessory ---
            ImagePreviewAccessory preview = new ImagePreviewAccessory();
            fileChooser.setAccessory(preview);
            fileChooser.addPropertyChangeListener(preview);
            // --- End New Feature ---

            // --- NEW: Force Details View & Sort by Date ---
            try {
                // Find the "Details" view button and click it
                Action detailsAction = fileChooser.getActionMap().get("viewTypeDetails");
                if (detailsAction != null) {
                    detailsAction.actionPerformed(null);
                }
                // Programmatically sort by "Date Modified"
                sortFileChooserByDate(fileChooser);
            } catch (Exception ex) {
                System.err.println("Could not force JFileChooser details/sort: " + ex.getMessage());
            }
            // --- End New Feature ---

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                imagePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        // Labels and Fields setup (3 columns: Label, Field, Button)
        String[] labels = {"Brand:", "Model:", "Price (Rs):", "Quantity:", "Image Path (URL or File):"};
        JComponent[] fields = {brandField, modelField, priceField, quantityField, imagePathField};

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i], JLabel.RIGHT);
            label.setForeground(TEXT_COLOR); // Manual Style
            formPanel.add(label);
            formPanel.add(fields[i]);

            if (i == labels.length - 1) {
                formPanel.add(fileChooserBtn);
            } else {
                formPanel.add(new JLabel()); // Placeholder
            }
        }

        JButton actionBtn = createStyledButton(isAdd ? "Add New Model" : "Save Updates"); // Manual Style (RED, Black Text)

        actionBtn.addActionListener(e -> {
            if (!validateInput(brandField, modelField, priceField, quantityField)) return;

            String brand = brandField.getText();
            String model = modelField.getText();
            double price = Double.parseDouble(priceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            String imagePath = imagePathField.getText();

            if (isAdd) {
                if (service.getCarModel(brand, model) != null) {
                    showErrorDialog("Model already exists. Use Update instead.");
                    return;
                }

                String localPath = imagePath.isEmpty() ? "" : service.storeImageLocally(imagePath, brand, model);
                if (!imagePath.isEmpty() && localPath == null) {
                    showWarningDialog("Could not save image. Please check the URL/Path. Model added without image.");
                }

                CarModel newCar = new CarModel(brand, model, price, quantity, localPath != null ? localPath : "");
                service.addCarModel(newCar);
                showInfoDialog("New Model Added Successfully!");
            } else {
                CarModel carToUpdate = (CarModel) formPanel.getClientProperty("CarModel");
                service.updateCarModel(carToUpdate, brand, model, price, quantity, imagePath);
                showInfoDialog("Model Updated Successfully!");
            }

            clearFields(brandField, modelField, priceField, quantityField, imagePathField);
            showMainMenu();
        });

        formPanel.add(new JLabel()); // Spacer
        formPanel.add(actionBtn);
        formPanel.add(new JLabel()); // Spacer

        // Apply Spring Layout for tidy grid alignment (3 columns)
        makeCompactGrid(formPanel, labels.length + 1, 3, 10, 10, 10, 10);

        panel.add(formPanel, BorderLayout.CENTER);

        // Store fields for dynamic use in showUpdateScreen
        panel.putClientProperty("brandField", brandField);
        panel.putClientProperty("modelField", modelField);
        panel.putClientProperty("priceField", priceField);
        panel.putClientProperty("quantityField", quantityField);
        panel.putClientProperty("imagePathField", imagePathField);
        panel.putClientProperty("actionBtn", actionBtn);

        Component titleComp = ((BorderLayout) headerPanel.getLayout()).getLayoutComponent(headerPanel, BorderLayout.CENTER);
        panel.putClientProperty("headerTitleLabel", titleComp);

        return panel;
    }

    /**
     * Tries to find the JTable inside the JFileChooser and sort it by the
     * "Date Modified" column, descending.
     */
    private void sortFileChooserByDate(JFileChooser fileChooser) {
        JTable table = findComponentOfType(fileChooser, JTable.class);
        if (table == null) return;

        int dateColumnIndex = -1;
        for (int i = 0; i < table.getColumnCount(); i++) {
            if ("Date Modified".equalsIgnoreCase(table.getColumnName(i))) {
                dateColumnIndex = i;
                break;
            }
        }

        if (dateColumnIndex != -1) {
            final int col = dateColumnIndex;
            SwingUtilities.invokeLater(() -> {
                table.getRowSorter().toggleSortOrder(col); // Ascending
                table.getRowSorter().toggleSortOrder(col); // Descending
            });
        }
    }

    /**
     * Recursively finds the first component of a specific type within a container.
     */
    private <T extends Component> T findComponentOfType(Container container, Class<T> type) {
        for (Component c : container.getComponents()) {
            if (type.isInstance(c)) {
                return type.cast(c);
            }
            if (c instanceof Container) {
                T found = findComponentOfType((Container) c, type);
                if (found != null) return found;
            }
        }
        return null;
    }


    // Style text fields (globally via UIManager)
    private void styleTextField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BG.brighter().brighter(), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
    }

    private void showUpdateScreen(CarModel car) {
        JPanel updatePanel = (JPanel) getComponentByName(mainContainer, "Add");

        if (updatePanel == null) {
            updatePanel = createAddUpdatePanel(false);
            mainContainer.add(updatePanel, "Add");
        }

        JTextField brandField = (JTextField) updatePanel.getClientProperty("brandField");
        JTextField modelField = (JTextField) updatePanel.getClientProperty("modelField");
        JTextField priceField = (JTextField) updatePanel.getClientProperty("priceField");
        JTextField quantityField = (JTextField) updatePanel.getClientProperty("quantityField");
        JTextField imagePathField = (JTextField) updatePanel.getClientProperty("imagePathField");
        JButton actionBtn = (JButton) updatePanel.getClientProperty("actionBtn");
        JLabel headerTitleLabel = (JLabel) updatePanel.getClientProperty("headerTitleLabel");

        headerTitleLabel.setText("Update Existing Model");
        actionBtn.setText("Save Updates");

        brandField.setText(car.getBrand());
        modelField.setText(car.getModel());
        priceField.setText(String.valueOf(car.getPrice()));
        quantityField.setText(String.valueOf(car.getQuantity()));

        imagePathField.setText(car.getImagePath() != null ? car.getImagePath() : "");

        // Make sure to access the form panel correctly (it's the CENTER component)
        ((JPanel)((BorderLayout)updatePanel.getLayout()).getLayoutComponent(BorderLayout.CENTER)).putClientProperty("CarModel", car);


        cardLayout.show(mainContainer, "Add");
    }

    private void showRemoveScreen(CarModel car) {
        int confirm = showConfirmDialog(
                "Are you sure you want to remove the entire inventory of: " + car.getBrand() + " " + car.getModel() + "?\n" +
                        "Quantity: " + car.getQuantity(),
                "Confirm Deletion"
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean removed = service.removeCarModel(car.getBrand(), car.getModel());
            if (removed) {
                showInfoDialog(car.getBrand() + " " + car.getModel() + " successfully removed.");
            } else {
                showErrorDialog("Failed to remove model.");
            }
        }
        showMainMenu();
    }

    private void showSellScreen(CarModel car) {
        if (car.getQuantity() <= 0) {
            showWarningDialog("Sale Failed. No cars available to sell for this model.");
            showMainMenu();
            return;
        }

        int confirm = showConfirmDialog(
                "Confirm Sale: " + car.getBrand() + " " + car.getModel() + "\n" +
                        "Price: Rs " + InventoryService.formatPrice(car.getPrice()) + "\n" +
                        "Current Stock: " + car.getQuantity(),
                "Confirm Sale"
        );

        if (confirm == JOptionPane.YES_OPTION) {
            CarModel updatedCar = service.sellCar(car.getBrand(), car.getModel());
            if (updatedCar != null) {
                showInfoDialog("Car Sold! Remaining stock: " + (car.getQuantity())); // Show remaining after sale
            } else {
                showErrorDialog("Sale Failed (Unknown Error).");
            }
        }
        showMainMenu();
    }

    // --- Custom Dialog Boxes (for consistent dark theme) ---

    private void showDialog(String message, String title, int messageType) {
        // Create a panel with the dark background
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_BG);

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setOpaque(false); // Use panel's background
        textArea.setForeground(TEXT_COLOR);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        panel.add(textArea, BorderLayout.CENTER);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));


        JOptionPane.showMessageDialog(this, panel, title, messageType);
    }

    private void showErrorDialog(String message) {
        showDialog(message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarningDialog(String message) {
        showDialog(message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfoDialog(String message) {
        showDialog(message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private int showConfirmDialog(String message, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_BG);

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setOpaque(false); // Use panel's background
        textArea.setForeground(TEXT_COLOR);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        panel.add(textArea, BorderLayout.CENTER);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Note: The buttons inside JOptionPane will still be native L&F
        return JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    }

    // --- Input Validation Helpers ---

    private boolean validateInput(JTextField brandField, JTextField modelField, JTextField priceField, JTextField quantityField) {
        String brand = brandField.getText().trim();
        String model = modelField.getText().trim();
        String priceText = priceField.getText().trim();
        String quantityText = quantityField.getText().trim();

        if (brand.isEmpty() || model.isEmpty()) {
            showErrorDialog("Brand and Model cannot be empty.");
            return false;
        }

        try {
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                showErrorDialog("Price must be a positive number.");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorDialog("Invalid Price. Please enter a numerical value.");
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity < 0) {
                showErrorDialog("Quantity cannot be negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorDialog("Invalid Quantity. Please enter a whole number.");
            return false;
        }

        return true;
    }

    private void clearFields(JTextField... fields) {
        for (JTextField field : fields) {
            field.setText("");
        }
    }

    // --- Advanced Filter/Search Screen ---

    private JPanel createFilterInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("Filter");
        panel.setBackground(PRIMARY_BG); // Manual Style

        panel.add(createHeaderPanel("View Inventory & Advanced Search"), BorderLayout.NORTH);

        String[] columnNames = {"Brand", "Model", "Price (Rs)", "Quantity", "Image Path"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int column) {
                return column == 3 ? Integer.class : String.class;
            }
        };
        JTable inventoryTable = new JTable(tableModel);
        inventoryTable.setForeground(TEXT_COLOR); // Text in cells
        inventoryTable.setBackground(PRIMARY_BG.brighter());
        inventoryTable.setGridColor(PRIMARY_BG.darker());
        inventoryTable.setSelectionBackground(BUTTON_COLOR.darker()); // Darker red selection
        inventoryTable.setSelectionForeground(Color.WHITE); // White text on selection

        // --- FIX: Set Table Header Text Color to BLACK ---
        JTableHeader header = inventoryTable.getTableHeader();
        header.setBackground(PRIMARY_BG.darker());
        header.setForeground(Color.BLACK); // As requested
        header.setFont(new Font("SansSerif", Font.BOLD, 14));

        inventoryTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.getViewport().setBackground(PRIMARY_BG);

        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        filterBar.setBackground(PRIMARY_BG.darker()); // Manual Style
        filterBar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel searchLabel = new JLabel("Search Text:");
        searchLabel.setForeground(TEXT_COLOR); // Manual Style

        JTextField searchField = new JTextField("Search Brand or Model...", 15);
        styleSearchField(searchField, "Search Brand or Model...");

        JLabel priceLabel = new JLabel("Price Range (Rs):");
        priceLabel.setForeground(TEXT_COLOR); // Manual Style

        JTextField minPriceField = new JTextField("Min Price", 8);
        JTextField maxPriceField = new JTextField("Max Price", 8);
        styleSearchField(minPriceField, "Min Price");
        styleSearchField(maxPriceField, "Max Price");

        JCheckBox inStockCheck = new JCheckBox("In Stock Only");
        inStockCheck.setBackground(PRIMARY_BG.darker()); // Manual Style
        inStockCheck.setForeground(TEXT_COLOR); // Manual Style
        inStockCheck.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton searchBtn = createStyledButton("Apply Filters"); // Manual Style (RED, Black Text)

        // --- FIX: Reset Button with BLACK text ---
        JButton resetBtn = createStyledButton("Reset"); // Manual Style (RED base)
        resetBtn.setBackground(new Color(200, 200, 200)); // Override for gray color
        resetBtn.setForeground(Color.BLACK); // Override for BLACK text

        filterBar.add(searchLabel);
        filterBar.add(searchField);
        filterBar.add(priceLabel);
        filterBar.add(minPriceField);
        filterBar.add(maxPriceField);
        filterBar.add(inStockCheck);
        filterBar.add(searchBtn);
        filterBar.add(resetBtn);

        panel.add(filterBar, BorderLayout.SOUTH);

        Runnable updateTable = () -> {
            tableModel.setRowCount(0);

            String query = searchField.getText().equals("Search Brand or Model...") ? "" : searchField.getText();

            double minPrice = 0;
            try {
                String minText = minPriceField.getText().equals("Min Price") ? "" : minPriceField.getText();
                if (!minText.isEmpty()) minPrice = Double.parseDouble(minText);
            } catch (NumberFormatException ex) {
                minPriceField.setText("Min Price");
            }

            double maxPrice = Double.MAX_VALUE;
            try {
                String maxText = maxPriceField.getText().equals("Max Price") ? "" : maxPriceField.getText();
                if (!maxText.isEmpty()) maxPrice = Double.parseDouble(maxText);
            } catch (NumberFormatException ex) {
                maxPriceField.setText("Max Price");
            }

            boolean inStock = inStockCheck.isSelected();

            List<CarModel> filteredModels = service.filterInventory(query, minPrice, maxPrice, inStock);

            for (CarModel car : filteredModels) {
                tableModel.addRow(new Object[]{
                        car.getBrand(),
                        car.getModel(),
                        InventoryService.formatPrice(car.getPrice()),
                        car.getQuantity(),
                        car.getImagePath()
                });
            }
        };

        updateTable.run();
        searchBtn.addActionListener(e -> updateTable.run());
        resetBtn.addActionListener(e -> {
            searchField.setText("Search Brand or Model..."); styleSearchField(searchField, "Search Brand or Model...");
            minPriceField.setText("Min Price"); styleSearchField(minPriceField, "Min Price");
            maxPriceField.setText("Max Price"); styleSearchField(maxPriceField, "Max Price");
            inStockCheck.setSelected(false);
            updateTable.run();
        });

        inventoryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = inventoryTable.getSelectedRow();
                    if (row != -1) {
                        int modelRow = inventoryTable.convertRowIndexToModel(row);
                        String brand = (String) tableModel.getValueAt(modelRow, 0);
                        String model = (String) tableModel.getValueAt(modelRow, 1);

                        CarModel car = service.getCarModel(brand, model);
                        if (car != null) {
                            showCarDetailsScreen(car);
                        }
                    }
                }
            }
        });

        return panel;
    }

    private void styleSearchField(JTextField field, String placeholder) {
        field.setForeground(Color.GRAY);
        // Style handled by UIManager
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_COLOR);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                } else {
                    field.setForeground(TEXT_COLOR);
                }
            }
        });
    }

    // --- Detail View Screen ---

    private void showCarDetailsScreen(CarModel carModel) {
        Component existingPanel = getComponentByName(mainContainer, "Details");
        if (existingPanel != null) mainContainer.remove(existingPanel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("Details");
        panel.setBackground(PRIMARY_BG); // Manual Style

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_BG.darker()); // Manual Style
        header.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton backBtn = new JButton("<- Back to Inventory Search");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        backBtn.setBackground(PRIMARY_BG.darker()); // Manual Style
        backBtn.setForeground(ACCENT_COLOR); // Manual Style
        backBtn.setBorder(BorderFactory.createEmptyBorder());
        backBtn.setOpaque(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "Filter"));
        header.add(backBtn, BorderLayout.WEST);

        panel.add(header, BorderLayout.NORTH);

        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setBorder(BorderFactory.createLineBorder(TEXT_COLOR, 1));

        // Load image in a background thread
        loadAndSetImage(carModel.getImagePath(), lblImage, 600, 350);

        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 10, 20));
        infoPanel.setBackground(PRIMARY_BG); // Manual Style
        infoPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        infoPanel.add(new JLabel("Brand:"));
        infoPanel.add(new JLabel(carModel.getBrand()));
        infoPanel.add(new JLabel("Model:"));
        infoPanel.add(new JLabel(carModel.getModel()));
        infoPanel.add(new JLabel("Price (Rs):"));
        infoPanel.add(new JLabel(InventoryService.formatPrice(carModel.getPrice())));
        infoPanel.add(new JLabel("Quantity:"));
        infoPanel.add(new JLabel(String.valueOf(carModel.getQuantity())));

        for (int i = 0; i < infoPanel.getComponentCount(); i++) {
            Component comp = infoPanel.getComponent(i);
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(TEXT_COLOR); // Manual Style
                if (i % 2 == 0) {
                    ((JLabel) comp).setFont(new Font("SansSerif", Font.BOLD, 18));
                } else {
                    ((JLabel) comp).setFont(new Font("SansSerif", Font.PLAIN, 18));
                }
            }
        }

        JPanel content = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 20));
        content.setBackground(PRIMARY_BG); // Manual Style
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.add(infoPanel);
        content.add(lblImage);

        panel.add(content, BorderLayout.CENTER);
        mainContainer.add(panel, "Details");
        cardLayout.show(mainContainer, "Details");
    }

    /**
     * Loads an image in a background thread and sets it on a JLabel.
     */
    private void loadAndSetImage(String imagePath, JLabel label, int width, int height) {
        label.setText("Loading image...");
        label.setForeground(TEXT_COLOR);
        label.setPreferredSize(new Dimension(width, height));

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                if (imagePath == null || imagePath.isEmpty() || !new File(imagePath).exists()) {
                    return null;
                }
                ImageIcon icon = new ImageIcon(imagePath);
                Image img = icon.getImage();
                // Calculate scaled dimensions while maintaining aspect ratio
                int originalW = icon.getIconWidth();
                int originalH = icon.getIconHeight();
                if (originalW <= 0 || originalH <= 0) return null; // Invalid image

                double scale = Math.min((double)width / originalW, (double)height / originalH);
                int scaledW = (int) (originalW * scale);
                int scaledH = (int) (originalH * scale);

                Image scaledImg = img.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon scaledIcon = get();
                    if (scaledIcon != null) {
                        label.setIcon(scaledIcon);
                        label.setText(null);
                    } else {
                        label.setText("Image Not Available");
                        label.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                    label.setText("Image Load Error");
                    label.setForeground(Color.RED);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }


    // --- Sales Log Screen ---

    private JPanel createSalesLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("SalesLog");
        panel.setBackground(PRIMARY_BG); // Manual Style

        panel.add(createHeaderPanel("Sales Transaction History"), BorderLayout.NORTH);

        String[] columnNames = {"Timestamp", "Brand", "Model", "Sale Price (Rs)"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable logTable = new JTable(tableModel);
        logTable.setForeground(TEXT_COLOR);
        logTable.setBackground(PRIMARY_BG.brighter());
        logTable.setGridColor(PRIMARY_BG.darker());
        logTable.setSelectionBackground(BUTTON_COLOR.darker());

        JTableHeader logHeader = logTable.getTableHeader();
        logHeader.setBackground(PRIMARY_BG.darker());
        logHeader.setForeground(Color.BLACK); // Black header text
        logHeader.setFont(new Font("SansSerif", Font.BOLD, 14));

        logTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.getViewport().setBackground(PRIMARY_BG);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        List<SaleModel> sales = service.getSalesLog().stream()
                .sorted(Comparator.comparing(SaleModel::getTimestamp).reversed())
                .collect(Collectors.toList());

        for (SaleModel sale : sales) {
            tableModel.addRow(new Object[]{
                    sale.getTimestamp().format(formatter),
                    sale.getBrand(),
                    sale.getModel(),
                    InventoryService.formatPrice(sale.getSalePrice())
            });
        }

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(PRIMARY_BG.darker()); // Manual Style
        JLabel countLabel = new JLabel("Total Transactions Recorded: " + sales.size());
        countLabel.setForeground(ACCENT_COLOR); // Manual Style
        countLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        footer.add(countLabel);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    // --- Reports Screen ---

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("Reports");
        panel.setBackground(PRIMARY_BG); // Manual Style

        panel.add(createHeaderPanel("Key Financial Reports"), BorderLayout.NORTH);

        JPanel reportGrid = new JPanel(new GridLayout(4, 2, 20, 30));
        reportGrid.setBackground(PRIMARY_BG); // Manual Style
        reportGrid.setBorder(new EmptyBorder(50, 100, 50, 100));

        double inventoryValue = service.getTotalInventoryValue();
        addReportMetric(reportGrid, "Total Current Inventory Value:", "Rs " + InventoryService.formatPrice(inventoryValue), false);

        double totalRevenue = service.getTotalRevenue();
        addReportMetric(reportGrid, "Total Sales Revenue (Lifetime):", "Rs " + InventoryService.formatPrice(totalRevenue), true);

        int totalSales = service.getSalesLog().size();
        addReportMetric(reportGrid, "Total Units Sold:", String.valueOf(totalSales) + " Units", false);

        String bestSeller = service.getBestSellingModel();
        addReportMetric(reportGrid, "Best Selling Model:", bestSeller, false);

        panel.add(reportGrid, BorderLayout.CENTER);

        return panel;
    }

    private void addReportMetric(JPanel panel, String title, String value, boolean highlight) {
        JLabel titleLabel = new JLabel(title, JLabel.RIGHT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR); // Manual Style

        JLabel valueLabel = new JLabel(value, JLabel.LEFT);
        valueLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        // Use Accent (Gold) for highlighted value, Red for others
        valueLabel.setForeground(highlight ? ACCENT_COLOR : BUTTON_COLOR); // Manual Style

        panel.add(titleLabel);
        panel.add(valueLabel);
    }

    // --- Spring Layout Utility ---
    private static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout) parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                if (r * cols + c < parent.getComponentCount()) { // Avoid IndexOutOfBounds
                    SpringLayout.Constraints constraints = layout.getConstraints(parent.getComponent(r * cols + c));
                    width = Spring.max(width, constraints.getWidth());
                }
            }
            for (int r = 0; r < rows; r++) {
                if (r * cols + c < parent.getComponentCount()) {
                    SpringLayout.Constraints constraints = layout.getConstraints(parent.getComponent(r * cols + c));
                    constraints.setX(x);
                    constraints.setWidth(width);
                }
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                if (r * cols + c < parent.getComponentCount()) {
                    SpringLayout.Constraints constraints = layout.getConstraints(parent.getComponent(r * cols + c));
                    height = Spring.max(height, constraints.getHeight());
                }
            }
            for (int c = 0; c < cols; c++) {
                if (r * cols + c < parent.getComponentCount()) {
                    SpringLayout.Constraints constraints = layout.getConstraints(parent.getComponent(r * cols + c));
                    constraints.setY(y);
                    constraints.setHeight(height);
                }
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }

    /**
     * Inner Class for JFileChooser Image Preview Accessory.
     * Shows a thumbnail of the selected image file.
     */
    private class ImagePreviewAccessory extends JComponent implements PropertyChangeListener {
        private JLabel previewLabel;
        private File selectedFile;
        private final int PREVIEW_WIDTH = 200;
        private final int PREVIEW_HEIGHT = 200;

        public ImagePreviewAccessory() {
            setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));
            setBorder(BorderFactory.createEtchedBorder());
            previewLabel = new JLabel();
            previewLabel.setHorizontalAlignment(JLabel.CENTER);
            previewLabel.setVerticalAlignment(JLabel.CENTER);
            setLayout(new BorderLayout());
            add(previewLabel, BorderLayout.CENTER);
            previewLabel.setText("Image Preview"); // Initial text
            // As requested: "Image Preview" text to WHITE
            previewLabel.setForeground(Color.WHITE);
            // The accessory panel background should be dark to match the overall app.
            setBackground(PRIMARY_BG.darker());
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(propName)) {
                selectedFile = (File) evt.getNewValue();
                updatePreview();
            }
        }

        private void updatePreview() {
            if (selectedFile == null) {
                previewLabel.setIcon(null);
                previewLabel.setText("No file selected");
                previewLabel.setForeground(Color.WHITE); // Keep consistent white
                return;
            }

            String path = selectedFile.getAbsolutePath().toLowerCase(); // Use lowercase for extension check
            if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") || path.endsWith(".gif")) {

                // Use SwingWorker to load image in background to avoid freezing UI
                SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
                    @Override
                    protected ImageIcon doInBackground() {
                        try {
                            ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
                            Image img = icon.getImage();
                            int w = PREVIEW_WIDTH - 10; // padding
                            int h = PREVIEW_HEIGHT - 10;

                            int originalW = icon.getIconWidth();
                            int originalH = icon.getIconHeight();
                            if(originalW <= 0 || originalH <= 0) return null; // Invalid image

                            double scale = Math.min((double)w / originalW, (double)h / originalH);
                            int scaledW = (int) (originalW * scale);
                            int scaledH = (int) (originalH * scale);

                            Image scaledImg = img.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
                            return new ImageIcon(scaledImg);
                        } catch (Exception e) {
                            return null; // Handle image loading errors
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            ImageIcon scaledIcon = get();
                            if (scaledIcon != null) {
                                previewLabel.setIcon(scaledIcon);
                                previewLabel.setText(null);
                            } else {
                                previewLabel.setIcon(null);
                                previewLabel.setText("Preview Error");
                                previewLabel.setForeground(Color.RED); // Error text can be red
                            }
                        } catch (Exception e) {
                            previewLabel.setIcon(null);
                            previewLabel.setText("Preview Error");
                            previewLabel.setForeground(Color.RED); // Error text can be red
                        }
                    }
                };
                worker.execute();

            } else {
                previewLabel.setIcon(null);
                previewLabel.setText("Not an image");
                previewLabel.setForeground(Color.WHITE); // Keep consistent white
            }
        }
    }
}

