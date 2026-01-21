package edu.usip.pdfdocumentmanager.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ChatPdfProperties.class)
public class ChatPdfConfig {
}
