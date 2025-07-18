describe('BestSoldProductCard', () => {
    // Wird einmal vor allen Tests ausgeführt
    before(() => {
        cy.intercept('http://localhost:8000/api/products/all', {
            statusCode: 200,
            body: [
                {
                    "productName": "Testprodukt",
                    "productId": "prod_SbIFdi0j0KJVPp",
                    "productDescription": "produkt",
                    "listPrice": 1.1,
                    "costPrice": 0.5,
                    "upcCode": "55",
                    "created": 1751381989,
                    "updated": 1751381989,
                    "active": true,
                    "currency": "eur",
                    "taxIncludedInPrice": true
                },
                {
                    "productName": "wurst",
                    "productId": "prod_SbHWy1WyqDchAm",
                    "productDescription": "wurst",
                    "listPrice": 238,
                    "costPrice": 0,
                    "upcCode": "6",
                    "created": 1751379310,
                    "updated": 1751381944,
                    "active": true,
                    "currency": "eur",
                    "taxIncludedInPrice": false
                }
            ]
        }).as('getAllProducts');
    });

    // Wird vor jedem einzelnen `it()` ausgeführt
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

        // Dynamischer Intercept für best-selling basierend auf dem aktuellen Test
        cy.intercept('GET', '**/api/products/best-selling*', (req) => {
            // Prüfe den aktuellen Test-Titel über Cypress.currentTest
            const currentTest = Cypress.currentTest.title;

            if (currentTest.includes('empty') || currentTest.includes('no sales data')) {
                // Für Tests mit leeren Daten
                req.reply({
                    statusCode: 200,
                    body: {
                        productName: 'Testprodukt',
                        totalQuantity: 0,
                        dates: [],
                    },
                });
            } else {
                // Für normale Tests mit Daten
                req.reply({
                    statusCode: 200,
                    body: {
                        productName: 'Testprodukt',
                        totalQuantity: 42,
                        dates: [
                            '2024-01-01T00:00:00Z',
                            '2024-01-01T00:00:00Z',
                            '2024-01-02T00:00:00Z',
                            '2024-01-03T00:00:00Z',
                        ],
                    },
                });
            }
        }).as('getBestSold');

        // Set authentication cookie
        cy.setCookie('token', 'mock-jwt-token');

        cy.visit('/dashboard/products');
    });

    it('displays product information and updates on time selection', () => {
        cy.intercept('GET', '**/api/products/best-selling*', (req) => {
            req.reply({
                statusCode: 200,
                body: {
                    productName: 'Testprodukt',
                    totalQuantity: 42,
                    dates: [
                        '2024-01-01T00:00:00Z',
                        '2024-01-01T00:00:00Z',
                        '2024-01-02T00:00:00Z',
                        '2024-01-03T00:00:00Z',
                    ],
                },
            });
        }).as('getBestSold');

        cy.wait('@getBestSold');

        cy.contains('Am besten verkauftes Produkt').should('be.visible');
        cy.contains('Produktname: Testprodukt').should('be.visible');
        cy.contains('Verkäufe in letzte Woche: 42').should('be.visible');

        cy.get('.bestsold-select input').click();
        cy.get('[role="listbox"] [role="option"]').eq(1).click();
        cy.wait('@getBestSold');
        cy.contains('Verkäufe in letzter Monat: 42').should('be.visible');
    });

    it('shows empty chart and placeholder if no sales data', () => {
        // Definiere den Intercept BEVOR die Seite besucht wird
        cy.intercept('GET', '**/api/products/best-selling*', (req) => {
            req.reply({
                statusCode: 200,
                body: {
                    productName: 'Testprodukt',
                    totalQuantity: 0,
                    dates: [], // Leeres Array
                },
            });
        }).as('getBestSoldEmpty');

        // Besuche die Seite erneut, damit der neue Intercept verwendet wird
        cy.reload();
        cy.wait('@getBestSoldEmpty');

        // Wenn dates leer ist, zeigt die Komponente "—" anstatt des Produktnamens
        cy.contains('Produktname: —').should('be.visible');
        cy.contains('Verkäufe in letzte Woche: 0').should('be.visible');
    });
});
