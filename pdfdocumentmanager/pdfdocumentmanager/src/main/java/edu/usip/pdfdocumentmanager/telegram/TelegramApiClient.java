package edu.usip.pdfdocumentmanager.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class TelegramApiClient {

    private final RestClient api;
    private final RestClient fileApi;
    private final String token;

    public TelegramApiClient(
            @Value("${telegram.api.base:https://api.telegram.org}") String baseUrl,
            @Value("${telegram.bot.token}") String token
    ) {
        this.api = RestClient.builder().baseUrl(baseUrl).build();
        this.fileApi = RestClient.builder().baseUrl("https://api.telegram.org").build();
        this.token = token;
    }

    public void sendMessage(Long chatId, String text) {
        api.post()
                .uri("/bot{token}/sendMessage", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("chat_id", chatId, "text", text))
                .retrieve()
                .toBodilessEntity();
    }

    public void sendMessage(Long chatId, String text, Object replyMarkup) {
        api.post()
                .uri("/bot{token}/sendMessage", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("chat_id", chatId, "text", text, "reply_markup", replyMarkup))
                .retrieve()
                .toBodilessEntity();
    }

    public void answerCallback(String callbackQueryId) {
        api.post()
                .uri("/bot{token}/answerCallbackQuery", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("callback_query_id", callbackQueryId))
                .retrieve()
                .toBodilessEntity();
    }

    // getFile -> retorna file_path
    public String getFilePath(String fileId) {
        Map resp = api.get()
                .uri("/bot{token}/getFile?file_id={fileId}", token, fileId)
                .retrieve()
                .body(Map.class);

        Map result = (Map) resp.get("result");
        return (String) result.get("file_path");
    }

    public byte[] downloadFileBytes(String filePath) {
        // https://api.telegram.org/file/bot<TOKEN>/<file_path>
        return fileApi.get()
                .uri("/file/bot{token}/{path}", token, filePath)
                .retrieve()
                .body(byte[].class);
    }

    public void sendDocument(Long chatId, byte[] bytes, String fileName, String caption) {
        // Telegram requiere multipart/form-data
        var body = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        body.add("chat_id", String.valueOf(chatId));
        if (caption != null && !caption.isBlank()) body.add("caption", caption);

        body.add("document", new org.springframework.core.io.ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });

        api.post()
                .uri("/bot{token}/sendDocument", token)
                .contentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

}
