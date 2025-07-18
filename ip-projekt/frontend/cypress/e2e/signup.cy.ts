describe("Signup Page", () => {
  beforeEach(() => {
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
    cy.intercept("GET", "**/role/all", {
      statusCode: 200,
      body: [
        { roleId: 1, roleName: "Admin" },
        { roleId: 2, roleName: "Mitarbeiter" },
      ],
    }).as("getRoles");

    // Mock Authentication zuerst
    cy.intercept("POST", "/api/auth/cookie-validation", {
      statusCode: 200,
      body: {
        accessRights: ["admin"], // Mocked access rights for admin
      },
    }).as("validateCookie");
      cy.visit("/signup");

  });

  it("should render the signup form", () => {
    cy.contains("Neuen Nutzer anlegen").should("be.visible");
    cy.get(".first-name input").should("exist");
    cy.get(".last-name input").should("exist");
    cy.get(".employee-id input").should("exist");
    cy.get(".password input").should("exist");
    cy.get(".repeat-password input").should("exist");
    cy.get(".submit-button").should("contain", "Nutzer anlegen");
  });

  it("should show error if Mitarbeiter ID is not a number", () => {
    cy.wait(5000);
    cy.get(".first-name input").type("Max");
    cy.get(".last-name input").type("Mustermann");
    cy.get(".employee-id input").type("abc");
    cy.get(".password input").type("test1234");
    cy.get(".repeat-password input").type("test1234");
    cy.get(".submit-button").click();
    cy.contains("ID muss eine Zahl sein!").should("exist");
  });

  it("should show error if passwords do not match", () => {
    cy.wait(5000);
    cy.get(".first-name input").type("Max");
    cy.get(".last-name input").type("Mustermann");
    cy.get(".employee-id input").type("123");
    cy.get(".password input").type("test1234");
    cy.get(".repeat-password input").type("test5678");
    cy.get(".submit-button").click();
    cy.contains("Passwörter sind nicht identisch!").should("exist");
  });

  it("should show error if backend returns error", () => {
    cy.intercept("POST", "**/auth/signup", {
      statusCode: 400,
      body: { success: false, error: "Fehler beim Anlegen" },
    }).as("signupRequest");

    cy.wait(3000);

    cy.get(".first-name input").type("Max");
    cy.get(".last-name input").type("Mustermann");
    cy.get(".employee-id input").type("123");
    cy.get(".password input").type("test1234");
    cy.get(".repeat-password input").type("test1234");
    cy.get(".role").click();
    cy.contains("Admin").click();
    cy.get(".submit-button").click();

    cy.contains("Fehler beim Anlegen").should("exist");
  });

  it("should redirect to employees dashboard on successful signup", () => {
    cy.intercept("POST", "**/auth/signup", {
      statusCode: 200,
      body: { success: true },
    }).as("signupRequest");

    cy.get(".first-name input").type("Max");
    cy.get(".last-name input").type("Mustermann");
    cy.get(".employee-id input").type("123");
    cy.get(".password input").type("test1234");
    cy.get(".repeat-password input").type("test1234");
    cy.get(".role").click();
    cy.contains("Admin").click();
    cy.get(".submit-button").click();

    cy.url().should("include", "/dashboard/employees");
  });

  it("should close the error message when clicking the close button", () => {
    cy.wait(3000);               
    cy.get(".close").click();
    cy.url().should("include", "/dashboard/employees");
  });
});
