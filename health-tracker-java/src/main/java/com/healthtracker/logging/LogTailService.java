package com.healthtracker.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LogTailService {
    private static final Logger log = LoggerFactory.getLogger(LogTailService.class);
    private final Path logPath;

    public LogTailService(@Value("${logging.file.name:logs/app.log}") String logFile) {
        this.logPath = Paths.get(logFile);
        try {
            Path parent = logPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(logPath)) {
                Files.createFile(logPath);
            }
        } catch (IOException ex) {
            log.warn("Log file init failed: {}", logPath, ex);
        }
    }

    public List<String> tail(int lines) throws IOException {
        if (lines <= 0) {
            return Collections.emptyList();
        }
        if (!Files.exists(logPath)) {
            return Collections.emptyList();
        }
        List<String> all = Files.readAllLines(logPath, StandardCharsets.UTF_8);
        if (all.isEmpty()) {
            return all;
        }
        int from = Math.max(0, all.size() - lines);
        return new ArrayList<>(all.subList(from, all.size()));
    }
}
