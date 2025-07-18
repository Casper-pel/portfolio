describe('Product Graph', () => {
    beforeEach(() => {
        cy.intercept('GET', '/api/stock/*', {
            statusCode: 200,
            body: { quantity: 99 },
        }).as('getStock');

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

    it('should select a product and display its data', () => {
        // Produktnamen ins Autocomplete tippen
        cy.get('.productgraph-select input')
            .click()
            .type('Testprodukt');



    });
});
