package com.healthtracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wechat")
public class WeChatProperties {
    private final Mini mini = new Mini();
    private final Web web = new Web();
    private final Message message = new Message();

    public Mini getMini() {
        return mini;
    }

    public Web getWeb() {
        return web;
    }

    public Message getMessage() {
        return message;
    }

    public static class Mini {
        private String appid;
        private String secret;
        private String templateId;
        private String sleepTemplateId;
        private String exerciseTemplateId;
        private String periodTemplateId;
        private String medicationTemplateId;

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }

        public String getSleepTemplateId() {
            return sleepTemplateId;
        }

        public void setSleepTemplateId(String sleepTemplateId) {
            this.sleepTemplateId = sleepTemplateId;
        }

        public String getExerciseTemplateId() {
            return exerciseTemplateId;
        }

        public void setExerciseTemplateId(String exerciseTemplateId) {
            this.exerciseTemplateId = exerciseTemplateId;
        }

        public String getPeriodTemplateId() {
            return periodTemplateId;
        }

        public void setPeriodTemplateId(String periodTemplateId) {
            this.periodTemplateId = periodTemplateId;
        }

        public String getMedicationTemplateId() {
            return medicationTemplateId;
        }

        public void setMedicationTemplateId(String medicationTemplateId) {
            this.medicationTemplateId = medicationTemplateId;
        }
    }

    public static class Web {
        private String appid;
        private String secret;
        private String redirectUri;

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
    }

    public static class Message {
        private String token;
        private String aesKey;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getAesKey() {
            return aesKey;
        }

        public void setAesKey(String aesKey) {
            this.aesKey = aesKey;
        }
    }
}
