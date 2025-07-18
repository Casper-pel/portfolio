// cypress/e2e/price_history.cy.ts

describe("Price History Chart", () => {
    beforeEach(() => {
        cy.intercept('POST', '/api/auth/cookie-validation', {
            statusCode: 200,
            body: { accessRights: ["admin"] },
        }).as('validateCookie');

        cy.intercept('GET', '/api/employee/current', {
            statusCode: 200,
            body: {
                employeeId: 10000,
                firstName: 'Admin',
                lastName: 'User',
                role: { roleId: 1, roleName: 'Administrator' },
            },
        }).as('getCurrentUser');

        cy.setCookie('token', 'mock-jwt-token');

        cy.visit("/dashboard/products");
    });

    it("zeigt die Preisveränderungen eines Produkts", () => {
        cy.contains("Preisdaten werden geladen...").should("exist");



        cy.contains("Preis").should("exist");
    });
});
