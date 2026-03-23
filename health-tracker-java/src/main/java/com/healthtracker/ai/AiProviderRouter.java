package com.healthtracker.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiProviderRouter {
    private final Map<String, AiProvider> providerMap;
    private final String activeProvider;

    public AiProviderRouter(List<AiProvider> providers,
                            @Value("${ai.provider:yuanqi}") String activeProvider) {
        this.providerMap = new HashMap<>();
        for (AiProvider provider : providers) {
            providerMap.put(provider.getName().toLowerCase(Locale.ROOT), provider);
        }
        this.activeProvider = activeProvider == null ? "yuanqi" : activeProvider;
    }

    public AiProvider current() {
        AiProvider provider = providerMap.get(activeProvider.toLowerCase(Locale.ROOT));
        if (provider == null) {
            throw new IllegalArgumentException("未找到 AI 提供方配置: " + activeProvider);
        }
        return provider;
    }

    public String getActiveProvider() {
        return activeProvider;
    }
}
