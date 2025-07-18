package ip.project.backend.backend.checker;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class PermissionManager {

    private static class RouteKey {
        private final Pattern pattern;
        private final String method;

        RouteKey(String method, String pathPattern) {
            this.pattern = Pattern.compile(pathPattern);
            this.method = method.toUpperCase();
        }

        boolean matches(String method, String path) {
            return this.method.equalsIgnoreCase(method) && pattern.matcher(path).matches();
        }
    }

    private final Map<RouteKey, String> pathPermissions = new HashMap<>();

    public PermissionManager() {
        // Auth Controller - öffentliche Endpunkte (keine Berechtigung erforderlich)
        add("POST", "^/api/auth/login$", "*");
        add("POST", "^/api/auth/signup$", "*");
        add("POST", "^/api/auth/logout$", "*");
        add("POST", "^/api/auth/cookie-validation$", "*");

        // Employee/User Controller
        add("GET", "^/api/employee/current$", "*"); // Aktueller Benutzer kann immer auf seine eigenen Daten zugreifen
        add("GET", "^/api/employee/get/.*", "user.read");
        add("GET", "^/api/employee/all$", "user.read");
        add("PUT", "^/api/employee/update$", "user.update");
        add("DELETE", "^/api/employee/delete/.*", "user.delete");
        add("PUT", "^/api/employee/update-password$", "*"); // Jeder kann sein eigenes Passwort ändern

        // Product Controller
        add("GET", "^/api/products/all$", "product.read");
        add("GET", "^/api/products/get/.*", "product.read");
        add("GET", "^/api/products/all/active$", "product.read");
        add("POST", "^/api/products/add$", "product.create");
        add("PUT", "^/api/products/update$", "product.update");
        add("DELETE", "^/api/products/delete/.*", "product.delete");
        add("GET", "^/api/products/price-history/.*", "product.read");
        add("GET", "^/api/products/cache/product/ean/.*", "product.read");
        add("GET", "^/api/products/best-selling$", "product.read");

        // Coupon Controller
        add("POST", "^/api/coupon/add$", "coupons.create");
        add("GET", "^/api/coupon/all$", "coupons.read");
        add("GET", "^/api/coupon/[^/]+$", "coupons.read");
        add("DELETE", "^/api/coupon/[^/]+/delete$", "coupons.delete");

        // Role Controller
        add("GET", "^/api/role/get/.*", "role.read");
        add("GET", "^/api/role/all$", "role.read");
        add("POST", "^/api/role/add$", "role.create");
        add("DELETE", "^/api/role/delete/.*", "role.delete");
        add("PUT", "^/api/role/update$", "role.update");

        // Stock Controller
        add("GET", "^/api/stock/.*", "product.read");
        add("GET", "^/api/stock/all$", "product.read");
        add("POST", "^/api/stock/add$", "product.update");
        add("PUT", "^/api/stock/update$", "product.update");
        add("DELETE", "^/api/stock/delete/.*", "product.delete");

        // Urlaubsantrag Controller
        add("GET", "^/api/urlaubsantrag/all$", "urlaub.read");
        add("GET", "^/api/urlaubsantrag/get/.*", "urlaub.read");
        add("GET", "^/api/urlaubsantrag/user$", "*"); // Eigene Urlaubsanträge anzeigen
        add("POST", "^/api/urlaubsantrag/add$", "*"); // Eigenen Urlaubsantrag erstellen
        add("DELETE", "^/api/urlaubsantrag/delete/.*", "*"); // Eigenen Urlaubsantrag löschen
        add("PUT", "^/api/urlaubsantrag/review$", "urlaub.review");
        add("PUT", "^/api/urlaubsantrag/update$", "*"); // Eigenen Urlaubsantrag bearbeiten

        // Order Controller
        add("GET", "^/api/order/between$", "finances");

        // Kasse Controller
        add("POST", "^/api/kassa/checkout$", "kasse");

        // Checkout Controller
        add("POST", "^/api/checkout/create-checkout-session$", "kasse");
    }

    private void add(String method, String pathPattern, String permission) {
        pathPermissions.put(new RouteKey(method, pathPattern), permission);
    }

    public String findRequiredPermission(String path, String method) {
        return pathPermissions.entrySet().stream()
                .filter(entry -> entry.getKey().matches(method, path))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
