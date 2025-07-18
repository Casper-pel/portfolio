package ip.project.backend.backend.checker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionManagerTest {

    private PermissionManager permissionManager;

    @BeforeEach
    void setUp() {
        permissionManager = new PermissionManager();
    }

    @Test
    void testValidPermissionsAcrossModules() {
        assertEquals("user.read", permissionManager.findRequiredPermission("/api/employee/get/123", "GET"));
        assertEquals("coupons.create", permissionManager.findRequiredPermission("/api/coupon/add", "POST"));
        assertEquals("product.update", permissionManager.findRequiredPermission("/api/products/update", "PUT"));
        assertEquals("role.delete", permissionManager.findRequiredPermission("/api/role/delete/5", "DELETE"));
        assertEquals("kasse", permissionManager.findRequiredPermission("/api/checkout/create-checkout-session", "POST"));
    }

    @Test
    void testInvalidOrUnknownPathsReturnNull() {
        assertNull(permissionManager.findRequiredPermission("/api/unknown/path", "GET"));
        assertNull(permissionManager.findRequiredPermission("/api/coupon", "GET")); // Missing name
        assertNull(permissionManager.findRequiredPermission("/api/user/additional", "POST")); // Not defined
    }
}
