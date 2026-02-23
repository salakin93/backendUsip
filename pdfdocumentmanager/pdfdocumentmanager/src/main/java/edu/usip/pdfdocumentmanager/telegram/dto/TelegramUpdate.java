package edu.usip.pdfdocumentmanager.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramUpdate(Message message, CallbackQuery callback_query) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(
            Long message_id,
            Chat chat,
            User from,
            String text,
            Contact contact,
            Document document
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CallbackQuery(String id, User from, Message message, String data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Chat(Long id, String type) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(Long id, String first_name, String username) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Contact(String phone_number, Long user_id) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(String file_id, String file_name, String mime_type, Integer file_size) {
    }
}
