package com.example.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件服务控制器
 */
@RestController
@RequestMapping("/image")
public class FileController {

    private static final String UPLOAD_PATH = "/app/uploads/";

    @GetMapping("/bigTypeImgs/{filename}")
    public ResponseEntity<Resource> getBigTypeImage(@PathVariable String filename) {
        return getImage("bigTypeImgs", filename);
    }

    @GetMapping("/productImgs/{filename}")
    public ResponseEntity<Resource> getProductImage(@PathVariable String filename) {
        return getImage("productImgs", filename);
    }

    @GetMapping("/swiperImgs/{filename}")
    public ResponseEntity<Resource> getSwiperImage(@PathVariable String filename) {
        return getImage("swiperImgs", filename);
    }

    @GetMapping("/productSwiperImgs/{filename}")
    public ResponseEntity<Resource> getProductSwiperImage(@PathVariable String filename) {
        return getImage("productSwiperImgs", filename);
    }

    private ResponseEntity<Resource> getImage(String folder, String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_PATH + folder + "/" + filename);
            File file = filePath.toFile();
            
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            
            // 确定内容类型
            String contentType = getContentType(filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
} 