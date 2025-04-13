package com.demo.service;

import com.demo.entity.Order;
import com.demo.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTests {

    @Mock
    private OrderService orderService;  // Mocking the OrderService interface

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;  // Testing the OrderServiceImpl implementation

    private Order order;

    @BeforeEach
    public void setUp() {
        order = new Order();
        order.setOrderID(1);
        order.setStartTime(LocalDateTime.now());
        order.setHours(2);
        order.setUserID("user123");
    }

    @Test
    public void testFindById_Valid() {
        // Arrange
        when(orderService.findById(1)).thenReturn(order);

        // Act
        Order result = orderService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getOrderID());
        assertEquals("user123", result.getUserID());
    }

    @Test
    public void testFindById_NotFound() {
        // Arrange
        when(orderService.findById(999)).thenReturn(null);

        // Act
        Order result = orderService.findById(999);

        // Assert
        assertNull(result);
    }

    @Test
    public void testFindNoAuditOrder() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Arrays.asList(order), pageable, 1);
        when(orderService.findNoAuditOrder(pageable)).thenReturn(page);

        // Act
        Page<Order> result = orderService.findNoAuditOrder(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("user123", result.getContent().get(0).getUserID());
    }

    @Test
    public void testFindAuditOrder() {
        // Arrange
        List<Order> orders = Arrays.asList(order);
        when(orderService.findAuditOrder()).thenReturn(orders);

        // Act
        List<Order> result = orderService.findAuditOrder();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user123", result.get(0).getUserID());
    }

    @Test
    public void testSubmitOrder() {
        LocalDateTime now = LocalDateTime.now();
        // Act
        orderService.submit("Test Venue", now, 2, "user123");

        // Assert
        verify(orderService, times(1)).submit("Test Venue", now, 2, "user123");
    }

    @Test
    public void testUpdateOrder() {
        LocalDateTime time = LocalDateTime.now();
        // Act
        orderService.updateOrder(1, "New Venue", time, 3, "user123");

        // Assert
        verify(orderService, times(1)).updateOrder(1, "New Venue", time, 3, "user123");
    }

    @Test
    public void testDelOrder() {
        // Arrange
        doNothing().when(orderService).delOrder(1);

        // Act
        orderService.delOrder(1);

        // Assert
        verify(orderService, times(1)).delOrder(1);
    }

    @Test
    public void testConfirmOrder() {
        // Arrange
        doNothing().when(orderService).confirmOrder(1);

        // Act
        orderService.confirmOrder(1);

        // Assert
        verify(orderService, times(1)).confirmOrder(1);
    }

    @Test
    public void testFinishOrder() {
        // Arrange
        doNothing().when(orderService).finishOrder(1);

        // Act
        orderService.finishOrder(1);

        // Assert
        verify(orderService, times(1)).finishOrder(1);
    }

    @Test
    public void testRejectOrder() {
        // Arrange
        doNothing().when(orderService).rejectOrder(1);

        // Act
        orderService.rejectOrder(1);

        // Assert
        verify(orderService, times(1)).rejectOrder(1);
    }
}
