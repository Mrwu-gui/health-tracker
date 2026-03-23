package com.healthtracker.ai;

public interface AiProvider {
    String getName();

    AiProviderResponse chat(AiProviderRequest request) throws Exception;
}
