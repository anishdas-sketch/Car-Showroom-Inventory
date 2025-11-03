package showroom.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single sale transaction.
 */
public class SaleModel {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime timestamp;
    private final String brand;
    private final String model;
    private final double salePrice;

    public SaleModel(LocalDateTime timestamp, String brand, String model, double salePrice) {
        this.timestamp = timestamp;
        this.brand = brand;
        this.model = model;
        this.salePrice = salePrice;
    }

    // Constructor for new sales (sets current timestamp)
    public SaleModel(String brand, String model, double salePrice) {
        this(LocalDateTime.now(), brand, model, salePrice);
    }

    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public double getSalePrice() { return salePrice; }

    /**
     * Converts the SaleModel object to a CSV string for saving.
     */
    @Override
    public String toString() {
        return timestamp.format(FORMATTER) + "," + brand + "," + model + "," + salePrice;
    }

    /**
     * Creates a SaleModel object from a single CSV line.
     */
    public static SaleModel fromCSV(String line) {
        try {
            String[] parts = line.split(",", 4);
            if (parts.length < 4) return null;

            LocalDateTime timestamp = LocalDateTime.parse(parts[0].trim(), FORMATTER);
            String brand = parts[1].trim();
            String model = parts[2].trim();
            double salePrice = Double.parseDouble(parts[3].trim());

            return new SaleModel(timestamp, brand, model, salePrice);
        } catch (Exception e) {
            System.err.println("Error parsing CSV line for SaleModel: " + line + ". Error: " + e.getMessage());
            return null;
        }
    }
}