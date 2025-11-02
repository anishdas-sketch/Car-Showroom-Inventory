package showroom.service;

import showroom.model.CarModel;
import showroom.model.SaleModel;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all business logic, persistence (CSV files), and image management.
 * Includes logic for inventory, sales, filtering, and reporting.
 * Data files are now saved in a 'data' folder *outside* the 'src' directory (relative to project root).
 */
public class InventoryService {
    private List<CarModel> inventory;
    private List<SaleModel> salesLog;
    
    // --- CORRECTED FILE PATHS ---
    // Use "../data/" to place the data folder one level up from the execution directory (src),
    // effectively placing it in the project root.
    private static final String DATA_DIR_RELATIVE_TO_SRC = "../Car Showroom Inventory/data/"; // Relative path from 'src'
    private final String INVENTORY_FILE = DATA_DIR_RELATIVE_TO_SRC + "inventory.csv";
    private final String SALES_LOG_FILE = DATA_DIR_RELATIVE_TO_SRC + "sales_log.csv";
    private final String IMAGE_DIR = DATA_DIR_RELATIVE_TO_SRC + "images"; // Image directory path

    // Store the absolute path to the project root's data directory for consistency
    private final Path dataDirectoryPath;
    private final Path imageDirectoryPath;


    public InventoryService() {
        inventory = new ArrayList<>();
        salesLog = new ArrayList<>();

        // Resolve absolute paths ONCE based on expected execution from 'src'
        Path executionPath = Paths.get("").toAbsolutePath(); // Should be the 'src' directory
        dataDirectoryPath = executionPath.resolve(DATA_DIR_RELATIVE_TO_SRC).normalize();
        imageDirectoryPath = dataDirectoryPath.resolve("images").normalize();


        // Ensure data directories exist using absolute paths
        try {
            if (!Files.exists(dataDirectoryPath)) {
                Files.createDirectories(dataDirectoryPath);
                System.out.println("Created data directory: " + dataDirectoryPath);
            }
            if (!Files.exists(imageDirectoryPath)) {
                Files.createDirectories(imageDirectoryPath);
                System.out.println("Created image directory: " + imageDirectoryPath);
            }
        } catch (IOException e) {
             System.err.println("FATAL ERROR: Could not create data directories: " + e.getMessage());
             // In a real app, might throw exception or exit
        }


        loadInventory();
        loadSalesLog();
    }

    // --- Persistence Methods ---

    // Helper to get the absolute path for data files relative to the PROJECT ROOT data dir
    private Path getDataFilePath(String fileName) {
        // Uses the resolved absolute path of the data directory
        return dataDirectoryPath.resolve(fileName).normalize();
    }


