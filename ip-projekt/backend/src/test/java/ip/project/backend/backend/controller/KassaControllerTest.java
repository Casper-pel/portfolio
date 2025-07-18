package ip.project.backend.backend.controller;

import ip.project.backend.backend.modeldto.OrderDto;
import ip.project.backend.backend.service.OrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KassaControllerTest {

    @InjectMocks
    private KasseController kasseController;

    @Mock
    private OrderService orderService;

    private OrderDto order;

    @BeforeEach
    void setUp() {
        order = new OrderDto();
        order.setProductNames(List.of("Product1", "Product2", "Product3"));
        order.setTotalPrice(BigDecimal.valueOf(100));
        order.setDate(new Date());
        order.setEmployeeId(1);
    }

    @Test
    void testCheckout_Success() {
        // Arrange
        when(orderService.orderExists(any(String.class))).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Object>> response = kasseController.checkout(order);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));

        // Verify service calls
        verify(orderService, times(1)).orderExists(any(String.class));
        verify(orderService, times(1)).createOrder(any(String.class),
                eq(order.getProductNames()),
                eq(order.getTotalPrice()),
                eq(order.getDate()),
                eq(order.getEmployeeId()));
    }

    @Test
    void testCheckout_OrderAlreadyExists() {
        // Arrange
        when(orderService.orderExists(any(String.class))).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Object>> response = kasseController.checkout(order);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("An order with this ID already exists", response.getBody().get("error"));

        // Verify service calls - createOrder should not be called when order exists
        verify(orderService, times(1)).orderExists(any(String.class));
        verify(orderService, never()).createOrder(any(), any(), any(), any(), any());
    }

    @Test
    void testCheckout_ServiceThrowsException() {
        // Arrange
        when(orderService.orderExists(any(String.class))).thenReturn(false);
        doThrow(new RuntimeException("Database error")).when(orderService)
                .createOrder(any(String.class), any(), any(BigDecimal.class), any(Date.class), any(Integer.class));

        // Act
        ResponseEntity<Map<String, Object>> response = kasseController.checkout(order);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("error").toString().contains("Server error occurred"));
        assertTrue(response.getBody().get("error").toString().contains("Database error"));

        // Verify service calls
        verify(orderService, times(1)).orderExists(any(String.class));
        verify(orderService, times(1)).createOrder(any(String.class),
                eq(order.getProductNames()),
                eq(order.getTotalPrice()),
                eq(order.getDate()),
                eq(order.getEmployeeId()));
    }

    @Test
    void testCheckout_OrderExistsCheckThrowsException() {
        // Arrange
        when(orderService.orderExists(any(String.class))).thenThrow(new RuntimeException("Service unavailable"));

        // Act
        ResponseEntity<Map<String, Object>> response = kasseController.checkout(order);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("error").toString().contains("Server error occurred"));
        assertTrue(response.getBody().get("error").toString().contains("Service unavailable"));

        // Verify service calls
        verify(orderService, times(1)).orderExists(any(String.class));
        verify(orderService, never()).createOrder(any(), any(), any(), any(), any());
    }

    @Test
    void testCheckout_WithEmptyProductList() {
        // Arrange
        order.setProductNames(List.of());
        when(orderService.orderExists(any(String.class))).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Object>> response = kasseController.checkout(order);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));

        // Verify service calls
        verify(orderService, times(1)).orderExists(any(String.class));
        verify(orderService, times(1)).createOrder(any(String.class),
                eq(order.getProductNames()),
                eq(order.getTotalPrice()),
                eq(order.getDate()),
                eq(order.getEmployeeId()));
    }

    @Test
    void testCheckout_WithZeroTotalPrice() {
        // Arrange
        order.setTotalPrice(BigDecimal.ZERO);
        when(orderService.orderExists(any(String.class))).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Object>> response = kasseController.checkout(order);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));

        // Verify service calls
        verify(orderService, times(1)).orderExists(any(String.class));
        verify(orderService, times(1)).createOrder(any(String.class),
                eq(order.getProductNames()),
                eq(order.getTotalPrice()),
                eq(order.getDate()),
                eq(order.getEmployeeId()));
    }
}
