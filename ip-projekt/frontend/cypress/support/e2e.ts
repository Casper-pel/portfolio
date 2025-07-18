// ***********************************************************
// This example support/e2e.ts is processed and
// loaded automatically before your test files.
//
// This is a great place to put global configuration and
// behavior that modifies Cypress.
//
// You can change the location of this file or turn off
// automatically serving support files with the
// 'supportFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/configuration
// ***********************************************************

// Import commands.js using ES2015 syntax:
import './commands'

// Globale Exception-Handler für alle Tests
Cypress.on('uncaught:exception', (err, runnable) => {
    // Ignoriere Hydration-Fehler und andere React/Next.js Warnungen
    if (err.message.includes('Hydration') || 
        err.message.includes('hydration') ||
        err.message.includes('Text content does not match') ||
        err.message.includes('Warning:') ||
        err.message.includes('ResizeObserver') ||
        err.message.includes('Non-Error promise rejection')) {
        return false;
    }
    // Lass andere Fehler durch
    return true;
});