    private void loadInventory() {
        Path filePath = getDataFilePath("inventory.csv"); // Use helper
        File file = filePath.toFile();

        System.out.println("Loading inventory from: " + filePath); // Debugging

        // Directory creation now handled in constructor, but double-check is safe
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            if (!file.exists()) {
                System.out.println("Inventory file not found, creating new one.");
                file.createNewFile();
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    CarModel car = CarModel.fromCSV(line);
                    if (car != null) {
                        // Ensure image path stored in CSV is correctly interpreted relative to project root
                        // If it's already relative like "data/images/...", it should work.
                        // If it became absolute somehow, it might need correction here.
                        inventory.add(car);
                    }
                }
                 System.out.println("Loaded " + inventory.size() + " cars from inventory.");
            }
        } catch (IOException e) {
            System.err.println("Error loading inventory file: " + filePath + ". Error: " + e.getMessage());
        }
    }

    private void saveInventory() {
         Path filePath = getDataFilePath("inventory.csv"); // Use helper
         System.out.println("Saving inventory to: " + filePath); // Debugging
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            for (CarModel car : inventory) {
                bw.write(car.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving inventory file: " + filePath + ". Error: " + e.getMessage());
        }
    }

    private void loadSalesLog() {
        Path filePath = getDataFilePath("sales_log.csv"); // Use helper
        File file = filePath.toFile();
        System.out.println("Loading sales log from: " + filePath); // Debugging

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            if (!file.exists()){
                 System.out.println("Sales log file not found, creating new one.");
                 file.createNewFile();
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    SaleModel sale = SaleModel.fromCSV(line);
                    if (sale != null) salesLog.add(sale);
                }
                 System.out.println("Loaded " + salesLog.size() + " sales records.");
            }
        } catch (IOException e) {
            System.err.println("Error loading sales log file: " + filePath + ". Error: " + e.getMessage());
        }
    }

    private void saveSalesLog(SaleModel sale) {
        Path filePath = getDataFilePath("sales_log.csv"); // Use helper
        System.out.println("Appending sale to: " + filePath); // Debugging
        // Appends the new sale to the log file
        try (FileWriter fw = new FileWriter(filePath.toFile(), true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(sale.toString());
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to sales log file: " + filePath + ". Error: " + e.getMessage());
        }
    }

    // --- Image Handling ---

    public String storeImageLocally(String sourcePath, String brand, String model) {
        if (sourcePath == null || sourcePath.isEmpty()) return null;

        try {
            String safeName = (brand + "_" + model).replaceAll("[^a-zA-Z0-9_\\s-]", "").trim().replace(" ", "_");
            String extension = ".png"; // Default

            // Try to determine extension
            int dotIndex = sourcePath.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < sourcePath.length() - 1) {
                String ext = sourcePath.substring(dotIndex).toLowerCase();
                if (ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") || ext.equals(".gif")) {
                    extension = ext;
                }
            }

            // Target path is now relative to the resolved absolute IMAGE directory path
            Path targetPath = imageDirectoryPath.resolve(safeName + extension).normalize();
             System.out.println("Target image path: " + targetPath); // Debugging

            if (sourcePath.toLowerCase().startsWith("http")) {
                // It's a URL, download it
                URL url = new URL(sourcePath);
                 System.out.println("Downloading image from URL: " + sourcePath); // Debugging
                try (InputStream in = url.openStream()) {
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                // It's a local file path, copy it
                Path source = Paths.get(sourcePath);
                 System.out.println("Copying image from local path: " + sourcePath); // Debugging
                if (!Files.exists(source) || !Files.isReadable(source)) {
                     System.err.println("Local file not found or not readable at path: " + sourcePath);
                     return null;
                }
                Files.copy(source, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Return the relative path used for persistence, now relative to the PROJECT ROOT.
            // Construct this based on the known structure "data/images/filename"
            String relativePathForStorage = "data/images/" + targetPath.getFileName().toString();
             System.out.println("Storing relative path: " + relativePathForStorage); // Debugging
            return relativePathForStorage.replace("\\", "/"); // Ensure forward slashes

        } catch (MalformedURLException e) {
             System.err.println("Invalid URL provided for image: " + sourcePath + ". Error: " + e.getMessage());
             return null;
        } catch (IOException e) {
            System.err.println("Failed to store image locally from source: " + sourcePath + ". IO Error: " + e.getMessage());
             e.printStackTrace(); // More detail on IO errors
            return null;
        } catch (Exception e) { // Catch broader exceptions
            System.err.println("An unexpected error occurred storing image from source: " + sourcePath + ". Error: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            return null;
        }
    }

    // --- Inventory CRUD Operations ---

    public void addCarModel(CarModel car) {
        // Ensure the image path is relative to project root before adding
        if(car.getImagePath() != null && !car.getImagePath().startsWith("data/images/")) {
             System.err.println("Warning: Correcting image path format before adding car.");
             // Attempt to correct - this assumes the filename is correct but prefix is wrong/missing
             Path imageFileName = Paths.get(car.getImagePath()).getFileName();
             car.setImagePath("data/images/" + imageFileName.toString());
        }
        inventory.add(car);
        saveInventory();
    }

    public void updateCarModel(CarModel car, String newBrand, String newModel, double newPrice, int newQty, String newImageSourcePath) {

        String finalRelativeImagePath = car.getImagePath(); // Default to old relative path

        // Only update image path if a new valid one is provided AND it's different from the old stored path
        if (newImageSourcePath != null && !newImageSourcePath.isEmpty()) {
            // Check if the input path is already the stored relative path (no change needed)
            if (!newImageSourcePath.equals(car.getImagePath())) {
                String storedRelativePath = storeImageLocally(newImageSourcePath, newBrand, newModel);

                if (storedRelativePath != null && !storedRelativePath.equals(car.getImagePath())) {
                    // Delete old image if it exists and path changed
                    deleteImageFile(car.getImagePath());
                    finalRelativeImagePath = storedRelativePath; // Use new relative path
                } else if (storedRelativePath == null) {
                    // Storing failed, keep the old path but log a warning
                    System.err.println("Failed to update image for " + newBrand + " " + newModel + ". Keeping old image path: " + car.getImagePath());
                }
                // if storedPath is same as old, finalRelativeImagePath remains unchanged
            }
        }

        car.setBrand(newBrand);
        car.setModel(newModel);
        car.setPrice(newPrice);
        car.setQuantity(newQty);
        car.setImagePath(finalRelativeImagePath); // Save the potentially updated stored relative path

        saveInventory();
    }

    public boolean removeCarModel(String brand, String model) {
         CarModel carToRemove = getCarModel(brand, model);
         if (carToRemove != null) {
            // Delete the associated image file when removing the car
            deleteImageFile(carToRemove.getImagePath());

            inventory.remove(carToRemove);
            saveInventory();
            return true;
        }
        return false;
    }

    // Helper to delete an image file using its stored relative path (relative to project root)
    private void deleteImageFile(String relativeImagePath) {
         if (relativeImagePath != null && !relativeImagePath.isEmpty()) {
            try {
                // Resolve the relative path against the project root's data directory
                Path imagePathToDelete = dataDirectoryPath.getParent().resolve(relativeImagePath).normalize(); // Go up from data dir to project root
                System.out.println("Attempting to delete image file: " + imagePathToDelete); // Debugging
                 if (Files.exists(imagePathToDelete)) {
                    Files.delete(imagePathToDelete);
                    System.out.println("Successfully deleted image file: " + imagePathToDelete);
                 } else {
                     System.out.println("Image file not found for deletion: " + imagePathToDelete);
                 }
            } catch (IOException e) {
                System.err.println("Could not delete image file: " + relativeImagePath + " Error: " + e.getMessage());
            } catch (Exception e) {
                 System.err.println("Error constructing path for image deletion: " + relativeImagePath + " Error: " + e.getMessage());
                 e.printStackTrace();
            }
        }
    }

    // --- Sales and Reporting ---

    public CarModel sellCar(String brand, String model) {
        CarModel car = getCarModel(brand, model);
        if (car != null && car.getQuantity() > 0) {
            car.setQuantity(car.getQuantity() - 1);
            saveInventory();

            SaleModel sale = new SaleModel(brand, model, car.getPrice());
            salesLog.add(sale);
            saveSalesLog(sale);

            return car;
        }
        return null;
    }

    public List<SaleModel> getSalesLog() {
        return new ArrayList<>(salesLog);
    }

    public double getTotalInventoryValue() {
        return inventory.stream()
            .mapToDouble(c -> c.getPrice() * c.getQuantity())
            .sum();
    }

    public double getTotalRevenue() {
        return salesLog.stream()
            .mapToDouble(SaleModel::getSalePrice)
            .sum();
    }

    public String getBestSellingModel() {
        return salesLog.stream()
            .collect(Collectors.groupingBy(s -> s.getBrand() + " " + s.getModel(), Collectors.counting()))
            .entrySet().stream()
            .max(Comparator.comparingLong(Map.Entry::getValue))
            .map(entry -> entry.getKey() + " (" + entry.getValue() + " units)")
            .orElse("N/A (No sales yet)");
    }

    // --- Getters and Filtering ---

    public List<CarModel> getAllModels() {
        return new ArrayList<>(inventory); // Return copy
    }

    public List<String> getAllBrands() {
        return inventory.stream().map(CarModel::getBrand).distinct().sorted().collect(Collectors.toList());
    }

    public List<CarModel> getModelsByBrand(String brand) {
        return inventory.stream().filter(c -> c.getBrand().equalsIgnoreCase(brand)).collect(Collectors.toList());
    }

    public CarModel getCarModel(String brand, String model) {
        for (CarModel c : inventory) {
            if (c.getBrand().equalsIgnoreCase(brand) && c.getModel().equalsIgnoreCase(model))
                return c;
        }
        return null;
    }

    public List<CarModel> filterInventory(String searchText, double minPrice, double maxPrice, boolean inStockOnly) {
        String query = searchText.toLowerCase().trim(); // Trim search text

        return inventory.stream()
            .filter(car ->
                (query.isEmpty() || car.getBrand().toLowerCase().contains(query) || car.getModel().toLowerCase().contains(query)) &&
                car.getPrice() >= minPrice &&
                car.getPrice() <= maxPrice &&
                (!inStockOnly || car.getQuantity() > 0)
            )
            .sorted(Comparator.comparing(CarModel::getBrand).thenComparing(CarModel::getModel))
            .collect(Collectors.toList());
    }

    public static String formatPrice(double price) {
        return String.format(Locale.US, "%,.2f", price);
    }
}
