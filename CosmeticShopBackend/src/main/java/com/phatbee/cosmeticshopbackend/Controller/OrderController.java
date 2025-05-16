package com.phatbee.cosmeticshopbackend.Controller;

import com.phatbee.cosmeticshopbackend.Entity.Order;
import com.phatbee.cosmeticshopbackend.Service.OrderService;
import com.phatbee.cosmeticshopbackend.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Value("${vnpay.tmn_code}")
    private String tmnCode;

    @Value("${vnpay.hash_secret}")
    private String hashSecret; // Secret key từ VNPay

    @Value("${vnpay.url}")
    private String vnpayUrl;

    @Autowired
    private OrderService orderService;

    @PostMapping("/orders/create")
    public ResponseEntity<Void> createOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        Order order = orderService.createOrder(orderRequestDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/order-lines/create")
    public ResponseEntity<Void> createOrderLines(@RequestBody List<OrderLineRequestDTO> orderLineRequests) {
        // In a real app, you'd pass the orderId from the previous response.
        // For simplicity, assume the last order is the one we just created.
        Order lastOrder = orderService.getLastOrder();
        int orderId = lastOrder.getOrderId();
        orderService.createOrderLines(orderLineRequests, orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/payments/create")
    public ResponseEntity<Void> createPayment(@RequestBody PaymentRequestDTO paymentRequestDTO) {
        Order lastOrder = orderService.getLastOrder();
        int orderId = lastOrder.getOrderId();
        orderService.createPayment(paymentRequestDTO, orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/shipping-addresses/create")
    public ResponseEntity<Void> createShippingAddress(@RequestBody ShippingAddressRequestDTO shippingAddressRequestDTO) {
        Order lastOrder = orderService.getLastOrder();
        int orderId = lastOrder.getOrderId();
        orderService.createShippingAddress(shippingAddressRequestDTO, orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/clear/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        orderService.clearCart(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<Map<String, List<OrderDTO>>> getOrdersByUserId(@PathVariable Long userId) {
        Map<String, List<OrderDTO>> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("orders/cancel/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable int orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/create-vnpay-url")
    public ResponseEntity<String> createVNPayPaymentUrl(@RequestParam Long userId, @RequestBody Map<String, String> paymentData) {
        double amount = Double.parseDouble(paymentData.get("amount"));
        String orderInfo = paymentData.getOrDefault("orderInfo", "Thanh toan don hang tu CosmeticShopApp");
        String orderType = paymentData.getOrDefault("orderType", "other");
        String returnUrl = paymentData.get("returnUrl"); // e.g., http://success.sdk.merchantbackapp

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", String.valueOf((int) (amount * 100))); // Số tiền nhân 100 (VND)
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderService.generateTransactionId());
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", "192.168.0.100"); // Thay bằng IP thực tế nếu cần

        // Tạo Secure Hash
        String secureHash = orderService.hashAllFields(vnpParams, hashSecret);
        vnpParams.put("vnp_SecureHash", secureHash);

        // Tạo URL thanh toán
        String vnpayUrlWithParams = vnpayUrl + "?" + orderService.toQueryString(vnpParams);
        return ResponseEntity.ok(vnpayUrlWithParams);
    }

    @GetMapping("/vnpay-redirect")
    public ResponseEntity<Void> vnpayRedirect(@RequestParam Map<String, String> params) {
        // Chuyển hướng đến scheme của ứng dụng
        String scheme = "cosmesticshopapp://result"; // Thay bằng scheme và path phù hợp
        String queryString = orderService.toQueryString(params);
        String redirectUrl = scheme + "?" + queryString;
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }


}