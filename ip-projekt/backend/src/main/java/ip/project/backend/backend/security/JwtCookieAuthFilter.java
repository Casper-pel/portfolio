package ip.project.backend.backend.security;



import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.repository.EmployeeRepository;
import ip.project.backend.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JwtCookieAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtCookieAuthFilter.class);
    private final JwtService jwtService;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public JwtCookieAuthFilter(JwtService jwtService, EmployeeRepository employeeRepository) {
        this.jwtService = jwtService;
        this.employeeRepository = employeeRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Zuerst versuchen Token aus Cookie zu holen
        String token = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(c -> "token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        // Falls kein Cookie-Token, versuche Authorization Header
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token != null) {
            boolean isValid = jwtService.validateToken(token);

            if (isValid) {
                Integer employeeId = jwtService.extractEmployeeId(token);

                Optional<Employee> employeeOpt = employeeRepository.findEmployeeByEmployeeId(employeeId);

                if (employeeOpt.isPresent()) {
                    Employee user = employeeOpt.get();

                    // Convert user permissions to Spring Security authorities
                    List<GrantedAuthority> authorities = user.getRolePermissions().stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // Set authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user, null, authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    request.setAttribute("currentUser", user);
                    logger.info("Successfully set currentUser attribute for employee: {}", user.getEmployeeId());
                } else {
                    logger.warn("Employee not found in database for ID: {}", employeeId);
                }
            } else {
                logger.warn("Invalid JWT token for request: {} {}", request.getMethod(), request.getRequestURI());
            }
        } else {
            logger.info("No token cookie found for request: {} {}", request.getMethod(), request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
