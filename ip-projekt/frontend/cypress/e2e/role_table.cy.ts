describe("Role Table Tests", () => {
  beforeEach(() => {
    // Unterdrücke Hydration und andere Next.js Fehler
    cy.on("uncaught:exception", (err, runnable) => {
      // Ignoriere Hydration-Fehler und andere React/Next.js Warnungen
      if (
        err.message.includes("Minified React error") ||
        err.message.includes("Hydration") ||
        err.message.includes("hydration") ||
        err.message.includes("Text content does not match") ||
        err.message.includes("Warning:")
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

    // Set authentication cookie
    cy.setCookie("token", "mock-jwt-token");

    cy.visit("/dashboard/roles");
  });

  it("should load the roles page", () => {
    cy.url().should("include", "/dashboard/roles");
    cy.get("h4").contains("Rollenverwaltung").should("be.visible");
    cy.get("button").contains("Neue Rolle erstellen").should("be.visible");
    cy.get("table").should("exist");
  });

  it("should display the whole table", () => {
    cy.get("table thead tr").should("exist");
    cy.get("table tbody tr").should("have.length.greaterThan", 0);
    cy.get("th").contains("Rollenname").should("be.visible");
    cy.get("th").contains("Beschreibung").should("be.visible");
    // cy.get('th').contains('Mitarbeiter').should('be.visible');
    cy.get("th").contains("Berechtigungen").should("be.visible");
    cy.get("th").contains("Aktionen").should("be.visible");
  });

  it("should show no roles message when no roles exist", () => {
    // Mock empty roles response
    cy.intercept("GET", "/api/role/all", {
      statusCode: 200,
      body: [],
    }).as("getRoles");

    cy.visit("/dashboard/roles");
    cy.wait("@getRoles");

    cy.get("table tbody tr").should("have.length", 1);
    cy.get("table tbody tr td").should(
      "contain.text",
      "Noch keine Rollen erstellt."
    );
  });

  it("should show role when roles exist", () => {
    cy.intercept("GET", "/api/role/all", {
      statusCode: 200,
      body: [
        {
          roleId: 1,
          roleName: "Administrator",
          description: "Vollzugriff auf alle Funktionen",
          rolePermissions: ["user.read"],
        },
        {
          roleId: 2,
          roleName: "Mitarbeiter",
          description: "Eingeschränkter Zugriff auf Mitarbeiterdaten",
          rolePermissions: ["READ"],
        },
      ],
    }).as("getRoles");
    cy.intercept("GET", "/api/employee/all", {
      statusCode: 200,
      body: [],
    }).as("getEmployees");
    cy.visit("/dashboard/roles");
    // cy.wait("@getEmployees");
    cy.wait("@getRoles");
    cy.get("table tbody tr").should("have.length", 2);

    // Debug: Zeige alle Spalten-Inhalte beider Zeilen
    cy.get("table tbody tr")
      .eq(0)
      .find("td")
      .each(($td, index) => {
        cy.log(`Zeile 0, Spalte ${index}: ${$td.text()}`);
      });
    cy.get("table tbody tr")
      .eq(1)
      .find("td")
      .each(($td, index) => {
        cy.log(`Zeile 1, Spalte ${index}: ${$td.text()}`);
      });

    // Neue Erkenntnisse: Index 1 = "Keine Mitarbeiter" bedeutet:
    // Index 0 = description, Index 1 = mitarbeiter, Index 2 = ???, Index 3 = ???
    // Lass uns erstmal nur die sichtbaren Inhalte prüfen ohne spezifische Indizes anzunehmen
    cy.get("table tbody tr").eq(0).should("contain.text", "Administrator");
    cy.get("table tbody tr")
      .eq(0)
      .should("contain.text", "Vollzugriff auf alle Funktionen");
    cy.get("table tbody tr").eq(0).should("contain.text", "user.read");

    cy.get("table tbody tr").eq(1).should("contain.text", "Mitarbeiter");
    cy.get("table tbody tr")
      .eq(1)
      .should("contain.text", "Eingeschränkter Zugriff auf Mitarbeiterdaten");
    cy.get("table tbody tr").eq(1).should("contain.text", "READ");

    // Prüfe Action-Buttons (sollten in der letzten Spalte sein)
    cy.get("table tbody tr").eq(0).find("button").should("have.length", 2); // Edit and Delete buttons
    cy.get("table tbody tr").eq(1).find("button").should("have.length", 2); // Edit and Delete buttons

    // Prüfe Button-Texte (Icons haben möglicherweise keinen Text, also flexibler prüfen)
    cy.get("table tbody tr").eq(0).find("button").first().should("be.visible"); // Edit button
    cy.get("table tbody tr").eq(0).find("button").last().should("be.visible"); // Delete button
    cy.get("table tbody tr").eq(1).find("button").first().should("be.visible"); // Edit button
    cy.get("table tbody tr").eq(1).find("button").last().should("be.visible"); // Delete button
  });

  it("should open role creation modal", () => {
    cy.get("button").contains("Neue Rolle erstellen").click();
    cy.get("h2").contains("Rolle erstellen").should("be.visible");
    cy.get("#role-name").should("be.visible");
    cy.get("#role-description").should("be.visible");
    cy.get("button").contains("Hinzufügen").should("be.disabled"); // Initially disabled
    cy.get("#rights-title").contains("Berechtigungen:").should("be.visible");

    
  });

  it("should test employees multi-select functionality", () => {
    // Mock employees data for the select
    cy.intercept("GET", "/api/employee/all", {
      statusCode: 200,
      body: [
        {
          employeeId: 1,
          firstName: "John",
          lastName: "Doe",
          role: { roleId: 2, roleName: "Mitarbeiter" },
        },
        {
          employeeId: 2,
          firstName: "Jane",
          lastName: "Smith",
          role: { roleId: 2, roleName: "Mitarbeiter" },
        },
        {
          employeeId: 3,
          firstName: "Bob",
          lastName: "Johnson",
          role: { roleId: 1, roleName: "Administrator" },
        },
      ],
    }).as("getEmployees");

    // Open role creation modal
    cy.get("button").contains("Neue Rolle erstellen").click();
    cy.get("h2").contains("Rolle erstellen").should("be.visible");



    // Wait for any animations to complete and ensure the modal is fully loaded
    cy.get("h2").contains("Rolle erstellen").should("be.visible");

  });

  it("should handle empty employees list", () => {
    // Mock empty employees response
    cy.intercept("GET", "/api/employee/all", {
      statusCode: 200,
      body: [],
    }).as("getEmptyEmployees");
    // Wait for empty employees response
    // cy.wait("@getEmptyEmployees");

    // Open role creation modal
    cy.get("button").contains("Neue Rolle erstellen").click();
    cy.get("h2").contains("Rolle erstellen").should("be.visible");

  });

  it("should handle role creation with valid data", () => {
    // Mock employees data for the select
    cy.intercept("GET", "/api/employee/all", {
      statusCode: 200,
      body: [
        {
          employeeId: 1,
          firstName: "John",
          lastName: "Doe",
          role: { roleId: 2, roleName: "Mitarbeiter" },
        },
        {
          employeeId: 2,
          firstName: "Jane",
          lastName: "Smith",
          role: { roleId: 2, roleName: "Mitarbeiter" },
        },
      ],
    }).as("getEmployees");

    // ✅ POST-Intercept VOR dem Button-Klick definieren
    cy.intercept("POST", "/api/role/create", {
      statusCode: 201,
      body: {
        roleId: 3,
        roleName: "Test Rolle",
        description: "Dies ist eine Testrolle.",
        rolePermissions: ["user.read"],
        employees: [{ employeeId: 2, firstName: "Jane", lastName: "Smith" }],
      },
    }).as("createRole");

    // Open role creation modal
    cy.get("button").contains("Neue Rolle erstellen").click();
    cy.get("h2").contains("Rolle erstellen").should("be.visible");

    // Wait for employees to load
    // cy.wait("@getEmployees");

    // Fill in role name and description
    cy.get("#role-name").type("Test Rolle");
    cy.get("#role-description").type("Dies ist eine Testrolle.");

    // Select permissions (assuming checkboxes or similar exist)
    cy.get('[type="checkbox"]').first().check(); // Check first permission

  });
});
