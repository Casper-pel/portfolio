// http://localhost:3000/dashboard/urlaub

describe("Main Page", () => {
  beforeEach(() => {
    // Hydration-Fehler unterdrücken
    cy.on("uncaught:exception", (err) => {
      if (
        err.message.includes("Minified React error") ||
        err.message.includes("Hydration") ||
        err.message.includes("hydration") ||
        err.message.includes("Text content does not match") ||
        err.message.includes("Warning:")
      ) {
        return false;
      }
      return true;
    });

    // Auth-Mocks
    cy.intercept("POST", "/api/auth/cookie-validation", {
      statusCode: 200,
      body: {
        accessRights: ["admin"], // Mocked access rights for admin
      },
    }).as("validateCookie");

    cy.intercept("GET", "/api/employee/current", {
      statusCode: 200,
      body: {
        employeeId: 10000,
        firstName: "Admin",
        lastName: "User",
        role: { roleId: 1, roleName: "Administrator" },
      },
    }).as("getCurrentUser");

    cy.intercept("GET", "/api/urlaubsantrag/user", {
      statusCode: 200,
      body: [],
    }).as("getMeineAntraege");

    cy.setCookie("token", "mock-jwt-token");
    cy.visit("/dashboard/urlaub");
    cy.get('[role="tab"]').contains("Meine Anträge").click();
  });

  it("sollte infos wenn keine anträge sind korrekt anzeigen", () => {
    cy.intercept("GET", "/api/urlaubsantrag/user", {
      statusCode: 200,
      body: [],
    }).as("getMeineAntraege");

    // Header & Hinweis
    cy.get("h5")
      .should("contain.text", "Meine Urlaubsanträge")
      .and("be.visible");
    cy.get("h6")
      .should("contain.text", "Sie haben noch keine Urlaubsanträge eingereicht")
      .and("be.visible");
    cy.get(".MuiTypography-body2")
      .should(
        "contain.text",
        'Wechseln Sie zum Tab "Neuen Antrag stellen", um Ihren ersten Antrag einzureichen.'
      )
      .and("be.visible");
  });

  it("sollte anträge korrekt anzeigen und Aktionen bereitstellen", () => {
    cy.intercept("GET", "/api/urlaubsantrag/user", {
      statusCode: 200,
      body: [
        {
          antragsId: 1,
          employeeId: 1,
          startDatum: "2025-07-04",
          endDatum: "2025-07-06",
          status: "pending",
          type: "Elternzeit",
          grund: "a",
          reviewDate: null,
          reviewerId: null,
          comment: null,
        },
        {
          antragsId: 2,
          employeeId: 1,
          startDatum: "2025-07-10",
          endDatum: "2025-07-12",
          status: "genehmigt",
          type: "Erholungsurlaub",
          grund: "b",
          reviewDate: "2025-07-01T10:00:00Z",
          reviewerId: 2,
          comment: "Alles gut",
        },
      ],
    }).as("getMeineAntraegeInitial");

    cy.reload();
    cy.get('[role="tab"]').contains("Meine Anträge").click();

    // Warte auf Daten
    cy.wait("@getMeineAntraegeInitial");

    // Tabelle und Kopfzeile
    cy.get("h5")
      .should("contain.text", "Meine Urlaubsanträge")
      .and("be.visible");
    cy.get("table").should("exist");
    cy.get("thead th").then(($ths) => {
      const headers = [...$ths].map((th) => th.innerText.trim());
      expect(headers).to.deep.equal([
        "Zeitraum",
        "Tage",
        "Art",
        "Status",
        "Erstellt von",
        "Eingereicht am",
        "Aktionen",
      ]);
    });

    // Zwei Zeilen
    cy.get("tbody tr").should("have.length", 2);

    // Erste Zeile: pending
    cy.get("tbody tr")
      .eq(0)
      .within(() => {
        cy.get("td").eq(0).should("contain.text", "04.07.2025 - 06.07.2025");
        cy.get("td").eq(1).should("contain.text", "3");
        cy.get("td").eq(2).should("contain.text", "Elternzeit");
        cy.get("td")
          .eq(3)
          .find(".MuiChip-label")
          .should("contain.text", "Ausstehend");
        // Sichtbar-Icon
        cy.get("button")
          .filter('[aria-label="Details anzeigen"]')
          .should("exist");
        // Bearbeiten + Löschen, weil pending
        cy.get("button").filter('[aria-label="Bearbeiten"]').should("exist");
        cy.get("button").filter('[aria-label="Löschen"]').should("exist");
      });

    // Zweite Zeile: genehmigt
    cy.get("tbody tr")
      .eq(1)
      .within(() => {
        cy.get("td").eq(0).should("contain.text", "10.07.2025 - 12.07.2025");
        cy.get("td")
          .eq(3)
          .find(".MuiChip-label")
          .should("contain.text", "Genehmigt");
        // Bearbeiten/Löschen nicht vorhanden
        cy.get("button")
          .filter('[aria-label="Bearbeiten"]')
          .should("not.exist");
        cy.get("button").filter('[aria-label="Löschen"]').should("not.exist");
      });

    // Details-Dialog öffnen und schließen
    cy.get("tbody tr")
      .eq(0)
      .within(() => {
        cy.get("button").filter('[aria-label="Details anzeigen"]').click();
      });
    cy.get("h2").should("contain.text", "Mein Urlaubsantrag - Details");
    cy.get("button").contains("Schließen").click();
    cy.get('[role="dialog"]').should("not.exist");

    // Delete-Dialog öffnen und abbrechen
    cy.get("tbody tr")
      .eq(0)
      .within(() => {
        cy.get("button").filter('[aria-label="Löschen"]').click();
      });
    cy.get("h2").should("contain.text", "Antrag löschen");
    cy.get("button").contains("Abbrechen").click();
    cy.get('[role="dialog"]').should("not.exist");

    // Edit-Modal öffnen und schließen
    cy.get("tbody tr")
      .eq(0)
      .within(() => {
        cy.get("button").filter('[aria-label="Bearbeiten"]').click();
      });
    cy.get("h2").should("contain.text", "Urlaubsantrag bearbeiten");
    cy.get("button").contains("Abbrechen").click();
    cy.get('[role="dialog"]').should("not.exist");
  });

  it("sollte antrag löschen und Erfolgsmeldung anzeigen", () => {
    cy.intercept("GET", "/api/urlaubsantrag/user", {
      statusCode: 200,
      body: [
        {
          antragsId: 1,
          employeeId: 1,
          startDatum: "2025-07-04",
          endDatum: "2025-07-06",
          status: "pending",
          type: "Elternzeit",
          grund: "a",
          reviewDate: null,
          reviewerId: null,
          comment: null,
        },
        {
          antragsId: 2,
          employeeId: 1,
          startDatum: "2025-07-10",
          endDatum: "2025-07-12",
          status: "genehmigt",
          type: "Erholungsurlaub",
          grund: "b",
          reviewDate: "2025-07-01T10:00:00Z",
          reviewerId: 2,
          comment: "Alles gut",
        },
      ],
    }).as("getMeineAntraegeNew");

    cy.reload();
    cy.get('[role="tab"]').contains("Meine Anträge").click();

    // Warte auf Daten
    cy.wait("@getMeineAntraegeNew");

    cy.intercept("DELETE", "/api/urlaubsantrag/delete/1", {
      statusCode: 200,
      body: { success: true, message: "Antrag erfolgreich gelöscht" },
    }).as("deleteAntrag");

    // 3. Stub für den zweiten GET nach dem Löschen – liefert jetzt eine leere Liste
    cy.intercept("GET", "/api/urlaubsantrag/user", {
      statusCode: 200,
      body: [],
    }).as("getMeineAntraegeAfterDelete");

    // Löschen-Button klicken
    cy.get("tbody tr")
      .eq(0)
      .within(() => {
        cy.get("button").filter('[aria-label="Löschen"]').click();
      });

    // Dialog bestätigen
    cy.get("h2").should("contain.text", "Antrag löschen");
    cy.get("button").contains("Löschen").click();

    // Anträge nach dem Löschen abwarten
    cy.wait("@getMeineAntraegeAfterDelete");

    // Erfolgsmeldung prüfen
    cy.get(".MuiAlert-root")
      .should("contain.text", "Antrag wurde erfolgreich gelöscht")
      .and("be.visible");

    // Tabelle sollte leer sein
    cy.get("tbody tr").should("have.length", 0);
  });

  it("soll Antrag bearbeiten und Erfolgsmeldung anzeigen", () => {
    cy.intercept("GET", "/api/urlaubsantrag/user", {
      statusCode: 200,
      body: [
        {
          antragsId: 1,
          employeeId: 1,
          startDatum: "2025-07-04",
          endDatum: "2025-07-06",
          status: "pending",
          type: "Elternzeit",
          grund: "a",
          reviewDate: null,
          reviewerId: null,
          comment: null,
        },
      ],
    }).as("getMeineAntraegeEdit");

    cy.reload();
    cy.get('[role="tab"]').contains("Meine Anträge").click();

    // Warte auf Daten
    cy.wait("@getMeineAntraegeEdit");

    // Edit-Button klicken
    cy.get("tbody tr")
      .eq(0)
      .within(() => {
        cy.get("button").filter('[aria-label="Bearbeiten"]').click();
      });

    // Modal prüfen
    cy.get("h2").should("contain.text", "Urlaubsantrag bearbeiten");
    // cy.get('.start-date-picker').should('have.value', '04.07.2025');
    // cy.get('.end-date-picker').should('have.value', '06.07.2025');
    cy.get("#art-select").should("contain.text", "Elternzeit");
    // cy.get('.grund-input').should('have.value', 'a');

    // Änderungen vornehmen
    cy.get(".start-date-picker").type("05072025");
    cy.get(".end-date-picker").type("07072025");
    cy.get("#art-select").click();
    cy.get("li").contains("Erholungsurlaub").click();
    cy.get(".grund-input").clear().type("Neuer Grund");

    // Intercept für Update
    cy.intercept("PUT", "/api/urlaubsantrag/update", {
      statusCode: 200,
      body: { success: true, message: "Antrag erfolgreich aktualisiert" },
    }).as("updateAntrag");

    // Speichern klicken
    cy.get("button").contains("Aktualisieren").click();
  });
});
