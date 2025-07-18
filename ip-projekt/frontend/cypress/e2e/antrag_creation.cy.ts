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

    it('should display the correct content', () => {
        cy.get('h6').should('contain.text', 'Urlaubsantrag einreichen').should('be.visible');
        cy.get("#info-text").should('contain.text', "Stellen Sie hier Ihren Urlaubsantrag. Alle Anträge werden von Ihrem Vorgesetzten geprüft.").should('be.visible');
        cy.get('button').contains('Urlaubsantrag').should('be.visible');
    })

    it('should open modal when clicking "Urlaubsantrag"', () => {
        cy.get('button').contains('Urlaubsantrag').click();
        cy.wait(300); // Wait for modal to open
        cy.get('h2').should('contain.text', 'Stelle deinen Urlaubsantrag').should('be.visible');
        cy.get('button').contains('Abbrechen').should('be.visible');
        cy.get('button').contains('Antrag einreichen').should('be.visible');
    });

    it('einreich-button nicht klickbar wenn Formular unvollständig ist', () => {
        cy.get('button').contains('Urlaubsantrag').click();
        // sofort auf Einreichen klicken
        cy.get('button').contains('Antrag einreichen').should('be.disabled');
    });

    it('should submit the form successfully with className selectors', () => {
        cy.intercept('POST', '/api/urlaubsantrag/add', {
            statusCode: 200,
            body: {
                success: true,
                message: 'Ihr Urlaubsantrag wurde erfolgreich eingereicht!'
            }
        }).as('submitAntrag');
        // Modal öffnen
        cy.get('button').contains('Urlaubsantrag').click();

        // Startdatum setzen
        cy.get('.start-date-picker').type('10072028');

        // Enddatum setzen
        cy.get('.end-date-picker').type('15072028');

        // Art auswählen
        cy.get('#art-select').click();
        cy.get('li').contains('Erholungsurlaub').click();

        // Grund eingeben
        cy.get('input[placeholder="Grund für Ihren Urlaubsantrag..."]')
            .type('Familienurlaub');

        // Einreichen-Button prüfen und klicken
        cy.get('button').contains('Antrag einreichen')
            .should('not.be.disabled')
            .click();

        // Erfolgsmeldung prüfen
        cy.get('.MuiAlert-root')
            .should('contain.text', 'Ihr Urlaubsantrag wurde erfolgreich eingereicht!');
    });

    it('fehler wenn start datum in der vergangenheit', () => {
        cy.intercept('POST', '/api/urlaubsantrag/add', {
            statusCode: 400,
            body: {
                success: false,
                message: 'Ungültige Datumsangaben'
            }
        }).as('submitAntragError');

        // Modal öffnen
        cy.get('button').contains('Urlaubsantrag').click();

        // Startdatum in der Vergangenheit setzen
        cy.get('.start-date-picker').type('01012020');

        // Enddatum in der Zukunft setzen
        cy.get('.end-date-picker').type('01012030');

        // Art auswählen
        cy.get('#art-select').click();
        cy.get('li').contains('Erholungsurlaub').click();

        // Grund eingeben
        cy.get('input[placeholder="Grund für Ihren Urlaubsantrag..."]')
            .type('Test');

        // Einreichen-Button klicken
        cy.get('button').contains('Antrag einreichen').click();

        // Fehlermeldung prüfen
        cy.get('.MuiAlert-root')
            .should('contain.text', 'Das Startdatum darf nicht in der Vergangenheit liegen');
    });

    it('fehler wenn start datum nach end datum', () => {
        cy.intercept('POST', '/api/urlaubsantrag/add', {
            statusCode: 400,
            body: {
                success: false,
                message: 'Ungültige Datumsangaben'
            }
        }).as('submitAntragError');

        // Modal öffnen
        cy.get('button').contains('Urlaubsantrag').click();

        // Startdatum in der Zukunft setzen
        cy.get('.start-date-picker').type('01012030');

        // Enddatum in der Vergangenheit setzen
        cy.get('.end-date-picker').type('01012020');

        // Art auswählen
        cy.get('#art-select').click();
        cy.get('li').contains('Erholungsurlaub').click();

        // Grund eingeben
        cy.get('input[placeholder="Grund für Ihren Urlaubsantrag..."]')
            .type('Test');

        // Einreichen-Button klicken
        cy.get('button').contains('Antrag einreichen').click();

        // Fehlermeldung prüfen
        cy.get('.MuiAlert-root')
            .should('contain.text', 'Das Enddatum muss nach dem Startdatum liegen');
    });
})