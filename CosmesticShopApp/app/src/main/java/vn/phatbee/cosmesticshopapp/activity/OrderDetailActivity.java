package vn.phatbee.cosmesticshopapp.activity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.adapter.OrderLineAdapter;
import vn.phatbee.cosmesticshopapp.manager.UserSessionManager;
import vn.phatbee.cosmesticshopapp.model.Cart;
import vn.phatbee.cosmesticshopapp.model.CartItemRequest;
import vn.phatbee.cosmesticshopapp.model.Order;
import vn.phatbee.cosmesticshopapp.model.OrderLine;
import vn.phatbee.cosmesticshopapp.model.Product;
import vn.phatbee.cosmesticshopapp.model.ShippingAddress;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderDate, tvStatus, tvReceiverName, tvReceiverPhone, tvAddress, tvPaymentMethod, tvTotal, tvPaymentTime;
    private ImageView ivBack;
    private RecyclerView rvOrderItems;
    private Button btnCancelOrder, btnReviewProduct, btnBuyAgain;
    private Order order;
    private OrderLineAdapter orderLineAdapter;
    private ApiService apiService;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Khởi tạo các thành phần giao diện
//        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderTime);
//        tvStatus = findViewById(R.id.tvStatus);
//        tvReceiverName = findViewById(R.id.tvReceiverName);
//        tvReceiverPhone = findViewById(R.id.tvReceiverPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvTotal = findViewById(R.id.tvTotalOrder);
        ivBack = findViewById(R.id.ivBackOrderDetail);
        rvOrderItems = findViewById(R.id.rvProductOrderItem);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        btnReviewProduct = findViewById(R.id.btnViewRate);
        tvPaymentTime = findViewById(R.id.tvPaymentTime);
        btnBuyAgain = findViewById(R.id.btnMuaLai);
