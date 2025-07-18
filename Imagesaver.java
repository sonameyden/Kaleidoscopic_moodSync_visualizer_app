
// utils/ImageSaver.java
package utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Imagesaver {
    
    public static void saveImage(BufferedImage image, File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        String format = "png"; // Default format
        
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            format = "jpg";
        } else if (fileName.endsWith(".gif")) {
            format = "gif";
        } else if (fileName.endsWith(".bmp")) {
            format = "bmp";
        }
        
        // Ensure parent directory exists
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        // Save the image
        boolean success = ImageIO.write(image, format, file);
        
        if (!success) {
            throw new IOException("Failed to save image in format: " + format);
        }
    }
    
    public static void saveWithTimestamp(BufferedImage image, String directory, String prefix) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filename = prefix + "_" + timestamp + ".png";
        File file = new File(dir, filename);
        
        saveImage(image, file);
    }
}