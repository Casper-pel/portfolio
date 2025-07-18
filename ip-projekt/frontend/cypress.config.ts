import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    baseUrl: "http://localhost:3000",
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
    // Deaktiviere Browser-Warnungen und Fehler
    chromeWebSecurity: false,
    video: false,
    screenshotOnRunFailure: false,
  },

  // Globale Einstellungen um Hydration-Fehler zu unterdrücken
  env: {
    // Deaktiviere Next.js Hydration-Warnungen
    NEXT_TELEMETRY_DISABLED: 1,
  },

  component: {
    devServer: {
      framework: "next",
      bundler: "webpack",
    },
  },
});



