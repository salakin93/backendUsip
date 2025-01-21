package edu.usip.library.Chat.controller;

import edu.usip.library.Chat.model.Document;
import edu.usip.library.Chat.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/document")
public class DocumentController {
    @Autowired
private DocumentService documentService;

    @GetMapping("/search")
    public List<Document> buscarTesis(
            @RequestParam(value = "degree", required = false) String degree,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author) {

        if (degree != null) {
            return documentService.findByDegree(degree);
        } else if (title != null) {
            return documentService.findByTitle(title);
        } else if (author != null) {
            return documentService.findByAuthor(author);
        } else {
            return documentService.findByDegreeTitleAndAuthor(degree, title, author);
        }
    }
}