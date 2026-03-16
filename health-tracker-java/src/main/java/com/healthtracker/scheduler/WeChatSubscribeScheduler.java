package com.healthtracker.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtracker.entity.SubscribeTask;
import com.healthtracker.service.SubscribeTaskService;
import com.healthtracker.service.WeChatMessageService;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeChatSubscribeScheduler {
    private static final Logger log = LoggerFactory.getLogger(WeChatSubscribeScheduler.class);
    private final SubscribeTaskService subscribeTaskService;
    private final WeChatMessageService weChatMessageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeChatSubscribeScheduler(SubscribeTaskService subscribeTaskService,
                                    WeChatMessageService weChatMessageService) {
        this.subscribeTaskService = subscribeTaskService;
        this.weChatMessageService = weChatMessageService;
    }

    @Scheduled(fixedDelayString = "${wechat.message.poll-interval-ms:60000}")
    public void dispatch() {
        LocalDateTime now = LocalDateTime.now();
        for (SubscribeTask task : subscribeTaskService.listDue(now, 50)) {
            try {
                Map<String, Object> data = objectMapper.readValue(task.getDataJson(), Map.class);
                String resp = weChatMessageService.sendTemplate(task.getOpenid(),
                    task.getTemplateId(),
                    task.getPage(),
                    data);
                task.setStatus(1);
                task.setSentAt(LocalDateTime.now());
                task.setResponse(resp);
                subscribeTaskService.updateById(task);
            } catch (Exception ex) {
                log.warn("WeChat subscribe send failed taskId={}", task.getId(), ex);
                task.setStatus(2);
                task.setSentAt(LocalDateTime.now());
                task.setResponse(ex.getMessage());
                subscribeTaskService.updateById(task);
            }
        }
    }
}
