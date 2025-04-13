package com.demo.controller.user;

import com.demo.entity.Order;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.entity.vo.OrderVo;
import com.demo.entity.vo.VenueOrder;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import com.demo.service.VenueService;
import com.demo.exception.LoginException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderVoService orderVoService;

    @Mock
    private VenueService venueService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testOrderManageWithLogin() {
        // Given
        User user = new User();
        when(request.getSession()).thenReturn(mock(HttpSession.class));
        when(request.getSession().getAttribute("user")).thenReturn(user);

        // When
        String viewName = orderController.order_manage(model, request);

        // Then
        assertEquals("order_manage", viewName);
        verify(orderService).findUserOrder(any(), any());
    }

    @Test
    void testOrderManageWithoutLogin() {
        // Given
        when(request.getSession().getAttribute("user")).thenReturn(null);

        // When & Then
        assertThrows(LoginException.class, () -> orderController.order_manage(model, request));
    }

    @Test
    void testGetOrderList() {
        // Given
        List<OrderVo> orderVos = new ArrayList<>();
        User user = new User();
        when(request.getSession().getAttribute("user")).thenReturn(user);
        when(orderVoService.returnVo(any())).thenReturn(orderVos);

        // When
        List<OrderVo> result = orderController.order_list(1, request);

        // Then
        assertEquals(orderVos, result);
        verify(orderService).findUserOrder(any(), any());
    }

    @Test
    void testAddOrder() throws Exception {
        // Given
        User user = new User();
        when(request.getSession().getAttribute("user")).thenReturn(user);

        // When
        orderController.addOrder("Venue1", "2025-01-01", "10:00", 2, request, response);

        // Then
        verify(orderService).submit(any(), any(), any(), any());
        verify(response).sendRedirect("order_manage");
    }

    @Test
    void testFinishOrder() {
        // When
        orderController.finishOrder(1);

        // Then
        verify(orderService).finishOrder(1);
    }

    @Test
    void testModifyOrder() {
        // Given
        Order order = new Order();
        Venue venue = new Venue();
        when(orderService.findById(anyInt())).thenReturn(order);
        when(venueService.findByVenueID(anyInt())).thenReturn(venue);

        // When
        String viewName = orderController.editOrder(model, 1);

        // Then
        assertEquals("order_edit", viewName);
        verify(model).addAttribute("venue", venue);
        verify(model).addAttribute("order", order);
    }

    @Test
    void testDelOrder() {
        // When
        boolean result = orderController.delOrder(1);

        // Then
        assertTrue(result);
        verify(orderService).delOrder(1);
    }

    @Test
    void testGetVenueOrder() {
        // Given
        VenueOrder venueOrder = new VenueOrder();
        when(orderService.findDateOrder(anyInt(), any(), any())).thenReturn(new ArrayList<>());
        when(venueService.findByVenueName(any())).thenReturn(new Venue());

        // When
        VenueOrder result = orderController.getOrder("Venue1", "2025-01-01");

        // Then
        assertNotNull(result);
        verify(orderService).findDateOrder(anyInt(), any(), any());
    }
}
