// http://localhost:3000/dashboard/urlaub

describe('Main Page', () => {
    beforeEach(() => {
        // Unterdrücke Hydration und andere Next.js Fehler
        cy.on('uncaught:exception', (err, runnable) => {
            // Ignoriere Hydration-Fehler und andere React/Next.js Warnungen
            if (err.message.includes('Hydration') ||
                err.message.includes('hydration') ||
                err.message.includes('Text content does not match') ||
                err.message.includes('Warning:')) {
                return false;
            }
            // Lass andere Fehler durch
            return true;
        });

        // Mock Authentication zuerst - Backend gibt "Cookie valid" als String zurück
        cy.intercept('POST', '/api/auth/cookie-validation', {
            statusCode: 200,
            body: { accessRights: ["urlaub.read", "urlaub.review"] },
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

        // Set authentication cookie
        cy.setCookie('token', 'mock-jwt-token');

        cy.visit('/dashboard/urlaub');
    });

    it('should display the title and tabs', () => {
        cy.get('h4').should('contain.text', 'Urlaubsverwaltung');

        // Check if tabs container exists
        cy.get('[role="tablist"], .MuiTabs-root').should('be.visible');

        // Check if individual tabs exist and are visible
        cy.get('[role="tab"]').should('have.length.greaterThan', 0);

        // Check for specific tab names (adjust based on your actual tabs)
        cy.get('[role="tab"]').contains('Neuen Antrag stellen').should('be.visible');
        cy.get('[role="tab"]').contains('Meine Anträge').should('be.visible');
        cy.get('[role="tab"]').contains('Anträge verwalten').should('be.visible');
        // Add more tabs as needed based on your implementation

        // Check if tabs are clickable (not disabled)
        cy.get('[role="tab"]').first().should('not.be.disabled');
        cy.get('[role="tab"]').last().should('not.be.disabled');
    });

    it('should have working tab navigation', () => {
        // Click on the first tab and verify it's selected
        cy.get('[role="tab"]').first().click();
        cy.get('[role="tab"]').first().should('have.attr', 'aria-selected', 'true');

        // Click on the second tab and verify it's selected
        cy.get('[role="tab"]').eq(1).click();
        cy.get('[role="tab"]').eq(1).should('have.attr', 'aria-selected', 'true');

        // Click on the third tab and verify it's selected
        cy.get('[role="tab"]').eq(2).click();
        cy.get('[role="tab"]').eq(2).should('have.attr', 'aria-selected', 'true');

        // Verify the first tab is no longer selected
        cy.get('[role="tab"]').first().should('have.attr', 'aria-selected', 'false');
    });

    it('should display correct tab content when switching tabs', () => {
        // Click on different tabs and verify content changes
        cy.get('[role="tab"]').contains('Meine Anträge').click();
        cy.get('[role="tabpanel"]').should('be.visible');
        cy.get('[role="tabpanel"]').should('contain.text', 'Meine Urlaubsanträge');

        cy.get('[role="tab"]').contains('Anträge verwalten').click();
        cy.get('[role="tabpanel"]').should('be.visible');
        cy.get('[role="tabpanel"]').should('contain.text', 'Urlaubsanträge');

        cy.get('[role="tab"]').contains('Neuen Antrag stellen').click();
        cy.get('[role="tabpanel"]').should('be.visible');
        cy.get('[role="tabpanel"]').should('contain.text', 'Urlaubsantrag einreichen');
    });

    it('should have accessible tab structure', () => {
        // Check if tabs have proper ARIA attributes
        cy.get('[role="tablist"]').should('exist');
        cy.get('[role="tab"]').should('have.length.greaterThan', 0);
        cy.get('[role="tabpanel"]').should('exist');

        // Check if tabs have proper accessibility attributes
        cy.get('[role="tab"]').each(($tab) => {
            cy.wrap($tab).should('have.attr', 'aria-selected');
            cy.wrap($tab).should('have.attr', 'aria-controls');
        });

        // Check if tab panels have proper accessibility attributes
        cy.get('[role="tabpanel"]').should('have.attr', 'aria-labelledby');
    });

    it('should show correct visual feedback for active tab', () => {
        // Check if active tab has visual indicators (Material-UI classes)
        cy.get('[role="tab"][aria-selected="true"]').should('have.class', 'Mui-selected');

        // Click on a different tab and check if visual state changes
        cy.get('[role="tab"]:not([aria-selected="true"])').first().click();
        cy.get('[role="tab"][aria-selected="true"]').should('have.class', 'Mui-selected');
    });
})