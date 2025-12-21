package com.arms.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
    
    public static boolean createDirectory(String path) {
        try {
            Files.createDirectories(Paths.get(path));
            return true;
        } catch (IOException e) {
            Logger.error("Failed to create directory: " + path, e);
            return false;
        }
    }
    
    public static boolean deleteDirectory(String path) {
        try {
            Files.walk(Paths.get(path))
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        Logger.error("Failed to delete file: " + p, e);
                    }
                });
            return true;
        } catch (IOException e) {
            Logger.error("Failed to delete directory: " + path, e);
            return false;
        }
    }
    
    public static boolean copyFile(String source, String destination) {
        try {
            Files.copy(Paths.get(source), Paths.get(destination), 
                StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            Logger.error("Failed to copy file from " + source + " to " + destination, e);
            return false;
        }
    }
    
    public static boolean moveFile(String source, String destination) {
        try {
            Files.move(Paths.get(source), Paths.get(destination), 
                StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            Logger.error("Failed to move file from " + source + " to " + destination, e);
            return false;
        }
    }
    
    public static long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            Logger.error("Failed to get file size: " + filePath, e);
            return 0;
        }
    }
    
    public static String readFileToString(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            Logger.error("Failed to read file: " + filePath, e);
            return null;
        }
    }
    
    public static boolean writeStringToFile(String content, String filePath) {
        try {
            Files.write(Paths.get(filePath), content.getBytes());
            return true;
        } catch (IOException e) {
            Logger.error("Failed to write file: " + filePath, e);
            return false;
        }
    }
    
    public static boolean appendToFile(String content, String filePath) {
        try {
            Files.write(Paths.get(filePath), content.getBytes(), 
                StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            Logger.error("Failed to append to file: " + filePath, e);
            return false;
        }
    }
    
    public static boolean zipDirectory(String sourceDir, String zipFile) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Path sourcePath = Paths.get(sourceDir);
            Files.walk(sourcePath)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                        zos.putNextEntry(zipEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        Logger.error("Failed to add file to zip: " + path, e);
                    }
                });
            return true;
        } catch (IOException e) {
            Logger.error("Failed to create zip file: " + zipFile, e);
            return false;
        }
    }
    
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }
    
    public static boolean isValidImageFile(String fileName) {
        String extension = getFileExtension(fileName);
        return extension.equals("jpg") || extension.equals("jpeg") || 
               extension.equals("png") || extension.equals("gif") || 
               extension.equals("bmp");
    }
    
    public static boolean isValidDocumentFile(String fileName) {
        String extension = getFileExtension(fileName);
        return extension.equals("pdf") || extension.equals("doc") || 
               extension.equals("docx") || extension.equals("txt") || 
               extension.equals("rtf");
    }
    
    public static String getFileSizeHumanReadable(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
    
    public static boolean createBackup(String sourceDir, String backupDir) {
        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupPath = backupDir + File.separator + "backup_" + timestamp + ".zip";
        
        return zipDirectory(sourceDir, backupPath);
    }
}
