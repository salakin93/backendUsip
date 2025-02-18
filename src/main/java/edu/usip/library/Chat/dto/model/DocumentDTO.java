package edu.usip.library.Chat.dto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor  // Constructor vacío para que Jackson pueda instanciar la clase
@AllArgsConstructor // Constructor con todos los argumentos (necesario para @Builder)
public class DocumentDTO {
    private String degree;
    private String author;
    private String title;
    private String summary;
    private String defense_date;
}
