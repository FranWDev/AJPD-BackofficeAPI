package org.dubini.backofficeAPI.service;

import net.coobird.thumbnailator.Thumbnails;

import org.dubini.backofficeAPI.dto.response.ImageResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    private final Path uploadPath;

    public ImageService(@Value("${app.upload.dir:${user.home}/uploads}") String uploadDir) {
        this.uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir).toAbsolutePath().normalize();
        try {
            if (!Files.exists(this.uploadPath)) {
                Files.createDirectories(this.uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de subida de archivos", e);
        }
    }

    public ImageResponseDTO saveImage(MultipartFile file, int width, int height, float quality) throws IOException {
        String extension = "webp";
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;
        Path filePath = uploadPath.resolve(uniqueFilename);

        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
 
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(originalImage)
                .size(width, height)
                .outputFormat("webp")
                .outputQuality(quality)
                .toOutputStream(outputStream);

        Files.write(filePath, outputStream.toByteArray());

        String fileUrl = "/images/" + uniqueFilename;
        long fileSize = Files.size(filePath);

        return new ImageResponseDTO(uniqueFilename, fileUrl, fileSize);
    }
}