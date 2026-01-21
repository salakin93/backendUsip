package edu.usip.pdfdocumentmanager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "chatpdf")
public class ChatPdfProperties {
    /**
     * Base URL, por defecto el oficial.
     */
    private String baseUrl = "https://api.chatpdf.com/v1";

    /**
     * API Key de ChatPDF.
     */
    private String apiKey;
}
