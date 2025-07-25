# Management- & Kassensystem

## Projektübersicht

Dieses Projekt ist ein umfassendes Management- und Kassensystem, das mehrere Verwaltungsfunktionen in einem zentralen Tool integriert.

### Hauptfunktionen

- **Kassensystem**: Integration einer modernen, digitalen Kasse mit Stripe-Zahlungsabwicklung
- **Finanzverwaltung**: Verknüpfung der Kassenbewegungen mit einem Finanzübersichtssystem
- **Mitarbeiterverwaltung**: Verwaltung von Nutzern und Mitarbeiterrollen
- **Urlaubsverwaltung**: Planen, Genehmigen und Nachverfolgen von Urlaubstagen der Mitarbeiter
- **Produktverwaltung**: Produkte und Coupons erstellen, updaten, löschen

---

## Screenshots

### 1. **Menü**

![Menü](./images/menu.png)

Das Menü erscheint nach der Anmeldung und bietet eine schnelle Navigation zum Dashboard oder zur Kasse 

### 2. **Startbildschirm des Dashboards**

![Startbildschirm](./images/startbildschirm.png)

Der Startbildschirm begrüßt den User und gibt ihn die möglichkeit sein Passwort zu aktualisieren.

### 3. **Kasse**

![Kasse](./images/kasse.png)

Der Kassenbereich bietet eine benutzerfreundliche Oberfläche zur Bearbeitung von Bestellungen.

### 4. **Checkout mit Stripe**

![Checkout Stripe](./images/checkout.png)

Das Kassensystem bietet eine nahtlose Stripe-Zahlungsabwicklung im Checkout-Prozess.

### 5. **Finanzübersicht**

![Finanzübersicht](./images/finanzen.png)

Die Finanzübersicht zeigt eine detaillierte Darstellung der Kassenbewegungen und ermöglicht eine einfache Nachverfolgung der Finanzen.

### 6. **Urlaubsanträge**

![Urlaubsanträge](./images/urlaub.png)

Mitarbeiter können Urlaubsanträge stellen, die durch das berechtigte User verwaltet werden.

### 7. **Rollenverwaltung**

![Rollenverwaltung](./images/rollen.png)

In der Rollenverwaltung können Nutzerrollen angepasst und verwaltet werden.

### 8. **Mitarbeiterverwaltung**

![Mitarbeiterverwaltung](./images/mitarbeiter.png)

Die Mitarbeiterverwaltung bietet eine Übersicht und Bearbeitung von Mitarbeiterdaten und -rollen.

### 9. **Produktverwaltung**

![Produktverwaltung](./images/produkte.png)

Produkte können hier erstellt, bearbeitet und gelöscht werden.

### 10. **Coupon Übersicht**

![Coupon Übersicht](./images/coupon.png)

In der Coupon Übersicht können alle aktiven und inaktiven Coupons angezeigt werden.

---

## Projektstart: Schritt-für-Schritt Anleitung

### 1. Umgebungsvariablen konfigurieren

#### Root-Verzeichnis (`./.env`)

```env
MONGO_INITDB_ROOT_USERNAME=deinMongoDBBenutzername
MONGO_INITDB_ROOT_PASSWORD=deinMongoDBPasswort
```

> **Wichtiger Hinweis**: Diese Zugangsdaten ersetzen den Standardbenutzer `admin:admin` – ändere sie unbedingt, um die Datenbank abzusichern.

#### Backend-Verzeichnis (`./backend/.env`)

```env
# Stripe Secret Key
STRIPE_SECRETKEY=sk_test_...

# JWT Signing Key (256-Bit Schlüssel für JWT-Signierung)
# Erzeuge diesen Schlüssel mit: openssl rand -base64 32
SIGNING_KEY=dein_mit_openssl_generierter_schluessel
```

> **Aplication.properties**: Der Connection String ersetzt `spring.data.mongodb.username` und `spring.data.mongodb.password` in der `application.properties`. Hier muss lediglich Benutzername und Passwort nach obiger Wahl angepasst werden:

```properties
spring.data.mongodb.uri=mongodb://deinMongoDBBenutzername:deinMongoDBPasswort@mongo:27017/your-database-name?authSource=admin
```

#### Frontend-Verzeichnis (`./frontend/.env`)

```env
NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY=pk_test_...
```

### 2. Projekt starten

Sobald alle `.env`-Dateien korrekt gesetzt sind, kannst du das gesamte System mit Docker Compose bauen und starten:

```bash
docker compose up -d --build
```

> **Hinweis**: Der `--build` Parameter stellt sicher, dass alle Abhängigkeiten und Änderungen berücksichtigt werden.

---

## Verzeichnisstruktur

```
.
├── backend/
│   ├── .env                    # Stripe Secret Key & 'Signing Key'
│   └── src/
│       └── main/resources/
│           └── application.properties  # nutzt SPRING_DATA_MONGODB_URI
├── frontend/
│   └── .env                    # Stripe Public Key
├── .env                        # MongoDB Root-Zugangsdaten für Initialisierung
├── docker-compose.yml
└── README.md
```

---

## GitLab CI/CD Konfiguration

In der GitLab CI/CD sollten folgende Umgebungsvariablen gesetzt sein. Diese sollten genutzt werden, um die .env Dateien zu befüllen und das Projekt zu starten.

- `STRIPE_SECRETKEY`
- `NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY`

---

## Wichtige Hinweise

### Wichtige Backend Server Deployment Infos!
- Das Backend hat ein wait-for-it script im image, welches wartet, dass der mongodb container fertig starten kann, bevor sich das Backend startet und darauf verbinden will. Das ist aktuell auf 10 Sekunden eingestellt. In der Zeit wird der nginx Reverse Proxy einen 502 Response Code antworten, da das Backend nicht verfügbar ist.

### Sicherheit
- **Ersetze `admin:admin`** in allen Konfigurationsdateien und `.env`-Werten durch sichere Zugangsdaten
- Achte auf Konsistenz zwischen `.env` und `application.properties` bzw. Docker-Komponenten

### Testing & Debugging
- Teste die Verbindung zur MongoDB nach Anpassung des Connection Strings
- Verwende Tools wie `mongosh` oder überprüfe die Docker-Logs für Verbindungsprobleme

### Deployment
- Stelle sicher, dass alle Umgebungsvariablen in der Produktionsumgebung korrekt gesetzt sind
- Dokumentiere alle Änderungen an der Konfiguration für das Team
