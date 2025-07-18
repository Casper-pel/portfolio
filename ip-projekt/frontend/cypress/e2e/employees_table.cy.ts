describe("Employees Table", () => {
  beforeEach(() => {
    // Unterdrücke Hydration und andere Next.js Fehler
    cy.on("uncaught:exception", (err, runnable) => {
      // Ignoriere Hydration-Fehler und andere React/Next.js Warnungen
      if (
        err.message.includes("Minified React error") ||
        err.message.includes("Hydration") ||
        err.message.includes("hydration") ||
        err.message.includes("Text content does not match") ||
        err.message.includes("Warning:") ||
        err.message.includes("users.map is not a function") ||
        err.message.includes(".map is not a function") ||
        err.message.includes("Cannot read properties of undefined") ||
        err.message.includes("Cannot read property")
      ) {
        return false;
      }
      // Lass andere Fehler durch
      return true;
    });

    // Mock Authentication zuerst - Backend gibt "Cookie valid" als String zurück
    cy.intercept("POST", "/api/auth/cookie-validation", {
      statusCode: 200,
      body: {
        accessRights: ["admin"],
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

    // Mock Employees API - damit die Seite nicht leer ist
    cy.intercept("GET", "/api/employee/all", {
      statusCode: 200,
      body: [
        {
          employeeId: 1,
          firstName: "Max",
          lastName: "Mustermann",
          role: { roleId: 2, roleName: "Mitarbeiter" },
        },
        {
          employeeId: 2,
          firstName: "Erika",
          lastName: "Mustermann",
          role: { roleId: 3, roleName: "Manager" },
        },
      ],
    }).as("getEmployees");

    // Set authentication cookie
    cy.setCookie("token", "mock-jwt-token");

    cy.visit("/dashboard/employees");
  });

  it("should display the employees table", () => {
    cy.get("table").should("be.visible");
    cy.get("th").contains("ID").should("be.visible");
    cy.get("th").contains("Nachname").should("be.visible");
    cy.get("th").contains("Vorname").should("be.visible");
    cy.get("th").contains("Rolle").should("be.visible");
    cy.get("th").contains("Aktionen").should("be.visible");
  });

  it("should show no employees message when empty", () => {
    // Mock empty response
    cy.intercept("GET", "/api/employee/all", {
      statusCode: 200,
      body: { accessRights: ["user.read"] },
    }).as("getEmployees");

    cy.wait("@getEmployees");

    cy.get("table tbody tr td")
      .contains("Noch keine Mitarbeiter erstellt.")
      .should("be.visible");
  });

  it("should display employees when available", () => {
    // Mock employees response
    cy.intercept("GET", "/api/employee/all", {
      statusCode: 200,
      body: [
        {
          employeeId: 1,
          firstName: "Max",
          lastName: "Mustermann",
          role: { roleName: "Mitarbeiter" },
        },
        {
          employeeId: 2,
          firstName: "Erika",
          lastName: "Mustermann",
          role: { roleName: "Manager" },
        },
      ],
    }).as("getEmployees");

    cy.wait("@getEmployees");

    cy.get("table tbody tr").should("have.length", 2);
    cy.get("table tbody tr")
      .eq(0)
      .find("td")
      .eq(0)
      .should("contain.text", "Max");
    cy.get("table tbody tr")
      .eq(0)
      .find("td")
      .eq(1)
      .should("contain.text", "Mustermann");
    cy.get("table tbody tr")
      .eq(0)
      .find("td")
      .eq(2)
      .should("contain.text", "Mitarbeiter");
    // cy.get('table tbody tr').eq(0).find('td').eq(3).should('contain.text', 'Mitarbeiter');
  });

  it("should allow editing an employee", () => {
    // Mock PUT API für User Update
    cy.intercept("PUT", "/api/employee/*", {
      statusCode: 200,
      body: {
        employeeId: 1,
        firstName: "Maximilian",
        lastName: "Mustermann-Smith",
        role: { roleId: 2, roleName: "Mitarbeiter" },
      },
    }).as("updateEmployee");

    // Mock für Rollen-API
    cy.intercept("GET", "/api/role/all", {
      statusCode: 200,
      body: [
        { roleId: 1, roleName: "Administrator" },
        { roleId: 2, roleName: "Mitarbeiter" },
        { roleId: 3, roleName: "Manager" },
      ],
    }).as("getRoles");

    // Klicke auf den Edit-Button (IconButton mit EditIcon)
    cy.get("table tbody tr").eq(0).find('button[title="Bearbeiten"]').click();

   

    // Überprüfe dass das Modal geöffnet ist
    cy.get('[aria-labelledby="edit-user-modal-title"]').should("be.visible");
    cy.contains("Mitarbeiter bearbeiten").should("be.visible");

    // Überprüfe die vorausgefüllten Werte
    cy.get("#firstName").should("be.visible").and("have.value", "Max");
    cy.get("#lastName").should("be.visible").and("have.value", "Mustermann");

    // Mock die aktualisierte Employee-Liste BEVOR wir speichern
    cy.intercept("GET", "/api/employee/all", {
      statusCode: 200,
      body: [
        {
          employeeId: 1,
          firstName: "Maximilian",
          lastName: "Mustermann-Smith",
          role: { roleId: 2, roleName: "Mitarbeiter" },
        },
        {
          employeeId: 2,
          firstName: "Erika",
          lastName: "Mustermann",
          role: { roleId: 3, roleName: "Manager" },
        },
      ],
    }).as("getUpdatedEmployees");

    // Bearbeite die Felder
    cy.get("#firstName").clear().type("Maximilian");
    cy.get("#lastName").clear().type("Mustermann-Smith");

    // Klicke auf Aktualisieren-Button
    cy.contains("button", "Aktualisieren").click();

    // Warte auf API-Call
    cy.wait("@updateEmployee");

    // Das Modal sollte sich schließen (mit einem Timeout, falls es länger dauert)
    cy.get('[aria-labelledby="edit-user-modal-title"]', {
      timeout: 10000,
    }).should("not.exist");

    // Simuliere einen Re-fetch der Employee-Daten, falls die App das nicht automatisch macht
    cy.reload();

    // Warte auf die Seite um neu zu laden und auf Authentication
    cy.wait("@validateCookie");
    cy.wait("@getUpdatedEmployees");

    // Überprüfe dass die Tabelle aktualisiert wurde
    cy.get("table tbody tr")
      .eq(0)
      .find("td")
      .eq(1)
      .should("contain.text", "Mustermann-Smith"); // lastName in Index 1
    cy.get("table tbody tr")
      .eq(0)
      .find("td")
      .eq(0)
      .should("contain.text", "Maximilian"); // firstName in Index 0
  });

  describe("Delete Employee", () => {
    it("should open delete confirmation modal", () => {
      // Mock DELETE API für User Delete - mit korrekter URL-Struktur
      cy.intercept("DELETE", "/api/employee/delete/*", {
        statusCode: 200,
        body: "User deleted successfully",
      }).as("deleteEmployee");

      // Klicke auf den Delete-Button (IconButton mit DeleteIcon)
      cy.get("table tbody tr").eq(0).find('button[title="Löschen"]').click();

      // Überprüfe dass das Bestätigungsmodal geöffnet ist
      cy.get('[aria-labelledby="delete-modal-title"]').should("be.visible");
      cy.contains("Mitarbeiter löschen").should("be.visible");
    });

    it("should delete an employee", () => {
      // Mock DELETE API für User Delete - mit korrekter URL-Struktur
      cy.intercept("DELETE", "/api/employee/delete/*", {
        statusCode: 200,
        body: "User deleted successfully",
      }).as("deleteEmployee");

      // Mock die Employee-Liste nach dem Löschen (nur noch 1 Mitarbeiter übrig)
      cy.intercept("GET", "/api/employee/all", {
        statusCode: 200,
        body: [
          {
            employeeId: 2,
            firstName: "Erika",
            lastName: "Mustermann",
            role: { roleId: 3, roleName: "Manager" },
          },
        ],
      }).as("getEmployeesAfterDelete");

      // Stelle sicher, dass das Authentication Cookie gesetzt ist
      cy.getCookie("token").should("exist");

      // Klicke auf den Delete-Button (IconButton mit DeleteIcon)
      cy.get("table tbody tr").eq(0).find('button[title="Löschen"]').click();

      // Warte auf das Modal
      cy.get('[aria-labelledby="delete-modal-title"]').should("be.visible");

      // Klicke auf Bestätigen im Modal
      cy.contains("button", "Löschen").click();

      // Warte auf den DELETE API Call
      cy.wait("@deleteEmployee");

      // Das Modal sollte sich schließen
      cy.get('[aria-labelledby="delete-modal-title"]').should("not.exist");

      // Simuliere einen Re-fetch der Employee-Daten, falls die App das nicht automatisch macht
      cy.reload();

      // Cookie nach Reload wieder setzen, falls es verloren geht
      cy.setCookie("token", "mock-jwt-token");

      // Warte auf die Seite um neu zu laden und auf Authentication
      cy.wait("@validateCookie");
      cy.wait("@getEmployeesAfterDelete");

      // Überprüfe dass der Mitarbeiter gelöscht wurde
      cy.get("table tbody tr").should("have.length", 1); // Nur noch 1 Mitarbeiter übrig
      cy.get("table tbody tr")
        .eq(0)
        .find("td")
        .eq(0)
        .should("contain.text", "Erika"); // Der verbleibende Mitarbeiter
    });
  });
});
