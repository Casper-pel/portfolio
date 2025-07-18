describe('New Product Creation Form', () => {
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

  it('should fill out and submit the new product form correctly', () => {
    // Produktname
    cy.get('.new-product-name input')
      .type('Testprodukt')
      .should('have.value', 'Testprodukt');

    // Verkaufspreis
    cy.get('.new-product-price input')
      .type('99.99')
      .should('have.value', '99.99');

    // Beschreibung
    cy.get('.new-product-description input')
      .type('Dies ist ein Testprodukt.')
      .should('have.value', 'Dies ist ein Testprodukt.');

    // Einkaufspreis
    cy.get('.new-product-cost-price input')
      .type('49.99')
      .should('have.value', '49.99');

    // UPC-Code
    cy.get('.new-product-upc-code input')
      .type('123456789012')
      .should('have.value', '123456789012');

    // Initialer Lagerbestand
    cy.get('.new-product-init-stock input')
      .type('20')
      .should('have.value', '20');

    // Steuer enthalten
    cy.get('.new-product-tax-included input')
      .check({ force: true })
      .should('be.checked');

    // Aktiv
    cy.get('.new-product-active input')
      .check({ force: true })
      .should('be.checked');

    // Currency Dropdown auswählen (MUI <TextField select />)
    cy.get('.new-product-currency')
      .click()
      .then(() => {
        cy.get('[role="option"]') // MUI benutzt <li role="option">
          .contains('EUR')
          .click();
      });

    // Intercept für Produkt-Anlage
    cy.intercept('POST', '**/products/**', (req) => {
      req.reply({ statusCode: 200, body: 'test-id-123' });
    }).as('createProduct');

    // Intercept für Stock-Anlage
    cy.intercept('POST', '**/stock/**', (req) => {
      req.reply({ statusCode: 200, body: {} });
    }).as('createStock');

    // Button klicken
    cy.contains('button', 'erstellen').click();

    // Überprüfen, ob Produktdaten korrekt gesendet wurden
    cy.wait('@createProduct').its('request.body').should((body) => {
      expect(body).to.deep.include({
        productName: 'Testprodukt',
        productDescription: 'Dies ist ein Testprodukt.',
        upcCode: '123456789012',
        listPrice: 99.99,
        costPrice: 49.99,
        currency: 'EUR',
        active: true,
        taxIncludedInPrice: true,
      });
    });

    // Überprüfen, ob Lagerbestand korrekt gesendet wurde
    cy.wait('@createStock').its('request.body').should((body) => {
      expect(body).to.deep.include({
        productId: 'test-id-123',
        quantity: 20,
        repurchased: false,
        shouldBeRepurchased: true,
      });
    });
  });
});
