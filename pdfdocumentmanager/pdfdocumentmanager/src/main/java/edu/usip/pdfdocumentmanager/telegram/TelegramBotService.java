package edu.usip.pdfdocumentmanager.telegram;

import edu.usip.pdfdocumentmanager.dto.request.UserRequest;
import edu.usip.pdfdocumentmanager.dto.response.DocumentResponse;
import edu.usip.pdfdocumentmanager.model.Document;
import edu.usip.pdfdocumentmanager.model.Role;
import edu.usip.pdfdocumentmanager.security.AuthenticationService;
import edu.usip.pdfdocumentmanager.security.JwtUtil;
import edu.usip.pdfdocumentmanager.service.DocumentService;
import edu.usip.pdfdocumentmanager.telegram.dto.TelegramUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TelegramBotService {

    private final TelegramApiClient telegramApiClient;
    private final TelegramSessionStore sessionStore;
    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;
    private final BackendApiClient backendApiClient;
    private final DocumentService documentService;

    public void handleUpdate(TelegramUpdate update) {
        if (update == null) return;

        // Callbacks (botones)
        if (update.callback_query() != null) {
            handleCallback(update);
            return;
        }

        if (update.message() == null || update.message().chat() == null || update.message().from() == null) return;

        Long chatId = update.message().chat().id();
        Long telegramUserId = update.message().from().id();

        // Contact (login)
        if (update.message().contact() != null) {
            handleContactLogin(update, chatId, telegramUserId);
            return;
        }

        // Si no está logueado -> pedir contacto
        if (!sessionStore.isLoggedIn(telegramUserId)) {
            telegramApiClient.sendMessage(
                    chatId,
                    "Hola 👋 Para continuar necesito tu número.\nPulsa: 📲 **Compartir mi número**",
                    requestContactKeyboard()
            );
            return;
        }

        // Si está logueado, procesar según estado
        TelegramSessionStore.Session session = sessionStore.get(telegramUserId).orElseThrow();

        // Si llega documento y estamos esperando PDF
        if (update.message().document() != null) {
            handleDocumentUploadStep(update, chatId, telegramUserId, session);
            return;
        }

        // Texto
        String text = update.message().text();
        if (text == null) {
            telegramApiClient.sendMessage(chatId, "Envía texto o un PDF según el menú.");
            return;
        }

        handleTextByState(chatId, telegramUserId, session, text);
    }

    // ---------- LOGIN ----------
    private void handleContactLogin(TelegramUpdate update, Long chatId, Long telegramUserId) {
        try {
            var msg = update.message();

            // Validación fuerte del contacto
            if (msg.from() == null || msg.contact().user_id() == null || !msg.contact().user_id().equals(msg.from().id())) {
                telegramApiClient.sendMessage(chatId, "❌ Debes compartir tu contacto usando el botón.");
                return;
            }

            String phone = normalizePhone(msg.contact().phone_number());
            String jwt = authenticationService.authenticate(phone);
            Set<Role> roles = jwtUtil.extractRoles(jwt);

            sessionStore.put(telegramUserId, new TelegramSessionStore.Session(
                    phone, jwt, roles, BotState.IDLE, new HashMap<>(), null, null
            ));

            boolean isAdmin = roles.contains(Role.ROLE_ADMIN);
            telegramApiClient.sendMessage(chatId,
                    "✅ Login exitoso.\nRol: " + (isAdmin ? Role.ROLE_ADMIN.getDisplayName() : Role.ROLE_STUDENT.getDisplayName()) + "\n\nSelecciona una opción:",
                    isAdmin ? adminMenu() : studentMenu()
            );

        } catch (Exception e) {
            telegramApiClient.sendMessage(chatId, "❌ Login falló: " + e.getMessage());
        }
    }

    // ---------- CALLBACKS (MENÚ) ----------
    private void handleCallback(TelegramUpdate update) {
        var cb = update.callback_query();
        telegramApiClient.answerCallback(cb.id());

        Long chatId = cb.message().chat().id();
        Long telegramUserId = cb.from().id();
        String data = cb.data();

        var sessionOpt = sessionStore.get(telegramUserId);
        if (sessionOpt.isEmpty()) {
            telegramApiClient.sendMessage(chatId, "⚠️ No estás autenticado. Escribe cualquier mensaje para iniciar.");
            return;
        }

        var session = sessionOpt.get();
        boolean isAdmin = session.roles().contains(Role.ROLE_ADMIN);

        switch (data) {
            case "ADMIN_UPLOAD" -> {
                if (!isAdmin) {
                    telegramApiClient.sendMessage(chatId, "❌ No tienes permisos.");
                    return;
                }
                sessionStore.setState(telegramUserId, BotState.ADMIN_WAITING_PDF);
                telegramApiClient.sendMessage(chatId, "📤 Envía ahora el archivo PDF (como documento).");
            }
            case "SEARCH" -> {
                sessionStore.setState(telegramUserId, BotState.WAITING_SEARCH_QUERY);
                telegramApiClient.sendMessage(chatId, "🔎 Escribe el criterio de búsqueda (título/autor/carrera).");
            }
            case "ADMIN_USERS" -> {
                if (!isAdmin) {
                    telegramApiClient.sendMessage(chatId, "❌ No tienes permisos.");
                    return;
                }
                telegramApiClient.sendMessage(chatId, "👥 Gestión de usuarios:", adminUsersMenu());
            }
            case "USER_CREATE" -> {
                if (!isAdmin) {
                    telegramApiClient.sendMessage(chatId, "❌ No tienes permisos.");
                    return;
                }
                sessionStore.clearTempOnly(telegramUserId);
                sessionStore.setState(telegramUserId, BotState.ADMIN_WAITING_NEWUSER_NAME);
                telegramApiClient.sendMessage(chatId, "👤 Crear usuario\nEnvía el **nombre** del nuevo usuario:");
            }
            case "USER_DISABLE" -> {
                if (!isAdmin) {
                    telegramApiClient.sendMessage(chatId, "❌ No tienes permisos.");
                    return;
                }
                sessionStore.setState(telegramUserId, BotState.ADMIN_WAITING_DISABLE_PHONE);
                telegramApiClient.sendMessage(chatId, "⛔ Deshabilitar usuario\nEnvía el **teléfono** a deshabilitar:");
            }

            // ✅ VOLVER AL MENÚ
            case "BACK_MAIN" -> telegramApiClient.sendMessage(chatId, "Menú:", isAdmin ? adminMenu() : studentMenu());

            // ✅ ROLES POR BOTÓN
            case "ROLE_ADMIN" -> {
                if (!isAdmin) {
                    telegramApiClient.sendMessage(chatId, "❌ No tienes permisos.");
                    return;
                }
                handleRolePick(chatId, telegramUserId, session, Role.ROLE_ADMIN);
            }
            case "ROLE_STUDENT" -> {
                if (!isAdmin) {
                    telegramApiClient.sendMessage(chatId, "❌ No tienes permisos.");
                    return;
                }
                handleRolePick(chatId, telegramUserId, session, Role.ROLE_STUDENT);
            }

            case "HELP" -> telegramApiClient.sendMessage(chatId, helpText(isAdmin));
            case "CHATPDF_EXIT" -> {
                sessionStore.setState(telegramUserId, BotState.IDLE);
                sessionStore.clearTempOnly(telegramUserId); // limpia docId

                telegramApiClient.sendMessage(
                        chatId,
                        "✅ Saliste del modo chat.",
                        isAdmin ? adminMenu() : studentMenu()
                );
            }

            default -> {
                if (data != null && data.startsWith("DOC_DL:")) {
                    Long docId = Long.parseLong(data.substring("DOC_DL:".length()));
                    handleDownloadDocument(chatId, telegramUserId, docId);
                    return;
                }
                if (data != null && data.startsWith("DOC_CHAT:")) {
                    Long docId = Long.parseLong(data.substring("DOC_CHAT:".length()));
                    startChatPdf(chatId, telegramUserId, docId);
                    return;
                }
                telegramApiClient.sendMessage(chatId, "Opción no reconocida.");
            }

        }
    }

    private void handleRolePick(Long chatId, Long telegramUserId, TelegramSessionStore.Session session, Role role) {
        // Solo válido si estamos en el estado esperado
        if (session.state() != BotState.ADMIN_WAITING_NEWUSER_ROLE) {
            telegramApiClient.sendMessage(chatId, "⚠️ No estoy en el flujo de creación de usuarios.");
            return;
        }

        try {
            var s = sessionStore.get(telegramUserId).orElseThrow();

            UserRequest req = new UserRequest();
            req.setName(s.temp().get("newUserName"));
            req.setPhone(s.temp().get("newUserPhone"));
            req.setRole(role);

            var created = backendApiClient.createUser(s.jwt(), req);
            telegramApiClient.sendMessage(chatId,
                    "✅ Usuario creado: " + created.getName() +
                            " (" + created.getRole().getDisplayName() + ")");
        } catch (BackendUnauthorizedException e) {
            forceReLogin(chatId, telegramUserId);
            return;
        } catch (Exception e) {
            telegramApiClient.sendMessage(chatId, "❌ Error creando usuario: " + e.getMessage());
        } finally {
            sessionStore.setState(telegramUserId, BotState.IDLE);
            telegramApiClient.sendMessage(chatId, "Menú:", adminMenu());
        }
    }

    // ---------- TEXTO SEGÚN ESTADO ----------
    private void handleTextByState(Long chatId, Long telegramUserId, TelegramSessionStore.Session session, String text) {
        boolean isAdmin = session.roles().contains(Role.ROLE_ADMIN);

        switch (session.state()) {

            // BUSQUEDA
            case WAITING_SEARCH_QUERY -> {

                var parsed = parseSearchQuery(text);

                sessionStore.putTemp(telegramUserId, "search_title",
                        parsed.getOrDefault("title", ""));

                sessionStore.putTemp(telegramUserId, "search_author",
                        parsed.getOrDefault("author", ""));

                sessionStore.putTemp(telegramUserId, "search_degree",
                        parsed.getOrDefault("degree", ""));

                sessionStore.putTemp(telegramUserId, "search_page", "0");
                sessionStore.putTemp(telegramUserId, "search_size", "5");

                showSearchPage(chatId, telegramUserId);
                return;
            }

            case SEARCH_PREV -> {
                var ss = sessionStore.get(telegramUserId).orElseThrow();
                int p = Integer.parseInt(ss.temp().getOrDefault("search_page", "0"));
                if (p > 0) sessionStore.putTemp(telegramUserId, "search_page", String.valueOf(p - 1));
                showSearchPage(chatId, telegramUserId);
            }
            case SEARCH_NEXT -> {
                var ss = sessionStore.get(telegramUserId).orElseThrow();
                int p = Integer.parseInt(ss.temp().getOrDefault("search_page", "0"));
                int total = Integer.parseInt(ss.temp().getOrDefault("search_totalPages", "1"));
                if (p < total - 1) sessionStore.putTemp(telegramUserId, "search_page", String.valueOf(p + 1));
                showSearchPage(chatId, telegramUserId);
            }
            case SEARCH_NEW -> {
                sessionStore.setState(telegramUserId, BotState.WAITING_SEARCH_QUERY);
                telegramApiClient.sendMessage(chatId,
                        "🔎 Nueva búsqueda.\n" +
                                "Puedes usar:\n" +
                                "• t: texto (título)\n" +
                                "• a: texto (autor)\n" +
                                "• d: texto (carrera)\n\n" +
                                "Ej: a: Juan");
            }
            case BACK_MAIN -> {
                telegramApiClient.sendMessage(chatId, "Menú:", isAdmin ? adminMenu() : studentMenu());
                sessionStore.setState(telegramUserId, BotState.IDLE);
            }

            // UPLOAD: metadata
            case ADMIN_WAITING_TITLE -> {
                sessionStore.putTemp(telegramUserId, "title", text);
                sessionStore.setState(telegramUserId, BotState.ADMIN_WAITING_AUTHOR);
                telegramApiClient.sendMessage(chatId, "**Autor:**");
            }
            case ADMIN_WAITING_AUTHOR -> {
                sessionStore.putTemp(telegramUserId, "author", text);
                sessionStore.setState(telegramUserId, BotState.ADMIN_WAITING_DEGREE);
                telegramApiClient.sendMessage(chatId, "**Carrera:**");
            }
            case ADMIN_WAITING_DEGREE -> {
                sessionStore.putTemp(telegramUserId, "degree", text);
                sessionStore.setState(telegramUserId, BotState.ADMIN_WAITING_DEFENSE_DATE);
                telegramApiClient.sendMessage(chatId, "**Fecha de defensa (YYYY-MM-DD):**");
            }
            case ADMIN_WAITING_DEFENSE_DATE -> {
                // ✅ validar formato ISO
                try {
                    LocalDate.parse(text.trim());
                } catch (Exception e) {
                    telegramApiClient.sendMessage(chatId, "Formato inválido. Usa YYYY-MM-DD. Ej: 2024-11-15");
                    return;
                }
                sessionStore.putTemp(telegramUserId, "defenseDate", text.trim());
                finalizeUpload(chatId, telegramUserId);
            }

            // USERS: create user flow
            case ADMIN_WAITING_NEWUSER_NAME -> {
                if (!isAdmin) {
                    telegramApiClient.sendMessage(chatId, "❌ No tienes permisos.");
                    return;
                }
                sessionStore.putTemp(telegramUserId, "newUserName", text);
                sessionStore.setState(telegramUserId, BotState.ADMIN_WAITING_NEWUSER_PHONE);
                telegramApiClient.sendMessage(chatId, "Teléfono del nuevo usuario:");
            }
            case ADMIN_WAITING_NEWUSER_PHONE -> {
                if (!isAdmin) {
                    telegramApiClient.sendMessage(chatId, "❌ No tienes permisos.");
                    return;
                }
                sessionStore.putTemp(telegramUserId, "newUserPhone", normalizePhone(text));
                sessionStore.setState(telegramUserId, BotState.ADMIN_WAITING_NEWUSER_ROLE);

                // ✅ ahora elegimos rol por botones
                telegramApiClient.sendMessage(chatId, "Selecciona el rol:", roleMenu());
            }

            // USERS: disable
            case ADMIN_WAITING_DISABLE_PHONE -> {
                if (!isAdmin) {
                    telegramApiClient.sendMessage(chatId, "❌ No tienes permisos.");
                    return;
                }
                try {
                    backendApiClient.disableUser(session.jwt(), normalizePhone(text));
                    telegramApiClient.sendMessage(chatId, "✅ Usuario deshabilitado.");
                } catch (BackendUnauthorizedException e) {
                    forceReLogin(chatId, telegramUserId);
                    return;
                } catch (Exception e) {
                    telegramApiClient.sendMessage(chatId, "❌ Error deshabilitando: " + e.getMessage());
                } finally {
                    sessionStore.setState(telegramUserId, BotState.IDLE);
                    telegramApiClient.sendMessage(chatId, "Menú:", adminMenu());
                }
            }
            case CHATPDF_CHATTING -> {

                // salir
                if (text != null && text.trim().equalsIgnoreCase("/salir")) {
                    sessionStore.setState(telegramUserId, BotState.IDLE);
                    telegramApiClient.sendMessage(chatId, "✅ Saliste del modo chat.", isAdmin ? adminMenu() : studentMenu());
                    return;
                }

                String docIdStr = session.temp().get("chatpdf_docId");
                if (docIdStr == null || docIdStr.isBlank()) {
                    telegramApiClient.sendMessage(chatId, "⚠️ No hay documento activo. Vuelve a buscar y presiona 💬 Chatear.");
                    sessionStore.setState(telegramUserId, BotState.IDLE);
                    return;
                }

                Long docId = Long.parseLong(docIdStr);

                try {
                    var resp = backendApiClient.chatWithChatPdf(session.jwt(), docId, text, true);

                    // ✅ mostrar respuesta
                    telegramApiClient.sendMessage(chatId, resp.getAnswer());

                    // (Opcional) mostrar referencias
                    if (resp.getReferences() != null && !resp.getReferences().isEmpty()) {

                        // Necesitamos datos del documento
                        Document document = documentService.getById(docId);

                        String author = formatAuthorApa(document.getAuthor());
                        String year = String.valueOf(document.getDefenseDate().getYear());

                        StringBuilder refs = new StringBuilder();
                        refs.append("📚 Referencias:\n\n");

                        resp.getReferences().stream()
                                .limit(5)
                                .forEach(r -> {
                                    if (r.getPageNumber() != null) {
                                        refs.append("• (")
                                                .append(author)
                                                .append(", ")
                                                .append(year)
                                                .append(", p. ")
                                                .append(r.getPageNumber())
                                                .append(")\n");
                                    }
                                });

                        telegramApiClient.sendMessage(
                                chatId,
                                refs.toString(),
                                chatPdfExitMenu()
                        );
                    }
                } catch (BackendUnauthorizedException e) {
                    forceReLogin(chatId, telegramUserId);
                } catch (Exception e) {
                    telegramApiClient.sendMessage(chatId, "❌ Error preguntando al documento: " + e.getMessage());
                }
            }


            default ->
                    telegramApiClient.sendMessage(chatId, "Usa el menú para continuar.", isAdmin ? adminMenu() : studentMenu());
        }
    }

    // ---------- PDF RECEPCIÓN ----------
    private void handleDocumentUploadStep(TelegramUpdate update, Long chatId, Long telegramUserId, TelegramSessionStore.Session session) {
        if (!session.roles().contains(Role.ROLE_ADMIN)) {
            telegramApiClient.sendMessage(chatId, "❌ Solo ADMIN puede subir documentos.");
            return;
        }
        if (session.state() != BotState.ADMIN_WAITING_PDF) {
            telegramApiClient.sendMessage(chatId, "Recibí un documento, pero no estoy en modo subida. Presiona 📤 Subir PDF.");
            return;
        }

        var doc = update.message().document();
        if (doc.mime_type() == null || !doc.mime_type().equalsIgnoreCase("application/pdf")) {
            telegramApiClient.sendMessage(chatId, "❌ Solo se permiten PDFs.");
            return;
        }
        if (doc.file_size() != null && doc.file_size() > 35 * 1024 * 1024) {
            telegramApiClient.sendMessage(chatId, "❌ El PDF supera 35MB.");
            return;
        }

        try {
            String filePath = telegramApiClient.getFilePath(doc.file_id());
            byte[] bytes = telegramApiClient.downloadFileBytes(filePath);

            sessionStore.setPendingPdf(telegramUserId, bytes, doc.file_name());
            sessionStore.setState(telegramUserId, BotState.ADMIN_WAITING_TITLE);

            telegramApiClient.sendMessage(chatId, "✅ PDF recibido.\nAhora envía el **Título**:");
        } catch (Exception e) {
            telegramApiClient.sendMessage(chatId, "❌ No pude descargar el PDF desde Telegram: " + e.getMessage());
        }
    }

    // ---------- FINALIZAR UPLOAD ----------
    private void finalizeUpload(Long chatId, Long telegramUserId) {
        var s = sessionStore.get(telegramUserId).orElseThrow();
        try {
            String title = s.temp().get("title");
            String author = s.temp().get("author");
            String degree = s.temp().get("degree");
            String defenseDate = s.temp().get("defenseDate");

            var created = backendApiClient.uploadDocument(
                    s.jwt(),
                    title, author, degree, defenseDate,
                    s.pendingPdfBytes(),
                    s.pendingPdfFileName()
            );

            telegramApiClient.sendMessage(chatId,
                    "✅ Documento subido.\nID: " + created.getId() +
                            "\nTítulo: " + created.getTitle() +
                            "\nDescarga: " + created.getDownloadUrl()
            );
        } catch (BackendUnauthorizedException e) {
            forceReLogin(chatId, telegramUserId);
            return;
        } catch (Exception e) {
            telegramApiClient.sendMessage(chatId, "❌ Error subiendo al backend: " + e.getMessage());
        } finally {
            sessionStore.clearTempAndPdf(telegramUserId);
            telegramApiClient.sendMessage(chatId, "Menú:", adminMenu());
        }
    }

    // ---------- MENÚS ----------
    private Map<String, Object> adminMenu() {
        return Map.of("inline_keyboard", List.of(
                List.of(
                        Map.of("text", "📤 Subir PDF", "callback_data", "ADMIN_UPLOAD"),
                        Map.of("text", "📄 Buscar", "callback_data", "SEARCH")
                ),
                List.of(
                        Map.of("text", "👥 Usuarios", "callback_data", "ADMIN_USERS"),
                        Map.of("text", "❓ Ayuda", "callback_data", "HELP")
                )
        ));
    }

    private Map<String, Object> studentMenu() {
        return Map.of("inline_keyboard", List.of(
                List.of(
                        Map.of("text", "📄 Buscar", "callback_data", "SEARCH"),
                        Map.of("text", "❓ Ayuda", "callback_data", "HELP")
                )
        ));
    }

    private Map<String, Object> adminUsersMenu() {
        return Map.of("inline_keyboard", List.of(
                List.of(
                        Map.of("text", "➕ Crear usuario", "callback_data", "USER_CREATE"),
                        Map.of("text", "⛔ Deshabilitar", "callback_data", "USER_DISABLE")
                ),
                List.of(
                        Map.of("text", "⬅️ Volver", "callback_data", "BACK_MAIN")
                )
        ));
    }

    private Map<String, Object> roleMenu() {
        return Map.of("inline_keyboard", List.of(
                List.of(
                        Map.of("text", Role.ROLE_ADMIN.getDisplayName(), "callback_data", "ROLE_ADMIN"),
                        Map.of("text", Role.ROLE_STUDENT.getDisplayName(), "callback_data", "ROLE_STUDENT")
                )
        ));
    }

    private Map<String, Object> requestContactKeyboard() {
        return Map.of(
                "keyboard", List.of(
                        List.of(Map.of("text", "📲 **Compartir mi número**", "request_contact", true))
                ),
                "resize_keyboard", true,
                "one_time_keyboard", true
        );
    }

    private String helpText(boolean isAdmin) {
        if (isAdmin) {
            return """
                    Ayuda (ADMIN)
                    • 📤 Subir PDF: envía PDF y metadata
                    • 📄 Buscar: busca en el repositorio
                    • 👥 Usuarios: crear o deshabilitar usuarios
                    """;
        }
        return """
                Ayuda (STUDENT)
                • 📄 Buscar: busca en el repositorio
                """;
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String p = phone.replaceAll("[^0-9+]", "");
        if (p.startsWith("+591")) p = p.substring(4);
        if (p.startsWith("591") && p.length() >= 11) p = p.substring(3);
        return p.replace("+", "");
    }

    private void forceReLogin(Long chatId, Long telegramUserId) {
        sessionStore.clearSession(telegramUserId);
        telegramApiClient.sendMessage(
                chatId,
                "🔐 Tu sesión expiró o no es válida. Por favor inicia sesión nuevamente.\nPulsa: 📲 **Compartir mi número**",
                requestContactKeyboard()
        );
    }

    private Map<String, String> parseSearchQuery(String text) {

        Map<String, String> result = new HashMap<>();

        if (text == null || text.isBlank()) return result;

        text = text.trim();

        // Buscar prefijos t:, a:, d:
        String[] parts = text.split("\\s+");

        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].toLowerCase();

            if (p.startsWith("t:")) {
                result.put("title", text.substring(text.toLowerCase().indexOf("t:") + 2).trim());
            } else if (p.startsWith("a:")) {
                result.put("author", text.substring(text.toLowerCase().indexOf("a:") + 2).trim());
            } else if (p.startsWith("d:")) {
                result.put("degree", text.substring(text.toLowerCase().indexOf("d:") + 2).trim());
            }
        }

        // Si no usa prefijos → búsqueda general por título
        if (result.isEmpty()) {
            result.put("title", text);
        }

        return result;
    }


    private void showSearchPage(Long chatId, Long telegramUserId) {
        var s = sessionStore.get(telegramUserId).orElseThrow();

        String title = s.temp().get("search_title");
        String author = s.temp().get("search_author");
        String degree = s.temp().get("search_degree");

        int page = Integer.parseInt(s.temp().getOrDefault("search_page", "0"));
        int size = Integer.parseInt(s.temp().getOrDefault("search_size", "5"));

        try {
            var resp = backendApiClient.searchDocuments(s.jwt(), title, author, degree, page, size);

            // ✅ si no hay resultados
            if (resp.getContent() == null || resp.getContent().isEmpty()) {
                telegramApiClient.sendMessage(chatId, "No encontré resultados con esos criterios.");
                // NO mandes menú aquí -> evita duplicados
                sessionStore.setState(telegramUserId, BotState.IDLE);
                return;
            }

            // ✅ cache totalPages para prev/next
            sessionStore.putTemp(telegramUserId, "search_totalPages", String.valueOf(resp.getTotalPages()));

            // ✅ texto simple (sin links)
            StringBuilder sb = new StringBuilder();
            sb.append("Resultados (página ").append(resp.getNumber() + 1)
                    .append(" / ").append(resp.getTotalPages()).append(")\n\n");

            for (var d : resp.getContent()) {
                sb.append("• ").append(d.getTitle())
                        .append(" — ").append(d.getAuthor())
                        .append("\n");
            }

            // ✅ modo browsing
            sessionStore.setState(telegramUserId, BotState.SEARCH_BROWSING);

            // ✅ botones: descargar por doc + prev/next + nueva búsqueda + menú
            telegramApiClient.sendMessage(
                    chatId,
                    sb.toString(),
                    searchResultsMenu(resp.getContent(), resp.getNumber(), resp.getTotalPages())
            );

        } catch (BackendUnauthorizedException e) {
            forceReLogin(chatId, telegramUserId);
        } catch (Exception e) {
            telegramApiClient.sendMessage(chatId, "❌ Error buscando: " + e.getMessage());
            sessionStore.setState(telegramUserId, BotState.IDLE);
        }
    }

    private Map<String, Object> searchNavMenu(int currentPage, int totalPages) {
        boolean hasPrev = currentPage > 0;
        boolean hasNext = currentPage < (totalPages - 1);

        // 1 fila: prev/next
        var row = new java.util.ArrayList<Map<String, Object>>();
        if (hasPrev) row.add(Map.of("text", "⬅️ Anterior", "callback_data", "SEARCH_PREV"));
        if (hasNext) row.add(Map.of("text", "Siguiente ➡️", "callback_data", "SEARCH_NEXT"));

        // 2 fila: nuevo / salir
        var row2 = List.of(
                Map.of("text", "🔎 Nueva búsqueda", "callback_data", "SEARCH_NEW"),
                Map.of("text", "⬅️ Menú", "callback_data", "BACK_MAIN")
        );

        return Map.of("inline_keyboard", List.of(
                row.isEmpty() ? List.of(Map.of("text", "⬅️ Menú", "callback_data", "BACK_MAIN")) : row,
                row2
        ));
    }

    private Map<String, Object> searchResultsMenu(List<DocumentResponse> docs, int currentPage, int totalPages) {
        var keyboard = new java.util.ArrayList<List<Map<String, Object>>>();

        // Botón descargar por documento (1 por fila)
        for (var d : docs) {
            keyboard.add(List.of(
                    Map.of("text", "⬇️ Descargar: " + d.getTitle(), "callback_data", "DOC_DL:" + d.getId()),
                    Map.of("text", "💬 Chatear " + d.getTitle(), "callback_data", "DOC_CHAT:" + d.getId())
            ));
        }

        // Navegación
        var navRow = new java.util.ArrayList<Map<String, Object>>();
        if (currentPage > 0) navRow.add(Map.of("text", "⬅️ Anterior", "callback_data", "SEARCH_PREV"));
        if (currentPage < totalPages - 1) navRow.add(Map.of("text", "Siguiente ➡️", "callback_data", "SEARCH_NEXT"));
        if (!navRow.isEmpty()) keyboard.add(navRow);

        // Acciones
        keyboard.add(List.of(
                Map.of("text", "🔎 Nueva búsqueda", "callback_data", "SEARCH_NEW"),
                Map.of("text", "⬅️ Menú", "callback_data", "BACK_MAIN")
        ));

        return Map.of("inline_keyboard", keyboard);
    }

    private void handleDownloadDocument(Long chatId, Long telegramUserId, Long docId) {

        var s = sessionStore.get(telegramUserId).orElseThrow();

        try {
            byte[] pdfBytes = backendApiClient.downloadDocumentBytes(s.jwt(), docId);

            String fileName = "document_" + docId + ".pdf";

            telegramApiClient.sendDocument(
                    chatId,
                    pdfBytes,
                    fileName,
                    "📄 Documento ID: " + docId
            );

        } catch (BackendUnauthorizedException e) {
            forceReLogin(chatId, telegramUserId);
        } catch (Exception e) {
            telegramApiClient.sendMessage(chatId,
                    "❌ No pude descargar el documento: " + e.getMessage());
        }
    }

    private void startChatPdf(Long chatId, Long telegramUserId, Long docId) {
        var s = sessionStore.get(telegramUserId).orElseThrow();

        try {
            telegramApiClient.sendMessage(chatId, "⏳ Preparando documento para chat...");

            // 1) sube (o reutiliza) sourceId en backend
            var uploadResp = backendApiClient.uploadToChatPdf(s.jwt(), docId);

            // 2) guardamos docId en sesión (para saber con qué doc se chatea)
            sessionStore.putTemp(telegramUserId, "chatpdf_docId", String.valueOf(docId));
            sessionStore.setState(telegramUserId, BotState.CHATPDF_CHATTING);

            telegramApiClient.sendMessage(chatId,
                    "✅ Listo. Documento preparado (sourceId: " + uploadResp.getSourceId() + ").\n\n" +
                            "Ahora escribe tu pregunta.\n" +
                            "Para salir escribe: /salir");

        } catch (BackendUnauthorizedException e) {
            forceReLogin(chatId, telegramUserId);
        } catch (Exception e) {
            telegramApiClient.sendMessage(chatId, "❌ No pude iniciar el chat: " + e.getMessage());
        }
    }

    private String formatAuthorApa(String fullName) {
        if (fullName == null || fullName.isBlank()) return "Autor";

        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1]; // último como apellido
    }

    private Map<String, Object> chatPdfExitMenu() {
        return Map.of(
                "inline_keyboard", List.of(
                        List.of(
                                Map.of("text", "🔙 Salir", "callback_data", "CHATPDF_EXIT")
                        )
                )
        );
    }

}
