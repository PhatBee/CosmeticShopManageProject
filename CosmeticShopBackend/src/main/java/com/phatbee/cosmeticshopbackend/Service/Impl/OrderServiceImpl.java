package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Entity.*;
import com.phatbee.cosmeticshopbackend.Repository.*;
import com.phatbee.cosmeticshopbackend.Service.OrderService;
import com.phatbee.cosmeticshopbackend.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderLineRepository orderLineRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ShippingAddressRepository shippingAddressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Override
    @Transactional
    public Order createOrder(OrderRequestDTO orderRequestDTO) {
        // Validate user
        Optional<User> userOptional = userRepository.findById(orderRequestDTO.getUserId());
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + orderRequestDTO.getUserId());
        }
        User user = userOptional.get();

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setTotal(orderRequestDTO.getTotal());
        order.setOrderStatus(orderRequestDTO.getOrderStatus());
        order.setDeliveryDate(orderRequestDTO.getDeliveryDate());
        order.setOrderDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void createOrderLines(List<OrderLineRequestDTO> orderLineRequests, int orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }
        Order order = orderOptional.get();

        for (OrderLineRequestDTO request : orderLineRequests) {
            Optional<Product> productOptional = productRepository.findById(request.getProductId());
            if (productOptional.isEmpty()) {
                throw new RuntimeException("Product not found with ID: " + request.getProductId());
            }
            Product product = productOptional.get();

            OrderLine orderLine = new OrderLine();
            orderLine.setOrder(order);
            orderLine.setProduct(product);
            orderLine.setQuantity(request.getQuantity());
            orderLine.setProductSnapshot(request.getProductSnapshot());
            orderLineRepository.save(orderLine);
        }
    }

    @Override
    @Transactional
    public void createPayment(PaymentRequestDTO paymentRequestDTO, int orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }
        Order order = orderOptional.get();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentRequestDTO.getPaymentMethod());
        payment.setPaymentStatus(paymentRequestDTO.getPaymentStatus());
        payment.setTotal(paymentRequestDTO.getTotal());
        payment.setPaymentDate(paymentRequestDTO.getPaymentDate());
        paymentRepository.save(payment);

        order.setPayment(payment);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void createShippingAddress(ShippingAddressRequestDTO shippingAddressRequestDTO, int orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }
        Order order = orderOptional.get();

        ShippingAddress shippingAddress = getShippingAddress(shippingAddressRequestDTO, order);
        shippingAddressRepository.save(shippingAddress);

        order.setShippingAddress(shippingAddress);
        orderRepository.save(order);
    }

    private static ShippingAddress getShippingAddress(ShippingAddressRequestDTO shippingAddressRequestDTO, Order order) {
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setOrder(order);
        shippingAddress.setReceiverName(shippingAddressRequestDTO.getReceiverName());
        shippingAddress.setReceiverPhone(shippingAddressRequestDTO.getReceiverPhone());
        shippingAddress.setAddress(shippingAddressRequestDTO.getAddress());
        shippingAddress.setProvince(shippingAddressRequestDTO.getProvince());
        shippingAddress.setDistrict(shippingAddressRequestDTO.getDistrict());
        shippingAddress.setWard(shippingAddressRequestDTO.getWard());
        return shippingAddress;
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Optional<Cart> cartOptional = cartRepository.findByCustomer_UserId(userId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            cart.getCartItems().clear();
            cartRepository.save(cart);
        }
    }

    @Override
    public Order getLastOrder() {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) return null;
        return orders.get(orders.size() - 1);
    }

    @Override
    public Map<String, List<OrderDTO>> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserUserId(userId);
        Map<String, List<OrderDTO>> categorizedOrders = new HashMap<>();

        List<OrderDTO> activeOrders = new ArrayList<>();
        List<OrderDTO> completedOrders = new ArrayList<>();
        List<OrderDTO> cancelledOrders = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO(order);
            String status = order.getOrderStatus();
            if (status == null) continue;

            switch (status) {
                case "PENDING":
                case "PROCESSING":
                case "SHIPPING":
                    activeOrders.add(orderDTO);
                    break;
                case "DELIVERED":
                    completedOrders.add(orderDTO);
                    break;
                case "CANCELLED":
                    cancelledOrders.add(orderDTO);
                    break;
                default:
                    break;
            }
        }

        categorizedOrders.put("active", activeOrders);
        categorizedOrders.put("completed", completedOrders);
        categorizedOrders.put("cancelled", cancelledOrders);

        return categorizedOrders;
    }


    public String generateTransactionId() {
        Random random = new Random();
        return "TXN" + System.currentTimeMillis() + random.nextInt(1000);
    }

    @Override
    public void updateOrderPaymentStatus(String txnRef, String status, Double amount) {
        // Logic cập nhật trạng thái đơn hàng trong database
        // Giả sử bạn có repository để truy vấn và cập nhật
        Order order = orderRepository.findByTransactionId(txnRef); // Cần triển khai repository
        if (order != null) {
            order.getPayment().setPaymentStatus(status);
            order.getPayment().setTotal(amount);
            orderRepository.save(order);
        }
    }

    public String hashAllFields(Map<String, String> fields, String secretKey) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                sb.append(fieldName).append("=").append(fieldValue);
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        return hmacSHA512(secretKey, sb.toString());
    }

    @Override
    public void cancelOrder(int orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }
        if (!"PENDING".equals(order.getOrderStatus())) {
            throw new RuntimeException("Order is not PENDING");
        }

        order.setOrderStatus("CANCELLED");
        orderRepository.save(order);
    }

    public String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error hashing data", e);
        }
    }

    public String hashHMAC512(String data, String secretKey) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hash.append('0');
                hash.append(hex);
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hash error", e);
        }
    }


    public String toQueryString(Map<String, String> params) {
        StringBuilder queryString = new StringBuilder();
        params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> queryString.append(e.getKey()).append("=").append(e.getValue()).append("&"));
        if (queryString.length() > 0) {
            queryString.setLength(queryString.length() - 1);
        }
        return queryString.toString();
    }




}
