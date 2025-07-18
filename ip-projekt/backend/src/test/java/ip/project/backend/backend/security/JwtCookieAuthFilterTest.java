package ip.project.backend.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.repository.EmployeeRepository;
import ip.project.backend.backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtCookieAuthFilterTest {

    @Mock
    JwtService jwtService;

    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    JwtCookieAuthFilter filter;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_noToken_callsFilterChainWithoutAuth() throws ServletException, IOException {
        when(request.getCookies()).thenReturn(new Cookie[0]);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_invalidToken_callsFilterChainWithoutAuth() throws ServletException, IOException {
        Cookie tokenCookie = new Cookie("token", "invalid-token");
        when(request.getCookies()).thenReturn(new Cookie[]{tokenCookie});
        when(jwtService.validateToken("invalid-token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {
        Cookie tokenCookie = new Cookie("token", "valid-token");
        when(request.getCookies()).thenReturn(new Cookie[]{tokenCookie});
        when(jwtService.validateToken("valid-token")).thenReturn(true);
        when(jwtService.extractEmployeeId("valid-token")).thenReturn(42);

        Employee employee = mock(Employee.class);
        when(employeeRepository.findEmployeeByEmployeeId(42)).thenReturn(Optional.of(employee));

        when(employee.getRolePermissions()).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(employee, auth.getPrincipal());
        assertEquals(2, auth.getAuthorities().size());
    }
}
