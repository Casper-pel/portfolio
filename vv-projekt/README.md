# AI-Pipeline System – Automatisierter Angebots-/Rechnungsvergleich

## Überblick

Dieses verteilte System automatisiert den Vergleich von Angeboten und Rechnungen für das fiktive Unternehmen **Tunnelgräber GmbH**. Es verarbeitet strukturierte PDF-Dateien, wandelt sie in JSON um, analysiert die Daten mit Hilfe von KI, vergleicht sie mit vorhandenen Angeboten und speichert sie in einer Datenbank. Die Architektur basiert auf Microservices und verwendet RabbitMQ zur Kommunikation.

## Architektur

Das Projekt besteht aus folgenden Komponenten:

### 🧾 DocumentImporter

- Überwacht ein konfiguriertes Verzeichnis (`PATH_OFFERS`)
- Erkennt neue oder geänderte PDF-Dateien
- Liest PDF-Dateien mit Apache PDFBox ein
- Wandelt den Inhalt in ein standardisiertes JSON-Format um
- Verarbeitet Dateien nur, wenn sie eine bestimmte maximale Größe nicht überschreiten (`MAX_FILE_SIZE`)
- Sendet die JSON-Daten über RabbitMQ an eine Message Queue
- Die Verarbeitung erfolgt über konfigurierbare Strategien:
  - `MessageBusStrategy`: Weiterleitung an RabbitMQ
  - `LoggingStrategy`: Ausgabe ins Log
- Unterstützt Konfiguration über Umgebungsvariablen (`.env`-Datei)

### 🧠 AiPipeline

- Subscribt auf die Message Queue des DocumentImporters
- Wandelt eingehende JSONs in Entitäten um:
  - Customer
  - Offer
  - OfferItems
- Persistiert diese Daten in einer SQL-Datenbank (MS SQL Server 2019)
- Bietet eine REST API mit:
  - Versionierung (`/api/v1`)
  - OpenAPI (Swagger) Dokumentation
  - Validierung und Trennung in Controller / Services / DTOs
- Speichert zusätzlich manuell eingereichte Rechnungen über POST-Requests (z. B. via Postman)
- Führt jede Minute einen AI-gestützten Matching-Prozess durch:
  - Lädt alle Rechnungen, die `isChecked == null` sind
  - Vergleicht sie mit den gespeicherten Angeboten
  - Wenn eine Übereinstimmung gefunden wird:
    - Setzt `isChecked` auf das aktuelle Datum
    - Setzt `isValid` auf `true`

### 🗃️ PersistenceService

- Subscribt ebenfalls auf die RabbitMQ Queue
- Empfängt dieselben JSON-Daten wie die AiPipeline
- Persistiert die empfangenen JSON-Dateien in einem separaten Verzeichnis (`OFFER_PERSISTENCE_PATH`)
- Dient als Backup und zur späteren Nachvollziehbarkeit der Originaldaten

### 📦 Shared Library

- Gemeinsame Abhängigkeit für alle Services
- Enthält wiederverwendbare:
  - Utility-Klassen
  - Konfigurationsklassen für Messaging

## Technologien

- Java 21
- Spring Boot 3
- Gradle 8
- RabbitMQ (Messaging)
- Docker & Docker Compose
- Apache PDFBox
- Microsoft SQL Server 2019 (via Docker)
- OpenAPI (Swagger)
- JUnit 5 für Tests

## Setup

### Voraussetzungen

- Java 21
- Gradle ≥ 8.0
- Docker & Docker Compose

## Ausführen (lokal)

```bash
docker-compose up --build
```

Die Services werden automatisch gestartet und verbinden sich mit der Message Queue und der Datenbank. Der DocumentImporter beginnt, das konfigurierte PDF-Verzeichnis zu überwachen.

## Beispielablauf

1. Eine neue PDF wird im überwachten Verzeichnis gespeichert.
2. Der DocumentImporter erkennt die Datei, konvertiert sie in JSON und sendet sie über RabbitMQ.
3. Die AiPipeline verarbeitet das JSON, legt Offer, Customer und OfferItems in der Datenbank an.
4. Die PersistenceService speichert das JSON-Original im Archivverzeichnis.
5. Die AiPipeline führt jede Minute einen Vergleich mit den Rechnungen durch.
6. Wenn eine Übereinstimmung gefunden wird, wird die Rechnung als geprüft und gültig markiert.

## Beispielstruktur einer PDF → JSON

```json
{
"offerNumber":"ANG-20250518-8266",
"offerValue":10690.0,
"offerValidTill":"2025-06-01",
"offerDate":"2025-05-18",
"customerDto":{
   "companyName":"GeoBau Solutions GmbH",
   "addressStreet":"Bauhofstraße",
   "addressHouseNumber":"7",
   "postCode":"10115",
   "city":"Berlin",
   "phone":"+49 30 123456789",
   "mail":"kontakt@geobau-solutions.de"
},
"offerItemsDto":[
   {
      "posNumber":1,
      "description":"Tunnelbohrung50mTiefe",
      "amount":1,
      "price":4000.0
   },
   {
      "posNumber":2,
      "description":"Stahlbetonverstärkung",
      "amount":3,
      "price":850.0
   },
   {   
      "posNumber":3,
      "description":"BaugrundanalysevorOrt",
      "amount":2,
      "price":620.0
   },
   {
      "posNumber":4,
      "description":"Sprengvorbereitung&Absicherung",
      "amount":1,
      "price":2900.0
   }
]
}
```

## Projektstruktur

```bash
vv-projekt/
├── document-importer/
├── ai-pipeline/
├── persistence-service/
├── shared/
└── docker-compose.yml
```

## Tests ausführen

Für jeden Service:

```bash
./gradlew test
```

Testergebnisse findest du unter `build/reports/tests/test`.

## Entwickler

* **Name:** Casper Pelsma
* **Modul:** Verteilte Verarbeitung
* **Semester:** Sommersemester 2025
