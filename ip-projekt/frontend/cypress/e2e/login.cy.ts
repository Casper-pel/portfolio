describe("Login Page", () => {
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
    cy.visit("/login");
  });

  it("should render the login form", () => {
    cy.contains("Mitarbeiter-ID").should("be.visible");
    cy.get(".employee-id input").should("exist");
    cy.get('.password input').should("exist");
    cy.get('.submit-button').should("exist");
  });

  
  it("should show error if employee ID is not a number", () => {
    cy.get('.employee-id input').type("abc");
    cy.get('.password input').type("password123");
    cy.get('.submit-button').click();
    cy.contains("Ein Fehler ist aufgetreten").should("exist");
  });


  it("should show error if credentials are invalid", () => {
    cy.intercept("POST", "http://localhost:8000/api/auth/login", {
      statusCode: 401,
      body: {},
    }).as("loginRequest");

    cy.get('.employee-id input').type("12345");
    cy.get('.password input').type("wrongpassword");
    cy.get('.submit-button').click();

    cy.contains("Ein Fehler ist aufgetreten").should("exist");
  });

});
