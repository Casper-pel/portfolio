describe("Coupon View Page", () => {
  beforeEach(() => {

    cy.on("uncaught:exception", (err) => {
      if (
        err.message.includes("Minified React error") ||
        err.message.includes("Hydration") ||
        err.message.includes("hydration") ||
        err.message.includes("Text content does not match") ||
        err.message.includes("Warning")
      ) {
        return false;
      }
      return true;
    });
    cy.intercept("GET", "/api/coupon/all", {
      statusCode: 200,
      body: [
        {
          id: "coupon1",
          name: "Sommer-Rabatt",
          amountOff: 500,
          currency: "EUR",
          duration: "once",
          percentOff: 0,
        },
        {
          id: "coupon2",
          name: "Neukunden",
          amountOff: 0,
          currency: "EUR",
          duration: "repeating",
          percentOff: 20,
        },
      ],
    }).as("getCoupons");

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

    cy.setCookie("token", "mock-jwt-token");
  });



  it("zeigt Coupons korrekt oder alternativ eine leere Nachricht an", () => {
    // Test mit vorhandenen Coupons
    cy.visit("/dashboard/coupons");
    cy.wait("@getCoupons");
    cy.wait("@getCurrentUser");

    // cy.get("h1").should("contain.text", "Coupon View");
    cy.get("table").should("exist");
    cy.get("thead tr").should("have.length", 1);
    cy.get("tbody tr").should("have.length", 2);

    cy.contains("Sommer-Rabatt").should("exist");
    cy.contains("Neukunden").should("exist");
    cy.contains("5").should("exist"); // 500 Cent als Euro-Anzeige?
    cy.contains("20%").should("exist");

    // Test mit leerer Coupon-Liste
    cy.intercept("GET", "/api/coupon/all", {
      statusCode: 200,
      body: [],
    }).as("getEmptyCoupons");

    cy.visit("/dashboard/coupons");
    cy.wait("@getEmptyCoupons");
    cy.wait("@getCurrentUser");

    cy.contains("Keine Coupons vorhanden").should("exist");
  });
});
