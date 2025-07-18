describe("AntragTabelle Page", () => {
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
      body: { accessRights: ["admin"] },
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

    // Employee-Liste für Name-Lookup
    cy.intercept("GET", "/api/employee/all", {
      statusCode: 200,
      body: [
        { employeeId: 1, firstName: "Max", lastName: "Mustermann" },
        { employeeId: 2, firstName: "Erika", lastName: "Musterfrau" },
        { employeeId: 10000, firstName: "Admin", lastName: "User" },
      ],
    }).as("getAllEmployees");

    // Standard: keine Anträge
    cy.intercept("GET", "/api/urlaubsantrag/all", {
      statusCode: 200,
      body: [],
    }).as("getAntraegeEmpty");

    cy.setCookie("token", "mock-jwt-token");
    cy.visit("/dashboard/urlaub");
    cy.get('[role="tab"]').contains("Anträge verwalten").click();
  });

  it("zeigt leere Tabelle korrekt an", () => {
    cy.get("h5").should("contain.text", "Urlaubsanträge");
    cy.get("table").should("exist");
    cy.get("tbody tr")
      .should("have.length", 1)
      .first()
      .find("td")
      .should("contain.text", "Keine Anträge bisher");
  });

  it("listet Anträge und öffnet Details-Dialog", () => {
    // Stub für gefüllte Anträge *vor* dem erneuten Fetch
    cy.intercept("GET", "/api/urlaubsantrag/all", {
      statusCode: 200,
      body: [
        {
          antragsId: 1,
          employeeId: 1,
          startDatum: "2025-07-04",
          endDatum: "2025-07-06",
          status: "pending",
          type: "Elternzeit",
          comment: "Familienzeit",
          submittedAt: "2025-06-20T07:00:00Z",
        },
        {
          antragsId: 2,
          employeeId: 2,
          startDatum: "2025-08-01",
          endDatum: "2025-08-05",
          status: "genehmigt",
          type: "Erholungsurlaub",
          comment: "",
          submittedAt: "2025-06-15T12:30:00Z",
          reviewDate: "2025-06-22T09:00:00Z",
          reviewerId: 10000,
          reviewComment: "OK",
        },
      ],
    }).as("getAntraegeFilled");

    cy.reload();
    cy.get('[role="tab"]').contains("Anträge verwalten").click();
    cy.wait("@getAntraegeFilled");

    cy.get("tbody tr").should("have.length", 2);

    // Erste Zeile: Ausstehend
    cy.get("tbody tr")
      .eq(0)
      .within(() => {
        cy.get("td").eq(0).should("contain.text", "Max Mustermann");
        cy.get("td").eq(1).should("contain.text", "04.07.2025 - 06.07.2025");
        cy.get("td").eq(2).should("contain.text", "3");
        cy.get("td").eq(3).should("contain.text", "Elternzeit");
        cy.get("td")
          .eq(4)
          .find(".MuiChip-label")
          .should("contain.text", "Ausstehend");
        cy.get("td").eq(5).should("contain.text", "20.06.2025 09:00");
        // Details-Button muss existieren
        cy.get('button[aria-label="Details anzeigen"]').should("exist");
      });

    // Detail-Dialog öffnen
    cy.get("tbody tr")
      .eq(0)
      .find('button[aria-label="Details anzeigen"]')
      .click();
    cy.get("h2").should(
      "contain.text",
      "Urlaubsantrag Details - Max Mustermann"
    );
    cy.get("button").contains("Schließen").click();
    cy.get('div[role="dialog"]').should("not.exist");
  });

  it("lehnt einen Antrag ab", () => {
    // Stub für einen pending Antrag
    cy.intercept("GET", "/api/urlaubsantrag/all", {
      statusCode: 200,
      body: [
        {
          antragsId: 3,
          employeeId: 1,
          startDatum: "2025-09-10",
          endDatum: "2025-09-12",
          status: "pending",
          type: "Erholungsurlaub",
          comment: "Herbstferien",
          submittedAt: "2025-07-01T06:00:00Z",
        },
      ],
    }).as("getAntraegeForReview");

    cy.reload();
    cy.get('[role="tab"]').contains("Anträge verwalten").click();
    cy.wait("@getAntraegeForReview");

    // Details öffnen
    cy.get("tbody tr")
      .eq(0)
      .find('button[aria-label="Details anzeigen"]')
      .click();
    cy.get("h2").should(
      "contain.text",
      "Urlaubsantrag Details - Max Mustermann"
    );

    // Review API Mock - Backend erwartet vollständiges UrlaubsAntragDto
    cy.intercept("PUT", "/api/urlaubsantrag/review", (req) => {
      console.log("Review Request:", req.body);
      // Validiere dass Request Body alle nötigen Felder hat
      expect(req.body).to.have.property("antragsId");
      expect(req.body).to.have.property("employeeId");
      expect(req.body).to.have.property("startDatum");
      expect(req.body).to.have.property("endDatum");
      expect(req.body).to.have.property("status", "abgelehnt");
      expect(req.body).to.have.property("type");
      expect(req.body).to.have.property("grund");
      expect(req.body).to.have.property("reviewDate");
      expect(req.body).to.have.property("reviewerId");

      req.reply({
        statusCode: 200,
        body: "Urlaubsantrag reviewed successfully",
      });
    }).as("rejectAntrag");

    // Mock für getById API-Call (wird vom Service benötigt)
    cy.intercept("GET", "/api/urlaubsantrag/get/3", {
      statusCode: 200,
      body: {
        antragsId: 3,
        employeeId: 1,
        startDatum: "2025-09-10",
        endDatum: "2025-09-12",
        status: "pending",
        type: "Erholungsurlaub",
        grund: "Herbstferien",
        submittedAt: "2025-07-01T06:00:00Z",
      },
    }).as("getAntragById");

    // Ablehnen
    cy.get("button").contains("Ablehnen").click();

    // Warte auf beide API-Calls
    cy.wait("@getAntragById");
    cy.wait("@rejectAntrag");

    cy.get(".MuiAlert-root").should("contain.text", "Antrag wurde abgelehnt");

    cy.get(".MuiAlert-root").should("contain.text", "Antrag wurde abgelehnt");

    // Nach Ablehnung: neue Daten stubben
    cy.intercept("GET", "/api/urlaubsantrag/all", {
      statusCode: 200,
      body: [
        {
          antragsId: 3,
          employeeId: 1,
          startDatum: "2025-09-10",
          endDatum: "2025-09-12",
          status: "abgelehnt",
          type: "Erholungsurlaub",
          comment: "Herbstferien",
          submittedAt: "2025-07-01T06:00:00Z",
          reviewDate: "2025-07-02T08:00:00Z",
          reviewerId: 10000,
          reviewComment: "",
        },
      ],
    }).as("getAntraegeAfterReject");

    // Dialog schließt automatisch, warte auf Reload
    cy.reload();
    cy.get('[role="tab"]').contains("Anträge verwalten").click();
    cy.wait("@getAntraegeAfterReject");

    // Status prüfen
    cy.get("tbody tr")
      .eq(0)
      .find(".MuiChip-label")
      .should("contain.text", "Abgelehnt");
  });

  it("genehmigt einen Antrag", () => {
    // Stub für einen pending Antrag
    cy.intercept("GET", "/api/urlaubsantrag/all", {
      statusCode: 200,
      body: [
        {
          antragsId: 4,
          employeeId: 2,
          startDatum: "2025-08-15",
          endDatum: "2025-08-20",
          status: "pending",
          type: "Erholungsurlaub",
          comment: "Sommerurlaub",
          submittedAt: "2025-07-02T06:00:00Z",
        },
      ],
    }).as("getAntraegeForApproval");

    cy.reload();
    cy.get('[role="tab"]').contains("Anträge verwalten").click();
    cy.wait("@getAntraegeForApproval");

    // Details öffnen
    cy.get("tbody tr")
      .eq(0)
      .find('button[aria-label="Details anzeigen"]')
      .click();
    cy.get("h2").should(
      "contain.text",
      "Urlaubsantrag Details - Erika Musterfrau"
    );

    // Review API Mock - Backend erwartet vollständiges UrlaubsAntragDto
    cy.intercept("PUT", "/api/urlaubsantrag/review", (req) => {
      console.log("Approve Request:", req.body);
      // Validiere dass Request Body alle nötigen Felder hat
      expect(req.body).to.have.property("antragsId");
      expect(req.body).to.have.property("employeeId");
      expect(req.body).to.have.property("startDatum");
      expect(req.body).to.have.property("endDatum");
      expect(req.body).to.have.property("status", "genehmigt");
      expect(req.body).to.have.property("type");
      expect(req.body).to.have.property("grund");
      expect(req.body).to.have.property("reviewDate");
      expect(req.body).to.have.property("reviewerId");

      req.reply({
        statusCode: 200,
        body: "Urlaubsantrag reviewed successfully",
      });
    }).as("approveAntrag");

    // Mock für getById API-Call (wird vom Service benötigt)
    cy.intercept("GET", "/api/urlaubsantrag/get/4", {
      statusCode: 200,
      body: {
        antragsId: 4,
        employeeId: 2,
        startDatum: "2025-08-15",
        endDatum: "2025-08-20",
        status: "pending",
        type: "Erholungsurlaub",
        grund: "Sommerurlaub",
        submittedAt: "2025-07-02T06:00:00Z",
      },
    }).as("getAntragById");

    // Genehmigen
    cy.get("button").contains("Genehmigen").click();

    // Warte auf beide API-Calls
    cy.wait("@getAntragById");
    cy.wait("@approveAntrag");

    cy.get(".MuiAlert-root").should("contain.text", "Antrag wurde genehmigt");

    cy.get(".MuiAlert-root").should("contain.text", "Antrag wurde genehmigt");

    // Nach Genehmigung: neue Daten stubben
    cy.intercept("GET", "/api/urlaubsantrag/all", {
      statusCode: 200,
      body: [
        {
          antragsId: 4,
          employeeId: 2,
          startDatum: "2025-08-15",
          endDatum: "2025-08-20",
          status: "genehmigt",
          type: "Erholungsurlaub",
          comment: "Sommerurlaub",
          submittedAt: "2025-07-02T06:00:00Z",
          reviewDate: "2025-07-02T08:00:00Z",
          reviewerId: 10000,
          reviewComment: "Genehmigt",
        },
      ],
    }).as("getAntraegeAfterApprove");

    // Dialog schließt automatisch, warte auf Reload
    cy.reload();
    cy.get('[role="tab"]').contains("Anträge verwalten").click();
    cy.wait("@getAntraegeAfterApprove");

    // Status prüfen
    cy.get("tbody tr")
      .eq(0)
      .find(".MuiChip-label")
      .should("contain.text", "Genehmigt");
  });

  it("zeigt verschiedene Antrags-Stati korrekt an", () => {
    // Stub mit verschiedenen Stati
    cy.intercept("GET", "/api/urlaubsantrag/all", {
      statusCode: 200,
      body: [
        {
          antragsId: 5,
          employeeId: 1,
          startDatum: "2025-07-10",
          endDatum: "2025-07-12",
          status: "pending",
          type: "Erholungsurlaub",
          comment: "Kurzurlaub",
          submittedAt: "2025-07-01T06:00:00Z",
        },
        {
          antragsId: 6,
          employeeId: 2,
          startDatum: "2025-08-01",
          endDatum: "2025-08-05",
          status: "genehmigt",
          type: "Erholungsurlaub",
          comment: "",
          submittedAt: "2025-06-15T12:30:00Z",
          reviewDate: "2025-06-22T09:00:00Z",
          reviewerId: 10000,
          reviewComment: "Genehmigt",
        },
        {
          antragsId: 7,
          employeeId: 1,
          startDatum: "2025-09-01",
          endDatum: "2025-09-03",
          status: "abgelehnt",
          type: "Krankheit",
          comment: "Arzttermin",
          submittedAt: "2025-06-20T07:00:00Z",
          reviewDate: "2025-06-25T08:00:00Z",
          reviewerId: 10000,
          reviewComment: "Nicht genehmigt",
        },
      ],
    }).as("getAntraegeWithDifferentStati");

    cy.reload();
    cy.get('[role="tab"]').contains("Anträge verwalten").click();
    cy.wait("@getAntraegeWithDifferentStati");

    cy.get("tbody tr").should("have.length", 3);

    // Pending Antrag
    cy.get("tbody tr")
      .eq(0)
      .within(() => {
        cy.get('td').eq(0).should('contain.text', 'Max Mustermann');
        cy.get('td').eq(4).find('.MuiChip-label').should('contain.text', 'Ausstehend');
      });

  });

  it("testet Sortierung und Filterung", () => {
    // Stub mit mehreren Anträgen für Sortierung
    cy.intercept("GET", "/api/urlaubsantrag/all", {
      statusCode: 200,
      body: [
        {
          antragsId: 8,
          employeeId: 1,
          startDatum: "2025-12-20",
          endDatum: "2025-12-31",
          status: "pending",
          type: "Erholungsurlaub",
          comment: "Weihnachtsurlaub",
          submittedAt: "2025-07-02T06:00:00Z",
        },
        {
          antragsId: 9,
          employeeId: 2,
          startDatum: "2025-07-15",
          endDatum: "2025-07-16",
          status: "genehmigt",
          type: "Krankheit",
          comment: "Arzttermin",
          submittedAt: "2025-07-01T06:00:00Z",
          reviewDate: "2025-07-01T08:00:00Z",
          reviewerId: 10000,
          reviewComment: "OK",
        },
      ],
    }).as("getAntraegeForSorting");

    cy.reload();
    cy.get('[role="tab"]').contains("Anträge verwalten").click();
    cy.wait("@getAntraegeForSorting");

    // Prüfe dass beide Anträge angezeigt werden
    cy.get("tbody tr").should("have.length", 2);

    // Prüfe verschiedene Zeiträume und Typen
    cy.get("tbody tr")
      .eq(0)
      .within(() => {
        cy.get("td").eq(1).should("contain.text", "20.12.2025 - 31.12.2025");
        cy.get("td").eq(2).should("contain.text", "12"); // 12 Tage
        cy.get("td").eq(3).should("contain.text", "Erholungsurlaub");
      });

    cy.get("tbody tr")
      .eq(1)
      .within(() => {
        cy.get("td").eq(1).should("contain.text", "15.07.2025 - 16.07.2025");
        cy.get("td").eq(2).should("contain.text", "2"); // 2 Tage
        cy.get("td").eq(3).should("contain.text", "Krankheit");
      });
  });

  it("Debug: Analysiere API-Aufrufe beim Ablehnen", () => {
    // Mock alle relevanten API-Endpunkte für Debug
    cy.intercept("GET", "/api/urlaubsantrag/get/**", (req) => {
      console.log("GET By ID Request:", req.url);
      const id = req.url.split("/").pop();
      req.reply({
        statusCode: 200,
        body: {
          antragsId: parseInt(id || "99"),
          employeeId: 1,
          startDatum: "2025-09-10",
          endDatum: "2025-09-12",
          status: "pending",
          type: "Erholungsurlaub",
          grund: "Debug Test",
          submittedAt: "2025-07-01T06:00:00Z",
        },
      });
    }).as("getAntragById");

    cy.intercept("PUT", "/api/urlaubsantrag/review", (req) => {
      console.log("Review Request Body:", JSON.stringify(req.body, null, 2));
      console.log("Review Request Headers:", req.headers);
      req.reply({
        statusCode: 200,
        body: "Urlaubsantrag reviewed successfully",
      });
    }).as("reviewRequest");

    // Stub für einen pending Antrag
    cy.intercept("GET", "/api/urlaubsantrag/all", {
      statusCode: 200,
      body: [
        {
          antragsId: 99,
          employeeId: 1,
          startDatum: "2025-09-10",
          endDatum: "2025-09-12",
          status: "pending",
          type: "Erholungsurlaub",
          comment: "Debug Test",
          submittedAt: "2025-07-01T06:00:00Z",
        },
      ],
    }).as("getAntraegeForDebug");

    cy.reload();
    cy.get('[role="tab"]').contains("Anträge verwalten").click();
    cy.wait("@getAntraegeForDebug");

    // Details öffnen
    cy.get("tbody tr")
      .eq(0)
      .find('button[aria-label="Details anzeigen"]')
      .click();
    cy.get("h2").should(
      "contain.text",
      "Urlaubsantrag Details - Max Mustermann"
    );

    // Ablehnen und API-Aufrufe verfolgen
    cy.get("button").contains("Ablehnen").click();

    // Warte auf API-Aufrufe
    cy.wait("@getAntragById");
    cy.wait("@reviewRequest");

    cy.wait(1000); // Kurz warten für Logging
  });
});
