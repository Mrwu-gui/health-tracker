package com.healthtracker.controller;

import com.healthtracker.logging.LogTailService;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs")
public class AdminLogController {
    private final LogTailService logTailService;

    public AdminLogController(LogTailService logTailService) {
        this.logTailService = logTailService;
    }

    @GetMapping("/tail")
    public Map<String, Object> tail(@RequestParam(defaultValue = "200") int lines) throws IOException {
        List<String> content = logTailService.tail(lines);
        Map<String, Object> res = new HashMap<>();
        res.put("lines", content);
        res.put("size", content.size());
        return res;
    }
}
