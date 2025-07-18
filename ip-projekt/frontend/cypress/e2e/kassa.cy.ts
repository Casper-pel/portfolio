describe("Kassa", () => {
  beforeEach(() => {
    cy.on("uncaught:exception", (err) => {
      if (
        err.message.includes("Minified React error") ||
        err.message.includes("Hydration") ||
        err.message.includes("hydration") ||
        err.message.includes("Text content does not match") ||
        err.message.includes("Warning")
      ) {
        return false;
      }
      return true;
    });

    cy.intercept("POST", "/api/auth/cookie-validation", {
      statusCode: 200,
      body: {
        accessRights: ["admin"],
      },
    }).as("validateCookie");
    cy.intercept("GET", "/api/employee/current", {
      statusCode: 200,
      body: {
        employeeId: 10000,
        firstName: "Admin",
        lastName: "User",
        role: { roleId: 1, roleName: "Administrator" },
      },
    }).as("getCurrentUser");

    cy.setCookie("token", "mock-jwt-token");

    cy.visit("kassa");
  });

  it("should display the Kassa page", () => {
    cy.contains("BINGO").should("be.visible");
  });

  it("should allow entering an EAN and add product to cart", () => {
    cy.intercept("GET", "**/products/cache/product/ean/1234567890123", {
      statusCode: 200,
      body: {
        productName: "test",
        productId: "test",
        productDescription: "testproduct",
        price: "preis",
        listPrice: 6,
        costPrice: 5,
        upcCode: "1234567890123",
        created: 0,
        updated: 0,
        active: true,
        currency: "euro",
        taxIncludedInPrice: true,
      },
    }).as("getProductByEAN");

    cy.get(".ean-input input").click();
    cy.get(".ean-input input").type("1234567890123");
    cy.get(".ean-input input").should("have.value", "1234567890123");

    cy.get(".ean-input input").type("{enter}");

    cy.contains("test").should("exist");
  });

  it("should allow changing quantity in cart", () => {
    cy.intercept("GET", "**/products/cache/product/ean/1234567890123", {
      statusCode: 200,
      body: {
        productName: "test",
        productId: "test",
        productDescription: "testproduct",
        price: "preis",
        listPrice: 6,
        costPrice: 5,
        upcCode: "1234567890123",
        created: 0,
        updated: 0,
        active: true,
        currency: "euro",
        taxIncludedInPrice: true,
      },
    }).as("getProductByEAN");

    cy.get(".ean-input input").click();
    cy.get(".ean-input input").type("1234567890123");
    cy.get(".ean-input input")
      .should("have.value", "1234567890123")
      .type("{enter}");

    cy.get(".cart-quantity-box").click().type("3").type("{enter}");
    cy.get(".cart-quantity-box").contains("3").should("exist");
  });

  it("should show error for invalid EAN", () => {
    cy.get(".ean-input input").click();

    cy.get(".ean-input input").type("invalid-ean");
    cy.contains("Nur Zahlen erlaubt!").should("exist");
  });

  it("should allow user to open user menu and logout", () => {
    cy.wait("@getCurrentUser");
    cy.get(".icon-button").click();
    cy.contains("Logout").click();
    cy.wait(3000);
    cy.url().should("include", "/login");
  });

  it("should allow removing a product from the cart", () => {
    cy.intercept("GET", "**/products/cache/product/ean/1234567890123", {
      statusCode: 200,
      body: {
        productName: "test",
        productId: "test",
        productDescription: "testproduct",
        price: "preis",
        listPrice: 6,
        costPrice: 5,
        upcCode: "1234567890123",
        created: 0,
        updated: 0,
        active: true,
        currency: "euro",
        taxIncludedInPrice: true,
      },
    }).as("getProductByEAN");

    cy.get(".ean-input input").click();
    cy.get(".ean-input input").type("1234567890123{enter}");
    cy.contains("test").should("exist");

    cy.get(".cart-item-name").click();
    cy.get(".delete-button").click();
    cy.contains("test").should("not.exist");
  });

  it("should allow editing the discount of a cart item", () => {
    cy.intercept("GET", "**/products/cache/product/ean/1234567890123", {
      statusCode: 200,
      body: {
        productName: "test",
        productId: "test",
        productDescription: "testproduct",
        price: "preis",
        listPrice: 6,
        costPrice: 5,
        upcCode: "1234567890123",
        created: 0,
        updated: 0,
        active: true,
        currency: "euro",
        taxIncludedInPrice: true,
      },
    }).as("getProductByEAN");

    cy.get(".ean-input input").click();
    cy.get(".ean-input input").type("1234567890123");
    cy.get(".ean-input input")
      .should("have.value", "1234567890123")
      .type("{enter}");

    cy.get(".cart-discount-box").click().type("10").type("{enter}");
    cy.get(".cart-discount-box").contains("10").should("exist");
  });

  it("should update the total price when quantity changes", () => {
    cy.intercept("GET", "**/products/cache/product/ean/1234567890123", {
      statusCode: 200,
      body: {
        productName: "test",
        productId: "test",
        productDescription: "testproduct",
        price: "preis",
        listPrice: 6,
        costPrice: 5,
        upcCode: "1234567890123",
        created: 0,
        updated: 0,
        active: true,
        currency: "euro",
        taxIncludedInPrice: true,
      },
    }).as("getProductByEAN");

    cy.get(".ean-input input").click();
    cy.get(".ean-input input").type("1234567890123");
    cy.get(".ean-input input")
      .should("have.value", "1234567890123")
      .type("{enter}");

    cy.get(".cart-quantity-box").click().type("2").type("{enter}");
    cy.get(".total-price").contains("12").should("exist");
  });

  it("should clear the cart when clicking the clear button", () => {
    cy.intercept("GET", "**/products/cache/product/ean/1234567890123", {
      statusCode: 200,
      body: {
        productName: "test",
        productId: "test",
        productDescription: "testproduct",
        price: "preis",
        listPrice: 6,
        costPrice: 5,
        upcCode: "1234567890123",
        created: 0,
        updated: 0,
        active: true,
        currency: "euro",
        taxIncludedInPrice: true,
      },
    }).as("getProductByEAN");

    cy.get(".ean-input input").click();
    cy.get(".ean-input input").type("1234567890123");
    cy.get(".ean-input input")
      .should("have.value", "1234567890123")
      .type("{enter}");

    cy.get(".delete-cart-button").click();
    cy.contains("test").should("not.exist");
  });

  it("should increase quantity when the same EAN is entered twice", () => {
    cy.intercept("GET", "**/products/cache/product/ean/1234567890123", {
      statusCode: 200,
      body: {
        productName: "test",
        productId: "test",
        productDescription: "testproduct",
        price: "preis",
        listPrice: 6,
        costPrice: 5,
        upcCode: "1234567890123",
        created: 0,
        updated: 0,
        active: true,
        currency: "euro",
        taxIncludedInPrice: true,
      },
    }).as("getProductByEAN");

    cy.get(".ean-input input").click();
    cy.get(".ean-input input").type("1234567890123{enter}");
    cy.get(".ean-input input").type("1234567890123{enter}");

    cy.get(".cart-quantity-box").should("contain", "2");
  });

  it("should enter numbers using the numpad and add product to cart", () => {
    cy.intercept("GET", "**/products/cache/product/ean/9876543210", {
      statusCode: 200,
      body: {
        productName: "numpad-test",
        productId: "numpad-test",
        productDescription: "testproduct",
        price: "preis",
        listPrice: 10,
        costPrice: 8,
        upcCode: "9876543210",
        created: 0,
        updated: 0,
        active: true,
        currency: "euro",
        taxIncludedInPrice: true,
      },
    }).as("getProductByEAN");

    // Warte darauf, dass die Seite vollständig geladen ist
    cy.wait(1000);
    cy.get(".numpad-toggle-button").should("be.visible").click({ force: true });
    cy.wait(1000); // Warte, damit das Numpad sichtbar ist

    cy.get(".numpad-button-9").click();
    cy.get(".numpad-button-8").click();
    cy.get(".numpad-button-7").click();
    cy.get(".numpad-button-6").click();
    cy.get(".numpad-button-5").click();
    cy.get(".numpad-button-4").click();
    cy.get(".numpad-button-3").click();
    cy.get(".numpad-button-2").click();
    cy.get(".numpad-button-1").click();
    cy.get(".numpad-button-0").click();

    cy.get(".numpad-button-enter").click();

    cy.contains("numpad-test").should("exist");
  });

  it("should add the same article twice as separate items when grouping is disabled", () => {
    cy.intercept("GET", "**/products/cache/product/ean/5555555555555", {
      statusCode: 200,
      body: {
        productName: "group-test",
        productId: "group-test",
        productDescription: "testproduct",
        price: "preis",
        listPrice: 7,
        costPrice: 5,
        upcCode: "5555555555555",
        created: 0,
        updated: 0,
        active: true,
        currency: "euro",
        taxIncludedInPrice: true,
      },
    }).as("getProductByEAN");

    cy.wait(3000);

    cy.get(".group-button").click();

    // Füge denselben Artikel zweimal hinzu
    cy.get(".ean-input input").click();
    cy.get(".ean-input input").type("5555555555555{enter}");
    cy.get(".ean-input input").type("5555555555555{enter}");

    // Es sollten jetzt zwei Einträge mit dem Namen "group-test" im Warenkorb sein
    cy.get(".cart-item").first().contains("1").should("exist");
  });
});
