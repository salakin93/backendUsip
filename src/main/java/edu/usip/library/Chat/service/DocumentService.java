package edu.usip.library.Chat.service;

import edu.usip.library.Chat.model.Document;
import edu.usip.library.Chat.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentService {
    @Autowired
    private DocumentRepository documentRepository;

    public List<Document> findByDegree(String degree) {
        return documentRepository.findByDegreeContainingIgnoreCase(degree);
    }

    public List<Document> findByTitle(String title) {
        return documentRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Document> findByAuthor(String author) {
        return documentRepository.findByAuthorContainingIgnoreCase(author);
    }

    public List<Document> findByDegreeTitleAndAuthor(String degree, String title, String author) {
        return documentRepository.findByDegreeContainingIgnoreCaseAndTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(degree, title, author);
    }

    public void save(Document document) {
        documentRepository.save(document);
    }

    public Document fillDocumentFromContent(String content) {
        // Crear un objeto Document
        Document document = new Document();

        // Expresiones regulares para extraer cada valor
        String degreePattern = "Degree:\\s*(.*?)\\s*\\n";
        String authorPattern = "Author:\\s*(.*?)\\s*\\n";
        String titlePattern = "Title:\\s*(.*?)\\s*\\n";
        String defensePattern = "Defense Date:\\s*(.*?)\\s*(?=\n)";

        // Buscar el grado, autor, título y fecha de defensa usando expresiones regulares
        document.setDegree(extractValue(content, degreePattern));
        document.setAuthor(extractValue(content, authorPattern));
        document.setTitle(extractValue(content, titlePattern));

        // Convertir la fecha de defensa en LocalDate (suponiendo el formato "MMM yyyy")
        String defenseDateString = extractValue(content, defensePattern);
        if (!defenseDateString.isEmpty()) {
            document.setDefense(parseDefenseDate(defenseDateString));
        } else {
            document.setDefense(LocalDate.of(2000, 1, 1));  // O asigna una fecha por defecto si lo prefieres
        }

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
}
