describe('Produkt-Update – Kein Produkt auswählbar', () => {
  beforeEach(() => {
    cy.intercept('GET', '/api/stock/*').as('getStock');
    cy.intercept('PUT', '/api/products/*').as('updateProduct');
    cy.intercept('POST', '/api/stock').as('addStock');

    cy.intercept('GET', '/api/products/**').as('searchProducts');

    cy.setCookie('token', 'mock-jwt-token');
    cy.visit('/dashboard/products');
  });

  it('soll keine Produktauswahl ermöglichen und keine Requests senden', () => {
    // Prüfen, dass das Autocomplete-Eingabefeld nicht existiert oder deaktiviert ist
    cy.get('.product-update-autocomplete input').should('not.exist');

    // Alternativ: falls es existiert, aber disabled ist
    // cy.get('.product-update-autocomplete input').should('be.disabled');

    // Warte kurz, um sicherzustellen, dass kein Request gesendet wird
    cy.wait(1000);

    // Sicherstellen, dass KEIN Netzwerkrequest ausgelöst wurde
    cy.get('@getStock.all').should('have.length', 0);
    cy.get('@updateProduct.all').should('have.length', 0);
    cy.get('@addStock.all').should('have.length', 0);
  });
});
