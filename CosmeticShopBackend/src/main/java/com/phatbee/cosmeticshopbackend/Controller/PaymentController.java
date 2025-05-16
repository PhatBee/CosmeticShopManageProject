package com.phatbee.cosmeticshopbackend.Controller;

import com.phatbee.cosmeticshopbackend.Service.Impl.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

   @Autowired
   private OrderServiceImpl orderService;

    @Value("${vnpay.hash_secret}")
    private String hashSecret;

    @PostMapping("/vnpay-ipn")
    public ResponseEntity<String> handleVNPayIPN(@RequestBody Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");

        String signData = orderService.toQueryString(params);
        String secureHash = orderService.hashHMAC512(signData, hashSecret);

        if (secureHash.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_Amount = params.get("vnp_Amount");
            String vnp_OrderInfo = params.get("vnp_OrderInfo");

            if ("00".equals(vnp_ResponseCode)) {
                // Thanh toán thành công
                orderService.updateOrderPaymentStatus(vnp_TxnRef, "COMPLETED", Double.parseDouble(vnp_Amount) / 100);
                return ResponseEntity.ok("OK");
            } else {
                // Thanh toán thất bại
                orderService.updateOrderPaymentStatus(vnp_TxnRef, "FAILED", Double.parseDouble(vnp_Amount) / 100);
                return ResponseEntity.ok("FAILED");
            }
        } else {
            return ResponseEntity.badRequest().body("INVALID_HASH");
        }
    }
}