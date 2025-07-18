package ip.project.backend.backend.security;

import ip.project.backend.backend.checker.PermissionManager;
import ip.project.backend.backend.model.Employee;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DynamicAuthorizationManagerTest {

    @Mock
    PermissionManager permissionManager;

    @InjectMocks
    DynamicAuthorizationManager authorizationManager;

    @Mock
    RequestAuthorizationContext context;

    @Mock
    HttpServletRequest request;

    @Mock
    Supplier<Authentication> authenticationSupplier;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(context.getRequest()).thenReturn(request);
    }

    @Test
    void check_noCurrentUser_returnsFalse() {
        when(request.getRequestURI()).thenReturn("/some/path");
        when(request.getMethod()).thenReturn("GET");
        when(request.getAttribute("currentUser")).thenReturn(null);

        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        assertFalse(decision.isGranted());
    }

    @Test
    void check_adminUser_returnsTrue() {
        // Mock required permission
        when(request.getRequestURI()).thenReturn("/api/products");
        when(request.getMethod()).thenReturn("GET");
        when(permissionManager.findRequiredPermission("/api/products", "GET")).thenReturn("product:view");

        // Mock current user with 'admin' role
        Employee admin = mock(Employee.class);
        when(admin.getRolePermissions()).thenReturn(List.of("admin"));
        when(request.getAttribute("currentUser")).thenReturn(admin);

        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        assertTrue(decision.isGranted());
    }

    @Test
    void check_noRequiredPermission_returnsFalse() {
        Employee user = mock(Employee.class);
        when(user.getRolePermissions()).thenReturn(List.of("some.permission"));
        when(request.getAttribute("currentUser")).thenReturn(user);

        when(request.getRequestURI()).thenReturn("/some/path");
        when(request.getMethod()).thenReturn("GET");
        when(permissionManager.findRequiredPermission("/some/path", "GET")).thenReturn(null);

        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        assertFalse(decision.isGranted());
    }

    @Test
    void check_userHasPermission_returnsTrue() {
        Employee user = mock(Employee.class);
        when(user.getRolePermissions()).thenReturn(List.of("required.permission", "other.permission"));
        when(request.getAttribute("currentUser")).thenReturn(user);

        when(request.getRequestURI()).thenReturn("/some/path");
        when(request.getMethod()).thenReturn("POST");
        when(permissionManager.findRequiredPermission("/some/path", "POST")).thenReturn("required.permission");

        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        assertTrue(decision.isGranted());
    }

    @Test
    void check_userLacksPermission_returnsFalse() {
        Employee user = mock(Employee.class);
        when(user.getRolePermissions()).thenReturn(List.of("other.permission"));
        when(request.getAttribute("currentUser")).thenReturn(user);

        when(request.getRequestURI()).thenReturn("/some/path");
        when(request.getMethod()).thenReturn("DELETE");
        when(permissionManager.findRequiredPermission("/some/path", "DELETE")).thenReturn("required.permission");

        AuthorizationDecision decision = authorizationManager.check(authenticationSupplier, context);

        assertFalse(decision.isGranted());
    }
}
