describe("Main Page", () => {
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

    cy.visit("/");
  });
  it("should display the main page with header and navigation", () => {
    cy.get("h1").should("contain.text", "Management System");
    cy.get("h2").should(
      "contain.text",
      "Wählen Sie eine Option, um mit der Verwaltung zu beginnen oder einen Checkout durchzuführen"
    );
  });

  it("should display both cards on the main page", () => {
    // Wait for page to load completely
    cy.get("h1").should("contain.text", "Management System");

    // Debug: Log all h5 elements to see what's actually on the page
    cy.get("body").then(($body) => {
      cy.log("Page loaded, checking for cards...");
    });

    // More flexible approach - check for cards first, then content
    cy.get("div").contains("Dashboard").should("be.visible");
    cy.get("div").contains("Kasse").should("be.visible");

    // Check Dashboard Card content with more specific selectors
    cy.contains("Dashboard").should("be.visible");
    cy.contains("Mitarbeiter und Rollen verwalten").should("be.visible");
    cy.contains("Zum Dashboard").should("be.visible");

    // Check Checkout Card content
    cy.contains("Kasse").should("be.visible");
    cy.contains("Produkte verkaufen und bezahlen").should("be.visible");
    cy.contains("Zur Kasse").should("be.visible");

    // Check if buttons are present and clickable
    cy.get("button")
      .contains("Zum Dashboard")
      .should("be.visible")
      .and("not.be.disabled");
    cy.get("button")
      .contains("Zur Kasse")
      .should("be.visible")
      .and("not.be.disabled");
  });

  it("should navigate to dashboard when dashboard card is clicked", () => {
    // Click on Dashboard card
    cy.get("button").contains("Zum Dashboard").click();

    // Check if navigated to dashboard (this would be mocked or we'd check URL)
    cy.url().should("include", "/dashboard");
  });

  it("should navigate to checkout when checkout card is clicked", () => {
    // Click on Checkout card
    cy.get("button").contains("Zur Kasse").click();

    // Check if navigated to checkout/kassa page
    cy.url().should("include", "/kassa");
  });

  it("should have hover effects on cards", () => {
    // Check if cards have hover styles (Material-UI cards should be interactive)
    cy.get(".MuiCard-root").first().should("have.css", "cursor", "pointer");
    cy.get(".MuiCard-root").last().should("have.css", "cursor", "pointer");

    // Hover over first card and check for style changes
    cy.get(".MuiCard-root").first().trigger("mouseover");
  });
});
