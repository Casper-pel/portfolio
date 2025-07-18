package ip.project.backend.backend.service;

import ip.project.backend.backend.model.Order;
import ip.project.backend.backend.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Service for managing order-related operations.
 * This service handles operations related to orders, including checking existence,
 * creating new orders, and retrieving orders within specific time periods.
 */
@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Checks if an order with the given ID exists in the database.
     *
     * @param orderId The order ID to check
     * @return true if the order exists, false otherwise
     */
    public boolean orderExists(final String orderId){
        logger.debug("Checking if order with ID {} exists", orderId);
        boolean exists = orderRepository.findOrderByOrderId(orderId).isPresent();
        if (exists) {
            logger.debug("Order with ID {} exists", orderId);
        } else {
            logger.debug("Order with ID {} does not exist", orderId);
        }
        return exists;
    }

    /**
     * Creates a new order with the given information.
     *
     * @param orderId The unique identifier for the order
     * @param products List of product names included in the order
     * @param totalPrice The total price of the order
     * @param date The date when the order was created
     * @param employeeId The ID of the employee who created the order
     * @return The created order entity
     */
    public Order createOrder(final String orderId, final List<String> products, final BigDecimal totalPrice, final Date date, final Integer employeeId){
        logger.info("Creating new order with ID: {}, products: {}, total price: {}, employee ID: {}", 
                orderId, products.size(), totalPrice, employeeId);

        try {
            Order order = new Order(orderId, products, totalPrice, date, employeeId);
            Order savedOrder = orderRepository.save(order);
            logger.info("Order with ID: {} created successfully", orderId);
            return savedOrder;
        } catch (Exception e) {
            logger.error("Error creating order with ID {}: {}", orderId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves all orders created within a specified time period.
     *
     * @param start The start date of the period (inclusive)
     * @param end The end date of the period (inclusive)
     * @return A list of orders within the specified period
     */
    public List<Order> getOrdersInPeriod(final Date start, final Date end){
        logger.info("Retrieving orders between {} and {}", start, end);

        try {
            List<Order> orders = orderRepository.getByDateBetween(start, end);
            logger.info("Found {} orders between {} and {}", orders.size(), start, end);
            return orders;
        } catch (Exception e) {
            logger.error("Error retrieving orders between {} and {}: {}", start, end, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves all orders created by a specific employee within a specified time period.
     *
     * @param start The start date of the period (inclusive)
     * @param end The end date of the period (inclusive)
     * @param employeeId The ID of the employee who created the orders
     * @return A list of orders created by the specified employee within the specified period
     */
    public List<Order> getOrdersInPeriodByEmployee(final Date start, final Date end, final Integer employeeId) {
        logger.info("Retrieving orders between {} and {} for employee ID: {}", start, end, employeeId);

        try {
            List<Order> orders = orderRepository.getByDateBetweenAndEmployeeId(start, end, employeeId);
            logger.info("Found {} orders between {} and {} for employee ID: {}", 
                    orders.size(), start, end, employeeId);
            return orders;
        } catch (Exception e) {
            logger.error("Error retrieving orders between {} and {} for employee ID {}: {}",
                    start, end, employeeId, e.getMessage(), e);
            throw e;
        }
    }
}
