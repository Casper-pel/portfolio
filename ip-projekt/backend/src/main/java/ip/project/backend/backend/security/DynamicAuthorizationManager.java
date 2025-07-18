package ip.project.backend.backend.security;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import ip.project.backend.backend.checker.PermissionManager;
import ip.project.backend.backend.model.Employee;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class DynamicAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    Logger logger = LoggerFactory.getLogger(DynamicAuthorizationManager.class);

    private final PermissionManager permissionManager;

    @Autowired
    public DynamicAuthorizationManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();

        String path = request.getRequestURI();
        String method = request.getMethod();

        String requiredPermission = permissionManager.findRequiredPermission(path, method);

        if(requiredPermission == null) {
            return new AuthorizationDecision(false);
        }

        if ("*".equals(requiredPermission)) {
            return new AuthorizationDecision(true);
        }

        Employee employee = (Employee) request.getAttribute("currentUser");

        if (employee == null) {
            logger.info("No current user found");
            return new AuthorizationDecision(false);
        }

        if (employee.getRolePermissions().contains("admin")) {
            return new AuthorizationDecision(true); // admin rights
        }

        boolean allowed = employee.getRolePermissions().contains(requiredPermission);
        return new AuthorizationDecision(allowed);
    }
}
