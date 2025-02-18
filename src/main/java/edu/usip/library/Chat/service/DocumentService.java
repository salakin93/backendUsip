package edu.usip.library.Chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.usip.library.Chat.dto.model.DocumentDTO;
import edu.usip.library.Chat.model.Document;
import edu.usip.library.Chat.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentService {
    @Autowired
    private DocumentRepository documentRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<Document> findBySummary(String summary) {
        return documentRepository.findBySummaryContainingIgnoreCase(summary);
    }

    public List<Document> findByTitle(String title) {
        return documentRepository.findByTitleContainingIgnoreCase(title);
    }

    public void save(Document document) {
        documentRepository.save(document);
    }

    public Document fillDocumentFromContent(String content) {
        DocumentDTO documentData = extractJsonFromContent(content);

        if (documentData == null) {
            throw new RuntimeException("No se pudo extraer información válida del documento.");
        }

        // Cambiar el patrón a MM-yyyy para obtener el formato "05-2024"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate defenseDate;
        try {
            // Parsear la fecha con el nuevo formato limpio
            defenseDate = LocalDate.parse("01-" + documentData.getDefense_date(), formatter);
        } catch (Exception e) {
            throw new RuntimeException("Formato de fecha inválido: " + documentData.getDefense_date(), e);
        }

        // Crear el objeto Document
        Document document = new Document();
        document.setAuthor(documentData.getAuthor());
        document.setTitle(documentData.getTitle());
        document.setDegree(documentData.getDegree());
        document.setDefense(defenseDate);  // Ponemos el primer día del mes
        document.setSummary(documentData.getSummary());

        return document;
    }

    // Método auxiliar para extraer un valor con una expresión regular
    private String extractValue(String content, String pattern) {
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim().replace("**", "");  // Extrae el valor y elimina espacios al principio y al final
        }
        return "";
    }

    // Método auxiliar para convertir la fecha en LocalDate
    private LocalDate parseDefenseDate(String dateString) {
        // Formato esperado: "Nov 2018" (mes abreviado y año)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        return LocalDate.parse(dateString, formatter);
    }

    // Método para extraer la cadena JSON dentro de "content"
    private static DocumentDTO extractJsonFromContent(String content) {
        try {
            // Buscar el JSON dentro del contenido
            int startIndex = content.indexOf("{");
            int endIndex = content.lastIndexOf("}");
            if (startIndex == -1 || endIndex == -1) {
                throw new RuntimeException("No se encontró JSON en el contenido");
            }

            String json = content.substring(startIndex, endIndex + 1);

            // Intentar convertir el JSON en un objeto DocumentDTO
            return objectMapper.readValue(json, DocumentDTO.class);
        } catch (IOException e) {
            throw new RuntimeException("Error al convertir el JSON en DocumentDTO", e);
        }
    }
}
