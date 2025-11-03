package showroom.model;

/**
 * Represents a single car model in the inventory.
 */
public class CarModel {
    private String brand;
    private String model;
    private double price;
    private int quantity;
    private String imagePath; // Path to the local image file

    public CarModel(String brand, String model, double price, int quantity, String imagePath) {
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.quantity = quantity;
        this.imagePath = imagePath;
    }

    // Getters
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImagePath() { return imagePath; }

    // Setters
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    /**
     * Converts the CarModel object to a CSV string for saving.
     */
    @Override
    public String toString() {
        return brand + "," + model + "," + price + "," + quantity + "," + imagePath;
    }

    /**
     * Creates a CarModel object from a single CSV line.
     */
    public static CarModel fromCSV(String line) {
        try {
            // Split only on commas that are NOT followed by a space
            String[] parts = line.split(",", 5);
            if (parts.length < 5) return null;
            
            String brand = parts[0].trim();
            String model = parts[1].trim();
            double price = Double.parseDouble(parts[2].trim());
            int quantity = Integer.parseInt(parts[3].trim());
            String imagePath = parts[4].trim();

            return new CarModel(brand, model, price, quantity, imagePath);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Error parsing CSV line for CarModel: " + line + ". Error: " + e.getMessage());
            return null;
        }
    }
}