package com.healthtracker.controller;

import com.healthtracker.entity.FileRecord;
import com.healthtracker.service.FileRecordService;
import com.healthtracker.service.UserService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileRecordService fileRecordService;
    private final UserService userService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public FileController(FileRecordService fileRecordService, UserService userService) {
        this.fileRecordService = fileRecordService;
        this.userService = userService;
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "type", required = false) String type,
                                      @RequestParam(value = "userId", required = false) Long userId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        String bucket = (type == null || type.isBlank()) ? "common" : type.trim();
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String name = UUID.randomUUID().toString().replace("-", "");
        String filename = ext == null || ext.isBlank() ? name : name + "." + ext;
        Path dirPath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(bucket);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException ex) {
            throw new IllegalArgumentException("创建目录失败", ex);
        }
        Path target = dirPath.resolve(filename);
        file.transferTo(target.toFile());

        Long ownerId = resolveUserId(userId);
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String url = baseUrl + "/uploads/" + bucket + "/" + filename;

        FileRecord record = new FileRecord();
        record.setUserId(ownerId);
        record.setType(bucket);
        record.setOriginalName(file.getOriginalFilename());
        record.setFileName(filename);
        record.setFilePath(target.toString());
        record.setFileUrl(url);
        record.setFileSize(file.getSize());
        record.setContentType(file.getContentType());
        record.setCreatedAt(LocalDateTime.now());
        fileRecordService.save(record);

        Map<String, Object> body = new HashMap<>();
        body.put("id", record.getId());
        body.put("url", url);
        body.put("type", bucket);
        return body;
    }

    private Long resolveUserId(Long fallbackUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            try {
                return Long.valueOf(auth.getPrincipal().toString());
            } catch (NumberFormatException ignored) {
            }
        }
        if (fallbackUserId != null) {
            return fallbackUserId;
        }
        return null;
    }
}
