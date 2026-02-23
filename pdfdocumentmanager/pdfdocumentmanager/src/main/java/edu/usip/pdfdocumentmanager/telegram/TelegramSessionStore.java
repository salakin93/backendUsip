package edu.usip.pdfdocumentmanager.telegram;

import edu.usip.pdfdocumentmanager.model.Role;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelegramSessionStore {

    public record Session(
            String phone,
            String jwt,
            Set<Role> roles,
            BotState state,
            Map<String, String> temp,
            byte[] pendingPdfBytes,
            String pendingPdfFileName
    ) {
    }

    private final Map<Long, Session> sessions = new ConcurrentHashMap<>();

    public Optional<Session> get(Long telegramUserId) {
        return Optional.ofNullable(sessions.get(telegramUserId));
    }

    public void put(Long telegramUserId, Session session) {
        sessions.put(telegramUserId, session);
    }

    public boolean isLoggedIn(Long telegramUserId) {
        return sessions.containsKey(telegramUserId);
    }

    public void setState(Long telegramUserId, BotState state) {
        Session s = sessions.get(telegramUserId);
        if (s == null) return;
        sessions.put(telegramUserId, new Session(
                s.phone(), s.jwt(), s.roles(), state, s.temp(), s.pendingPdfBytes(), s.pendingPdfFileName()
        ));
    }

    public void putTemp(Long telegramUserId, String key, String value) {
        Session s = sessions.get(telegramUserId);
        if (s == null) return;
        Map<String, String> copy = new HashMap<>(s.temp());
        copy.put(key, value);
        sessions.put(telegramUserId, new Session(
                s.phone(), s.jwt(), s.roles(), s.state(), copy, s.pendingPdfBytes(), s.pendingPdfFileName()
        ));
    }

    public void setPendingPdf(Long telegramUserId, byte[] bytes, String fileName) {
        Session s = sessions.get(telegramUserId);
        if (s == null) return;
        sessions.put(telegramUserId, new Session(
                s.phone(), s.jwt(), s.roles(), s.state(), s.temp(), bytes, fileName
        ));
    }

    public void clearTempAndPdf(Long telegramUserId) {
        Session s = sessions.get(telegramUserId);
        if (s == null) return;
        sessions.put(telegramUserId, new Session(
                s.phone(), s.jwt(), s.roles(), BotState.IDLE, new HashMap<>(), null, null
        ));
    }

    public void clearTempOnly(Long telegramUserId) {
        Session s = sessions.get(telegramUserId);
        if (s == null) return;

        sessions.put(telegramUserId, new Session(
                s.phone(),
                s.jwt(),
                s.roles(),
                s.state(),              // ✅ mantiene estado
                new HashMap<>(),        // ✅ limpia temp
                s.pendingPdfBytes(),    // ✅ mantiene pdf si no quieres tocarlo
                s.pendingPdfFileName()
        ));
    }

    public void clearTempAndPdfKeepState(Long telegramUserId) {
        Session s = sessions.get(telegramUserId);
        if (s == null) return;

        sessions.put(telegramUserId, new Session(
                s.phone(), s.jwt(), s.roles(),
                s.state(), new HashMap<>(),
                null, null
        ));
    }

    public void clearSession(Long telegramUserId) {
        sessions.remove(telegramUserId);
    }


}
