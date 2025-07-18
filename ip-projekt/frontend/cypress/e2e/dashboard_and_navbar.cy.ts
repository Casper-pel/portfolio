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

        // Set authentication cookie
        cy.setCookie('token', 'mock-jwt-token');

        cy.visit('/dashboard');
    });

    it('should welcome the user with a header and description', () => {
        cy.get('h1').should('contain.text', 'Willkommen, Admin User!');
        cy.get('p').should('contain.text', 'Schön, Sie wieder zu sehen.');
    });

    it('should display the navbar with correct links', () => {
        // Check if AppBar is visible
        cy.get('[role="banner"], .MuiAppBar-root').should('be.visible');

        // Check for BINGO logo/title
        cy.get('a').contains('BINGO').should('be.visible');

        // Check navigation buttons (desktop view)
        cy.get('button').contains('Produkte').should('be.visible');
        cy.get('button').contains('Mitarbeiter').should('be.visible');
        cy.get('button').contains('Finanzen').should('be.visible');
        cy.get('button').contains('Rollen').should('be.visible');
        cy.get('button').contains('Urlaub').should('be.visible');

        // Check if user avatar/menu is present
        cy.get('[data-testid="avatar"], .MuiAvatar-root').should('be.visible');

        // Check if user name is displayed (Admin User from mock)
        cy.contains('Admin User').should('be.visible');
    });

    it('should have working navigation buttons', () => {
        // Test Produkte navigation
        cy.get('button').contains('Rollen').click();
        cy.wait(1000); // Wait for roles to load
        cy.url().should('include', '/dashboard/roles');

        // Go back to dashboard
        cy.visit('/dashboard');

        // Test Mitarbeiter navigation
        cy.get('button').contains('Mitarbeiter').click();
        cy.url().should('include', '/dashboard/employees');

        // Go back to dashboard
        cy.visit('/dashboard');

        // Test Rollen navigation
        cy.get('button').contains('Rollen').click();
        cy.url().should('include', '/dashboard/roles');
    });

    it('should have working user menu', () => {
        // Click on user avatar to open menu
        cy.get('.MuiAvatar-root').click();

        // Check if user menu is open and contains expected items
        cy.get('[role="menu"]').should('be.visible');
        cy.get('[role="menuitem"]').contains('Menü').should('be.visible');
        cy.get('[role="menuitem"]').contains('Kasse').should('be.visible');
        cy.get('[role="menuitem"]').contains('Logout').should('be.visible');

        // Test Kasse navigation from menu
        cy.get('[role="menuitem"]').contains('Kasse').click();
        cy.url().should('include', '/kassa');
    });

    it('should display user information correctly', () => {
        // Wait for user data to load
        cy.wait('@getCurrentUser');

        // Check if user name is displayed in the navbar
        cy.contains('Admin User').should('be.visible');

        // Check if avatar has correct initials
        cy.get('.MuiAvatar-root').should('contain.text', 'AU'); // Admin User initials

        // Hover over avatar to see tooltip
        cy.get('.MuiAvatar-root').trigger('mouseover');
        cy.get('[role="tooltip"]').should('contain.text', 'Admin User');
    });
})