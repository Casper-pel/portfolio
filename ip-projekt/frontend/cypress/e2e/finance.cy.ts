describe("Finance Dashboard", () => {
  beforeEach(() => {
    cy.intercept('POST', '/api/auth/cookie-validation', {
      statusCode: 200,
      body: {
        accessRights: ['admin'], // Mocked access rights for admin
      },
    }).as('validateCookie');
    cy.intercept("GET", "/api/employee/current", {
      statusCode: 200,
      body: {
        employeeId: 10000,
        firstName: "Admin",
        lastName: "User",
        role: { roleId: 1, roleName: "Administrator" },
      },
    }).as("getCurrentUser");
    cy.intercept("GET", "**/employee/all", {
      statusCode: 200,
      body: [
        { employeeId: 1, firstName: "Max", lastName: "Mustermann" },
        { employeeId: 2, firstName: "Erika", lastName: "Musterfrau" },
      ],
    }).as("getEmployees");

    cy.intercept("GET", /order\/between.*/, {
      statusCode: 200,
      body: [{ totalPrice: 100 }, { totalPrice: 50 }],
    }).as("getOrders");

    cy.visit("/dashboard/finance");
  });

  it("should render the finance dashboard", () => {
    cy.contains("Finanzübersicht").should("be.visible");
    cy.contains("Zeitspanne").should("be.visible");
    cy.contains("Mitarbeiter").should("be.visible");
    cy.get("button").contains("Daten laden").should("exist");
    cy.contains("Umsatz").should("exist");
    cy.contains("Transaktionen").should("exist");
    cy.contains("Transaktionswert").should("exist");
  });

  it("should load data and update cards", () => {
    cy.get("button").contains("Daten laden").click();
    cy.wait("@getOrders");
    cy.contains("150.00 €").should("exist"); 
    cy.contains("2").should("exist"); 
    cy.contains("75.00 €").should("exist"); 
  });

  it("should allow selecting a different employee", () => {
    cy.get("label")
      .contains("Mitarbeiter")
      .parent()
      .find(".MuiSelect-select")
      .click();
    cy.contains("Max Mustermann").click();
    cy.get("button").contains("Daten laden").click();
    cy.wait("@getOrders");
    cy.contains("150.00 €").should("exist");
  });

  it("should allow changing the time range to custom and pick dates", () => {
    cy.get(".time-range").click();
    cy.contains("Benutzerdefiniert").click();
    cy.get('.start-date').first().type("01/01/2024");
    cy.get('.end-date').last().type("31/01/2024");
    cy.get("button").contains("Daten laden").click();
    cy.wait("@getOrders");
    cy.contains("150.00 €").should("exist");
  });

  it("should show 0 € and 0 transactions if no orders", () => {
    cy.intercept("GET", /order\/between.*/, {
      statusCode: 200,
      body: [],
    }).as("getEmptyOrders");
    cy.get("button").contains("Daten laden").click();
    cy.wait("@getEmptyOrders");
    cy.contains("0.00 €").should("exist");
    cy.contains("0").should("exist");
  });
});
