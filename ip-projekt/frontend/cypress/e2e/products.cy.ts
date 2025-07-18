describe('template spec', () => {
  before(() => {
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
  })
  it('passes', () => {
    cy.visit("/dashboard/products");
  })
})