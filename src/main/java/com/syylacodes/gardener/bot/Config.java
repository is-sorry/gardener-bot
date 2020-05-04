package com.syylacodes.gardener.bot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "bot")
@Data
@Component
public class Config {
    private String token;
    private String prefix;
    private Long emoji;
}