//        tvStatus = findViewById(R.id.tvStatus);


        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = new UserSessionManager(this);

        // Nhận dữ liệu từ Intent
        order = (Order) getIntent().getSerializableExtra("order");
        if (order == null) {
            Toast.makeText(this, "Không thể tải chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Thiết lập RecyclerView cho danh sách sản phẩm
        List<OrderLine> orderLines = new ArrayList<>(order.getOrderLines());
        orderLineAdapter = new OrderLineAdapter(this, orderLines);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(orderLineAdapter);

        // Hiển thị thông tin đơn hàng
//        tvOrderId.setText("Mã đơn hàng: #" + order.getOrderId());
        if (order.getOrderDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            tvOrderDate.setText(order.getOrderDate().format(formatter));
        } else {
            tvOrderDate.setText("N/A");
        }

        if (order.getPayment().getPaymentDate() != null) {
            String paymentDateStr = order.getPayment().getPaymentDate(); // nếu là String
            DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(paymentDateStr, parser);

// Sau đó format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String formattedDate = dateTime.format(formatter);
            tvPaymentTime.setText(formattedDate);

        }

        else {
            tvPaymentTime.setText("N/A");
        }

//        tvStatus.setText("Trạng thái: " + getStatusText(order.getOrderStatus()));
        tvTotal.setText("Tổng: " + String.format("%,.0f VND", order.getTotal()));

        // Hiển thị thông tin thanh toán
        if (order.getPayment() != null) {
            tvPaymentMethod.setText("Phương thức: " + order.getPayment().getPaymentMethod());
        } else {
            tvPaymentMethod.setText("Phương thức: N/A");
        }

        // Hiển thị địa chỉ giao hàng
        ShippingAddress shippingAddress = order.getShippingAddress();
        if (shippingAddress != null) {
//            tvReceiverName.setText("Người nhận: " + shippingAddress.getReceiverName());
//            tvReceiverPhone.setText("Số điện thoại: " + shippingAddress.getReceiverPhone());
            tvAddress.setText("Địa chỉ: " + shippingAddress.getAddress() + ", " +
                    shippingAddress.getWard() + ", " +
                    shippingAddress.getDistrict() + ", " +
                    shippingAddress.getProvince());
        } else {
//            tvReceiverName.setText("Người nhận: N/A");
//            tvReceiverPhone.setText("Số điện thoại: N/A");
            tvAddress.setText("Địa chỉ: N/A");
        }

        // Hiển thị nút hành động dựa trên trạng thái
        if ("PENDING".equals(order.getOrderStatus())) {
            btnCancelOrder.setVisibility(View.VISIBLE);
            btnCancelOrder.setOnClickListener(v -> cancelOrder());
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }

        if ("DELIVERED".equals(order.getOrderStatus())) {
            btnReviewProduct.setVisibility(View.VISIBLE);
            btnReviewProduct.setOnClickListener(v -> reviewProduct());
        } else {
            btnReviewProduct.setVisibility(View.GONE);
        }

        // Xử lý nút quay lại
        ivBack.setOnClickListener(v -> finish());

        btnBuyAgain.setOnClickListener(v -> buyAgain());
    }

    private String getStatusText(String status) {
        switch (status) {
            case "PENDING": return "Chờ xác nhận";
            case "PROCESSING": return "Đang xử lý";
            case "SHIPPING": return "Đang giao hàng";
            case "DELIVERED": return "Đã giao hàng";
            case "CANCELLED": return "Đã hủy";
            default: return "Không xác định";
        }
    }

    private void buyAgain() {
        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) {
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        // Prepare list of product IDs and cart item requests
        List<Long> productIds = new ArrayList<>();
        List<CartItemRequest> cartItemRequests = new ArrayList<>();
        for (OrderLine orderLine : order.getOrderLines()) {
            productIds.add(orderLine.getProductId());
            CartItemRequest request = new CartItemRequest();
            request.setUserId(userId);
            request.setProductId(orderLine.getProductId());
            request.setQuantity(orderLine.getQuantity());
            cartItemRequests.add(request);
        }

        Log.d(TAG, "Checking product status for product IDs: " + productIds);

        // Check product status
        Call<List<Product>> call = apiService.getProductsStatus(productIds);
        Log.d(TAG, "API Call URL: " + call.request().url());
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Product status response: " + response.body());
                    List<Product> products = response.body();
                    List<CartItemRequest> validRequests = new ArrayList<>();
                    List<String> unavailableProducts = new ArrayList<>();

                    for (CartItemRequest request : cartItemRequests) {
                        Product product = products.stream()
                                .filter(p -> p.getProductId().equals(request.getProductId()))
                                .findFirst()
                                .orElse(null);
                        if (product != null && product.isActive()) {
                            validRequests.add(request);
                        } else {
                            OrderLine orderLine = order.getOrderLines().stream()
                                    .filter(ol -> ol.getProductId().equals(request.getProductId()))
                                    .findFirst()
                                    .orElse(null);
                            if (orderLine != null) {
                                Map<String, Object> snapshot = orderLine.getProductSnapshot();
                                unavailableProducts.add((String) snapshot.get("productName"));
                            }
                        }
                    }

                    if (!unavailableProducts.isEmpty()) {
                        String message = "Các sản phẩm không còn khả dụng: " + String.join(", ", unavailableProducts);
                        Log.w(TAG, message);
                        Toast.makeText(OrderDetailActivity.this, message, Toast.LENGTH_LONG).show();
                    }

                    if (!validRequests.isEmpty()) {
                        addToCart(validRequests);
                    } else {
                        Log.w(TAG, "No valid products to add to cart");
                        Toast.makeText(OrderDetailActivity.this,
                                "Không có sản phẩm nào có thể thêm vào giỏ hàng",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = "Lỗi khi kiểm tra sản phẩm. Mã lỗi: " + response.code() + ", Thông điệp: " + response.message();
                    Log.e(TAG, errorMessage);
                    Toast.makeText(OrderDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                String errorMessage = "Lỗi mạng khi kiểm tra sản phẩm: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                Toast.makeText(OrderDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void addToCart(List<CartItemRequest> requests) {
        Long userId = sessionManager.getUserDetails().getUserId();
        for (CartItemRequest request : requests) {
            Log.d(TAG, "Adding to cart: " + request.getProductId() + ", Quantity: " + request.getQuantity());
            Call<Cart> call = apiService.addToCart(request);
            call.enqueue(new Callback<Cart>() {
                @Override
                public void onResponse(Call<Cart> call, Response<Cart> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Successfully added product " + request.getProductId() + " to cart");
                    } else {
                        String errorMessage = "Lỗi khi thêm sản phẩm " + request.getProductId() + ": " + response.message();
                        Log.e(TAG, errorMessage);
                        Toast.makeText(OrderDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Cart> call, Throwable t) {
                    String errorMessage = "Lỗi mạng khi thêm sản phẩm: " + t.getMessage();
                    Log.e(TAG, errorMessage, t);
                    Toast.makeText(OrderDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Navigate to CartActivity
        Log.d(TAG, "Navigating to CartActivity");
        Intent intent = new Intent(OrderDetailActivity.this, CartActivity.class);
        startActivity(intent);
        Toast.makeText(OrderDetailActivity.this, "Đã thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    private void cancelOrder() {
        Call<Void> call = apiService.cancelOrder(order.getOrderId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OrderDetailActivity.this, "Đơn hàng đã được hủy", Toast.LENGTH_SHORT).show();
                    order.setOrderStatus("CANCELLED");
//                    tvStatus.setText("Trạng thái: Đã hủy");
                    btnCancelOrder.setVisibility(View.GONE);
                } else {
                    Toast.makeText(OrderDetailActivity.this, "Không thể hủy đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reviewProduct() {
        // Giả sử bạn có một ReviewActivity để đánh giá sản phẩm
        Intent intent = new Intent(this, ReviewListActivity.class);
        intent.putExtra("orderLines", new ArrayList<>(order.getOrderLines()));
        intent.putExtra("orderId", order.getOrderId());
        startActivity(intent);
    }
}