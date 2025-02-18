package edu.usip.library.Chat.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Locale;
import java.util.Map;

public class SpanishDateDeserializer extends JsonDeserializer<LocalDate> {
    private static final Map<String, String> MONTHS_MAP = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("Enero", "01"),
            new AbstractMap.SimpleEntry<>("Febrero", "02"),
            new AbstractMap.SimpleEntry<>("Marzo", "03"),
            new AbstractMap.SimpleEntry<>("Abril", "04"),
            new AbstractMap.SimpleEntry<>("Mayo", "05"),
            new AbstractMap.SimpleEntry<>("Junio", "06"),
            new AbstractMap.SimpleEntry<>("Julio", "07"),
            new AbstractMap.SimpleEntry<>("Agosto", "08"),
            new AbstractMap.SimpleEntry<>("Septiembre", "09"),
            new AbstractMap.SimpleEntry<>("Octubre", "10"),
            new AbstractMap.SimpleEntry<>("Noviembre", "11"),
            new AbstractMap.SimpleEntry<>("Diciembre", "12")
    );

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText().toLowerCase(); // Convertimos a minúsculas
        String[] parts = dateStr.split(" ");
        if (parts.length != 2) {
            throw new IOException("Formato de fecha inválido: " + dateStr);
        }

        String spanishMonth = parts[0];
        String year = parts[1];

        String englishMonth = MONTHS_MAP.get(spanishMonth);
        if (englishMonth == null) {
            throw new IOException("Mes inválido: " + spanishMonth);
        }

        String formattedDate = englishMonth + " " + year;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
        return LocalDate.parse(formattedDate, formatter).withDayOfMonth(1); // Asigna el primer día del mes
    }
}