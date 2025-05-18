package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Entity.*;
import com.phatbee.cosmeticshopbackend.Repository.*;
import com.phatbee.cosmeticshopbackend.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderLineRepository orderLineRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ShippingAddressRepository shippingAddressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Order order;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setUserId(1L);

        order = new Order();
        order.setOrderId(1);
        order.setUser(user);
        order.setTotal(100.0);
        order.setOrderStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryDate(LocalDateTime.now().plusDays(3));
        order.setOrderLines(new HashSet<>()); // Khởi tạo orderLines

        product = new Product();
        product.setProductId(1L);
        product.setProductName("Lipstick");

        cart = new Cart();
        cart.setCustomer(user);
        cart.setCartItems(new HashSet<>());
    }

    @Test
    void createOrder_withValidUser_createsOrder() {
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setUserId(1L);
        orderRequestDTO.setTotal(100.0);
        orderRequestDTO.setOrderStatus("PENDING");
        orderRequestDTO.setDeliveryDate(LocalDateTime.now().plusDays(3));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createOrder(orderRequestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getUser().getUserId());
        assertEquals(100.0, result.getTotal());
        assertEquals("PENDING", result.getOrderStatus());
        verify(userRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_withNonExistentUser_throwsException() {
        OrderRequestDTO orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.createOrder(orderRequestDTO),
                "User not found with ID: 1");
        verify(userRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderLines_withValidOrderAndProducts_createsOrderLines() {
        OrderLineRequestDTO orderLineRequest = new OrderLineRequestDTO();
        orderLineRequest.setProductId(1L);
        orderLineRequest.setQuantity(2L);
        orderLineRequest.setProductSnapshot(new HashMap<>());
        List<OrderLineRequestDTO> orderLineRequests = Arrays.asList(orderLineRequest);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderLineRepository.save(any(OrderLine.class))).thenReturn(new OrderLine());

        orderService.createOrderLines(orderLineRequests, 1);

        verify(orderRepository, times(1)).findById(1);
        verify(productRepository, times(1)).findById(1L);
        verify(orderLineRepository, times(1)).save(any(OrderLine.class));
    }

    @Test
    void createOrderLines_withNonExistentOrder_throwsException() {
        OrderLineRequestDTO orderLineRequest = new OrderLineRequestDTO();
        List<OrderLineRequestDTO> orderLineRequests = Arrays.asList(orderLineRequest);

        when(orderRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.createOrderLines(orderLineRequests, 1),
                "Order not found with ID: 1");
        verify(orderRepository, times(1)).findById(1);
        verify(productRepository, never()).findById(anyLong());
        verify(orderLineRepository, never()).save(any(OrderLine.class));
    }

    @Test
    void createOrderLines_withNonExistentProduct_throwsException() {
        OrderLineRequestDTO orderLineRequest = new OrderLineRequestDTO();
        orderLineRequest.setProductId(1L);
        List<OrderLineRequestDTO> orderLineRequests = Arrays.asList(orderLineRequest);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.createOrderLines(orderLineRequests, 1),
                "Product not found with ID: 1");
        verify(orderRepository, times(1)).findById(1);
        verify(productRepository, times(1)).findById(1L);
        verify(orderLineRepository, never()).save(any(OrderLine.class));
    }

    @Test
    void createPayment_withValidOrder_createsPayment() {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO();
        paymentRequestDTO.setPaymentMethod("CREDIT_CARD");
        paymentRequestDTO.setPaymentStatus("PAID");
        paymentRequestDTO.setTotal(100.0);
        paymentRequestDTO.setPaymentDate(LocalDateTime.now());

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.createPayment(paymentRequestDTO, 1);

        verify(orderRepository, times(1)).findById(1);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createPayment_withNonExistentOrder_throwsException() {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO();

        when(orderRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.createPayment(paymentRequestDTO, 1),
                "Order not found with ID: 1");
        verify(orderRepository, times(1)).findById(1);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createShippingAddress_withValidOrder_createsShippingAddress() {
        ShippingAddressRequestDTO shippingAddressRequestDTO = new ShippingAddressRequestDTO();
        shippingAddressRequestDTO.setReceiverName("John Doe");
        shippingAddressRequestDTO.setReceiverPhone("1234567890");
        shippingAddressRequestDTO.setAddress("123 Main St");
        shippingAddressRequestDTO.setProvince("Province");
        shippingAddressRequestDTO.setDistrict("District");
        shippingAddressRequestDTO.setWard("Ward");

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(shippingAddressRepository.save(any(ShippingAddress.class))).thenReturn(new ShippingAddress());
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.createShippingAddress(shippingAddressRequestDTO, 1);

        verify(orderRepository, times(1)).findById(1);
        verify(shippingAddressRepository, times(1)).save(any(ShippingAddress.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createShippingAddress_withNonExistentOrder_throwsException() {
        ShippingAddressRequestDTO shippingAddressRequestDTO = new ShippingAddressRequestDTO();

        when(orderRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.createShippingAddress(shippingAddressRequestDTO, 1),
                "Order not found with ID: 1");
        verify(orderRepository, times(1)).findById(1);
        verify(shippingAddressRepository, never()).save(any(ShippingAddress.class));
    }

    @Test
    void clearCart_withExistingCart_clearsCart() {
        when(cartRepository.findByCustomer_UserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        orderService.clearCart(1L);

        assertTrue(cart.getCartItems().isEmpty());
        verify(cartRepository, times(1)).findByCustomer_UserId(1L);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void clearCart_withNonExistentCart_doesNothing() {
        when(cartRepository.findByCustomer_UserId(1L)).thenReturn(Optional.empty());

        orderService.clearCart(1L);

        verify(cartRepository, times(1)).findByCustomer_UserId(1L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getLastOrder_withOrders_returnsLastOrder() {
        List<Order> orders = Arrays.asList(new Order(), order);
        when(orderRepository.findAll()).thenReturn(orders);

        Order result = orderService.getLastOrder();

        assertNotNull(result);
        assertEquals(order, result);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getLastOrder_withNoOrders_returnsNull() {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        Order result = orderService.getLastOrder();

        assertNull(result);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getOrdersByUserId_withOrders_returnsCategorizedOrders() {
        Order pendingOrder = new Order();
        pendingOrder.setOrderId(1);
        pendingOrder.setOrderStatus("PENDING");
        pendingOrder.setOrderLines(new HashSet<>());

        Order completedOrder = new Order();
        completedOrder.setOrderId(2);
        completedOrder.setOrderStatus("DELIVERED");
        completedOrder.setOrderLines(new HashSet<>());

        Order cancelledOrder = new Order();
        cancelledOrder.setOrderId(3);
        cancelledOrder.setOrderStatus("CANCELLED");
        cancelledOrder.setOrderLines(new HashSet<>());

        List<Order> orders = Arrays.asList(pendingOrder, completedOrder, cancelledOrder);
        when(orderRepository.findByUserUserId(1L)).thenReturn(orders);

        Map<String, List<OrderDTO>> result = orderService.getOrdersByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.get("active").size());
        assertEquals("PENDING", result.get("active").get(0).getOrderStatus());
        assertEquals(1, result.get("completed").size());
        assertEquals("DELIVERED", result.get("completed").get(0).getOrderStatus());
        assertEquals(1, result.get("cancelled").size());
        assertEquals("CANCELLED", result.get("cancelled").get(0).getOrderStatus());
        verify(orderRepository, times(1)).findByUserUserId(1L);
    }

    @Test
    void getOrdersByUserId_withNoOrders_returnsEmptyMap() {
        when(orderRepository.findByUserUserId(1L)).thenReturn(Collections.emptyList());

        Map<String, List<OrderDTO>> result = orderService.getOrdersByUserId(1L);

        assertNotNull(result);
        assertTrue(result.get("active").isEmpty());
        assertTrue(result.get("completed").isEmpty());
        assertTrue(result.get("cancelled").isEmpty());
        verify(orderRepository, times(1)).findByUserUserId(1L);
    }

    @Test
    void generateTransactionId_generatesValidId() {
        String transactionId = orderService.generateTransactionId();

        assertNotNull(transactionId);
        assertTrue(transactionId.startsWith("TXN"));
        assertTrue(transactionId.length() > 10);
    }

    @Test
    void updateOrderPaymentStatus_withValidOrder_updatesStatus() {
        Payment payment = new Payment();
        payment.setPaymentStatus("PENDING");
        payment.setTotal(100.0);
        order.setPayment(payment);

        when(orderRepository.findByTransactionId("TXN123")).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.updateOrderPaymentStatus("TXN123", "PAID", 150.0);

        assertEquals("PAID", order.getPayment().getPaymentStatus());
        assertEquals(150.0, order.getPayment().getTotal());
        verify(orderRepository, times(1)).findByTransactionId("TXN123");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrderPaymentStatus_withNonExistentOrder_doesNothing() {
        when(orderRepository.findByTransactionId("TXN123")).thenReturn(null);

        orderService.updateOrderPaymentStatus("TXN123", "PAID", 150.0);

        verify(orderRepository, times(1)).findByTransactionId("TXN123");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_withPendingOrder_cancelsOrder() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.cancelOrder(1);

        assertEquals("CANCELLED", order.getOrderStatus());
        verify(orderRepository, times(1)).findById(1);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void cancelOrder_withNonExistentOrder_throwsException() {
        when(orderRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(1),
                "Order not found with ID: 1");
        verify(orderRepository, times(1)).findById(1);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_withNonPendingOrder_throwsException() {
        order.setOrderStatus("DELIVERED");
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(1),
                "Order is not PENDING");
        verify(orderRepository, times(1)).findById(1);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void hashAllFields_withValidFields_generatesHash() {
        Map<String, String> fields = new HashMap<>();
        fields.put("key1", "value1");
        fields.put("key2", "value2");
        String secretKey = "secret";

        String hash = orderService.hashAllFields(fields, secretKey);

        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertEquals(128, hash.length());
    }

    @Test
    void hashAllFields_withEmptyFields_returnsEmptyHash() {
        Map<String, String> fields = new HashMap<>();
        String secretKey = "secret";

        String hash = orderService.hashAllFields(fields, secretKey);

        assertNotNull(hash);
        assertEquals(128, hash.length());
    }

    @Test
    void toQueryString_withValidParams_generatesQueryString() {
        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");

        String queryString = orderService.toQueryString(params);

        assertEquals("key1=value1&key2=value2", queryString);
    }

    @Test
    void toQueryString_withEmptyParams_returnsEmptyString() {
        Map<String, String> params = new HashMap<>();

        String queryString = orderService.toQueryString(params);

        assertEquals("", queryString);
    }
}