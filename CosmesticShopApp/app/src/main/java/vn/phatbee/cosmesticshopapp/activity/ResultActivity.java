package vn.phatbee.cosmesticshopapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import vn.phatbee.cosmesticshopapp.R;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        String action = intent.getStringExtra("action");
        Uri data = intent.getData(); // Lấy dữ liệu từ deep link nếu có

        if (action != null) {
            Log.wtf("ResultActivity", "action: " + action);
            switch (action) {
                case "SuccessBackAction":
                    Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show();
                    // Chuyển đến màn hình xác nhận đơn hàng
                    Intent successIntent = new Intent(this, MainActivity.class); // Thay bằng activity của bạn
                    startActivity(successIntent);
                    finish();
                    break;
                case "FaildBackAction":
                    Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case "WebBackAction":
                    Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case "AppBackAction":
                case "CallMobileBankingApp":
                    finish();
                    break;
            }
        } else if (data != null) {
            // Xử lý nếu VNPay gửi dữ liệu qua deep link
            String query = data.getQuery();
            if (query != null) {
                Log.d("ResultActivity", "Deep link data: " + query);
                // Phân tích query để lấy vnp_ResponseCode, vnp_TxnRef, vnp_Amount, vnp_OrderInfo, vnp_SecureHash
                // Gọi API backend để xác nhận kết quả thanh toán
                handleVNPayCallback(query);
            }
            finish();
        } else {
            finish();
        }
    }

    private void handleVNPayCallback(String query) {
        // Giả sử bạn phân tích query và gọi API backend để xác nhận
        // Ví dụ: gửi query đến endpoint IPN của backend
        Map<String, String> params = new HashMap<>();
        for (String param : query.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        // Gọi API backend để xử lý (tương tự handleVNPayIPN)
        // Ví dụ: apiService.handleVNPayIPN(params);
    }
}