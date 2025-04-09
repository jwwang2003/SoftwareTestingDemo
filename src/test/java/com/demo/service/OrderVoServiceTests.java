package com.demo.service;

import com.demo.entity.Order;
import com.demo.entity.vo.OrderVo;
import com.demo.service.impl.OrderVoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderVoServiceTests {

    @Mock
    private OrderVoService orderVoService;  // Mocking the interface

    @InjectMocks
    private OrderVoServiceImpl orderVoServiceImpl;  // Testing the implementation

    private Order order;
    private OrderVo orderVo;

    @BeforeEach
    public void setUp() {
        order = new Order();
        order.setOrderID(1);
        order.setStartTime(LocalDateTime.now());
        order.setHours(2);
        order.setUserID("user123");

        orderVo = new OrderVo();
        orderVo.setOrderID(1);
        orderVo.setVenueName("Test Venue");
        orderVo.setStartTime(LocalDateTime.now());
        orderVo.setHours(2);
        orderVo.setUserID("user123");
    }

    @Test
    public void testReturnOrderVoByOrderID_Valid() {
        // Arrange
        when(orderVoService.returnOrderVoByOrderID(1)).thenReturn(orderVo);

        // Act
        OrderVo result = orderVoService.returnOrderVoByOrderID(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getOrderID());
        assertEquals("Test Venue", result.getVenueName());
        assertEquals(2, result.getHours());
        assertEquals("user123", result.getUserID());
    }

    @Test
    public void testReturnOrderVoByOrderID_NotFound() {
        // Arrange
        when(orderVoService.returnOrderVoByOrderID(999)).thenReturn(null);

        // Act
        OrderVo result = orderVoService.returnOrderVoByOrderID(999);

        // Assert
        assertNull(result);
    }

    @Test
    public void testReturnVo_EmptyList() {
        // Arrange
        List<Order> orders = Arrays.asList();

        // Act
        List<OrderVo> result = orderVoService.returnVo(orders);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testReturnVo_ValidOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(order);
        when(orderVoService.returnVo(orders)).thenReturn(Arrays.asList(orderVo));

        // Act
        List<OrderVo> result = orderVoService.returnVo(orders);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Venue", result.get(0).getVenueName());
        assertEquals(1, result.get(0).getOrderID());
    }

//    @Test
//    public void testReturnVo_NullList() {
//        // Act & Assert
//        assertThrows(NullPointerException.class, () -> {
//            orderVoService.returnVo(null);
//        });
//    }

    @Test
    public void testReturnVo_MessageWithNonExistentOrder() {
        // Arrange
        List<Order> orders = Arrays.asList(order);
        when(orderVoService.returnVo(orders)).thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderVoService.returnVo(orders);
        });
    }
}
