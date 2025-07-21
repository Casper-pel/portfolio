package de.thro.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parser für PDF-Angebotsdokumente.
 * Extrahiert relevante Informationen aus einer PDF-Datei und konvertiert sie in ein JSON-Format.
 * Nutzt PDFBox für das Auslesen und Jackson für die JSON-Erstellung.
 */
public class PdfParser {

    private static final Logger logger = LoggerFactory.getLogger(PdfParser.class);

    private final Path path;

    private static final Pattern ABLAUF_DATUM =
            Pattern.compile("gültig bis zum (\\d{2}\\.\\d{2}\\.\\d{4})");

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Erstellt einen neuen PdfParser für die angegebene PDF-Datei.
     *
     * @param path Pfad zur PDF-Datei
     */
    public PdfParser(Path path) {
        this.path = path;
    }

    /**
     * Liest die PDF-Datei und konvertiert deren Inhalt in einen JSON-String.
     *
     * @return JSON-String mit extrahierten Angebotsdaten
     * @throws IOException bei Fehlern beim Lesen der PDF
     * @throws PdfReadException bei Fehlern beim Parsen der PDF
     */
    public String pdfToString() throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true); // ← ganz wichtig!
            String text = stripper.getText(document);
            String json = convertToJson(text);
            return json;
        }catch (Exception e){
            logger.error("Error reading PDF file.", e);
            throw new PdfReadException("Error reading PDF file: " + path, e);
        }
    }

    /**
     * Konvertiert den extrahierten Text aus der PDF in ein JSON-Format.
     *
     * @param text Extrahierter Text aus der PDF
     * @return JSON-String mit Angebotsdaten
     */
    public String convertToJson(String text) {
        List<String> lines = text.lines().collect(Collectors.toList());
        logger.info("line: {}", lines);
        String companyName = getLine(lines, 1);
        String addressStreet = getLine(lines, 2).split(" ")[0];
        String addressHouseNumber = getLine(lines,2).split(" ")[1];
        String postCode = getLine(lines,3).split(" ")[0];
        String city = getLine(lines,3).split(" ")[1];
        String phone = getLine(lines,4).split(":")[1].trim();
        String mail = getLine(lines,5).split(":")[1].trim();

        String offerNumber = getLine(lines, 11).split(" ")[1].trim();
        String offerDate =  getLine(lines,12).split(" ")[1].trim();
//        String offerDate = text.lines().filter(line -> line.contains("Datum: ")).findFirst().orElse("");
        String totalPriceString = text.lines()
                .filter(l -> l.contains("Gesamtpreis:"))
                .findFirst().orElse("").split(":")[1].trim().split(" ")[0];
        BigDecimal totalPrice = new BigDecimal(totalPriceString);
        Matcher validationDateMatcher = ABLAUF_DATUM.matcher(text);
        String validTillDate = validationDateMatcher.find() ? validationDateMatcher.group(1) : "";

        ObjectNode json = mapper.createObjectNode();
        json.put("companyName", companyName);
        json.put("addressStreet", addressStreet);
        json.put("addressHouseNumber", addressHouseNumber);
        json.put("postCode", postCode);
        json.put("city", city);
        json.put("phone", phone);
        json.put("mail", mail);
        json.put("offerNumber", offerNumber);
        json.put("offerDate", offerDate);
        json.put("totalPrice", totalPrice);
        json.put("validTillDate", validTillDate);

        ArrayNode invoiceItems = mapper.createArrayNode();
        text.lines().filter(line -> line.trim().matches("^B\\d+\\b.*"))
                .forEach(line -> {
                    Pattern pattern = Pattern.compile("^B(\\d+)\\b.*");
                    Matcher matcher = pattern.matcher(line);
                    String posNumber = matcher.matches() ? matcher.group(1) : "";
                    String[] parts = line.split("\\s+");
                    ObjectNode item = mapper.createObjectNode();
                    item.put("posNumber", Integer.parseInt(posNumber));
                    item.put("description", String.join("", Arrays.copyOfRange(parts, 1, parts.length - 2)));
                    item.put("amount", Integer.parseInt(parts[parts.length - 2]));
                    item.put("price", new BigDecimal(parts[parts.length - 1]));
                    invoiceItems.add(item);
                });

        json.set("invoiceItems", invoiceItems);

        try{
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        }catch(Exception e){
            logger.error("Error serializing JSON.", e);
            throw new RuntimeException("JSON serialization error", e);
        }
    }

    /**
     * Gibt die Zeile mit dem angegebenen Index aus der Liste zurück.
     *
     * @param lines Liste der Textzeilen
     * @param index Index der gewünschten Zeile
     * @return Inhalt der Zeile oder ein leerer String, falls ungültig
     */
    public String getLine(List<String> lines, int index){
        return (index >= 0 && index < lines.size()) ? lines.get(index) : "";
    }
}


