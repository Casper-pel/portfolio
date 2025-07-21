# DocumentImporter – Übersicht

## Szenario

Im Rahmen des Moduls **Verteilte Verarbeitung (SS 2025)** entwickeln wir ein verteiltes System zur Automatisierung von Dokumentenvergleichen für das fiktive Unternehmen **Tunnelgräber GmbH**. Das Unternehmen vergleicht Angebote mit nachfolgenden Rechnungen, um Fehler und übersehene Leistungen zu vermeiden. Ziel ist es, diesen manuellen Schritt durch einen intelligenten Service zu ersetzen.

## Teilprojekte

- [DocumentImporter](./src/services/documentImporter/DocumentImporter/README.md)
- [AiPipeline](./src/services/aiPipeline/AiPipeline/README.md)
- [Messaging Service](./src/services/persistenceService/PersistenceService/README.md)
- [Shared Library](./src/services/shared/README.md)

## Warum Apache PDFBox?

Die **Apache PDFBox**-Bibliothek wird für das Parsen und Extrahieren von Daten aus PDF-Dateien verwendet, weil:

- Sie eine robuste, quelloffene Bibliothek mit aktiver Community ist.
- Sie umfangreiche Funktionen zum Lesen, Schreiben und Bearbeiten von PDF-Dokumenten bietet.
- Sie das Extrahieren von Text, Metadaten und eingebetteten Dateien unterstützt, was für die Aufbereitung von JSON-Daten essenziell ist.
- Sie gut dokumentiert ist und sich nahtlos in Java-basierte Anwendungen integrieren lässt.

## Voraussetzungen

- Java 21
- Gradle 8.13
- Docker & Docker Compose
- Eine `.env`-Datei mit konfigurierten Umgebungsvariablen. Eine Beispiel-Datei `.env.example` befindet sich im Projektverzeichnis und zeigt die benötigten angaben.
> **⚠️ Wichtiger Hinweis:**  
> Bitte ändere alle Standard-Passwörter und Zugangsdaten in der `.env`-Datei unbedingt vor dem Produktiveinsatz! Die Beispielwerte sind unsicher und dürfen nicht verwendet werden.

### Benötigte Umgebungsvariablen

| Variable                 | Beschreibung                              | Beispielwert         |
|--------------------------|------------------------------------------|----------------------|
| `MAX_FILE_SIZE`          | Maximale Größe von PDF-Dateien in MB         | `2`                  |
| `PATH_OFFERS`            | Verzeichnispfad zum Überwachen von PDFs      | `.`                  |
| `LOG_LEVEL`              | Protokollstufe für den Dienst               | `INFO`               |
| `OFFER_PERSISTENCE_PATH` | Pfad zum Speichern verarbeiteter Angebote          | `.`                  |
| `OFFER_STRATEGY`         | Strategie für die Verarbeitung von Angeboten          | `MessageBusStrategy` |
| `RABBITMQ_HOST`          | RabbitMQ-Server-Hostname                | `rabbitmq`           |
| `RABBITMQ_PORT`          | RabbitMQ-Server-Port                    | `5672`               |
| `RABBITMQ_USERNAME`      | RabbitMQ-Benutzername                       | `user`               |
| `RABBITMQ_PASSWORD`      | RabbitMQ-Passwort                       | `password`           |


## Tests ausführen

Um die Tests für den **DocumentImporter** oder **PersistenceService**-Dienst auszuführen:

1. Stelle sicher, dass Java 21 und Gradle installiert ist.
2. Öffne das Projekt in IntelliJ oder einem anderen Java-kompatiblen Editor.
3. Führe die Tests über die IDE aus oder nutze die Kommandozeile.
4. Wenn sie die komandozeile verwenden, navigiere zum Projektverzeichnis und führe den folgenden Befehl aus:
   ```bash
   ./gradlew test
   ```
3. Die Testergebnisse sind im Verzeichnis `build/reports/tests/test` oder innerhalb von InteliJ verfügbar.

## Docker Compose Starten

Um den **DocumentImporter**-Dienst mit Docker Compose auszuführen:

1. Stelle sicher, dass Docker und Docker Compose auf dem System installiert ist.
2. Starte den Dienst mit dem folgenden Befehl:
   ```bash
   docker-compose up --build
   ```
3. Der Dienst überwacht das konfigurierte Verzeichnis und verarbeitet `.pdf`-Dateien gemäß den Angaben in der `.env`-Datei.

## Projektdetails

- **Modul:** Verteilte Verarbeitung
- **Semester:** Sommersemester 2025
- **Student:** Casper Pelsma
