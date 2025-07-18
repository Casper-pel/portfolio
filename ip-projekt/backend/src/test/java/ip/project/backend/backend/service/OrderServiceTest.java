package ip.project.backend.backend.service;

import ip.project.backend.backend.model.Order;
import ip.project.backend.backend.repository.OrderRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository; // Mock the OrderRepository

    @InjectMocks
    private OrderService orderService; // Inject the mocked repository into the OrderService

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    public void testOrderExists_ExistingOrder() {
        // Arrange
        String orderId = "12345";
        when(orderRepository.findOrderByOrderId(orderId)).thenReturn(Optional.of(new Order())); // Simulate that the order exists

        // Act
        boolean result = orderService.orderExists(orderId);

        // Assert
        assertTrue(result, "The order should exist.");
    }

    @Test
    public void testOrderExists_NonExistingOrder() {
        // Arrange
        String orderId = "12345";
        when(orderRepository.findOrderByOrderId(orderId)).thenReturn(Optional.empty()); // Simulate that the order does not exist

        // Act
        boolean result = orderService.orderExists(orderId);

        // Assert
        assertFalse(result, "The order should not exist.");
    }

    @Test
    public void testCreateOrder() {
        // Arrange
        String orderId = "12345";
        List<String> products = List.of("1", "2", "3");
        BigDecimal totalPrice = BigDecimal.valueOf(100.50);
        Date date = new Date();
        Integer employeeId = 1;
        Order order = new Order(orderId, products, totalPrice, date, employeeId);

        when(orderRepository.save(any(Order.class))).thenReturn(order); // Simulate saving the order

        // Act
        Order createdOrder = orderService.createOrder(orderId, products, totalPrice, date, employeeId);

        // Assert
        assertNotNull(createdOrder, "The order should not be null");
        assertEquals(orderId, createdOrder.getOrderId(), "The order ID should match");
        verify(orderRepository, times(1)).save(any(Order.class)); // Ensure save method was called once
    }
}
