package vn.phatbee.cosmesticshopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vnpay.authentication.VNP_AuthenticationActivity;
import com.vnpay.authentication.VNP_SdkCompletedCallback;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.adapter.CartItemCheckoutAdapter;
import vn.phatbee.cosmesticshopapp.manager.UserSessionManager;
import vn.phatbee.cosmesticshopapp.model.Address;
import vn.phatbee.cosmesticshopapp.model.Cart;
import vn.phatbee.cosmesticshopapp.model.CartItem;
import vn.phatbee.cosmesticshopapp.model.CartItemLite;
import vn.phatbee.cosmesticshopapp.model.OrderLine;
import vn.phatbee.cosmesticshopapp.model.OrderRequest;
import vn.phatbee.cosmesticshopapp.model.Payment;
import vn.phatbee.cosmesticshopapp.model.Product;
import vn.phatbee.cosmesticshopapp.model.ShippingAddress;
import vn.phatbee.cosmesticshopapp.model.User;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class CheckoutActivity extends AppCompatActivity {

    private ImageView ivBack, ivEditAddressCheckout, ivEditContact;
    private TextView tvAddress, tvPhone, tvEmail, tvTotal;
    private RecyclerView rvCartItems;
    private Button btnProceed, btnEditPayment;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbVNPay;
    private UserSessionManager sessionManager;
    private ApiService apiService;
    private CartItemCheckoutAdapter cartItemAdapter;
    private ActivityResultLauncher<Intent> addressListLauncher;
    private Address defaultAddress;
    private Address selectedAddress;
    private List<CartItem> selectedCartItems;

    private String tmnCode = "KB5K1R3O"; // Thay bằng mã TMN mà VNPay cung cấp
    private String scheme = "cosmesticmobile"; // Thay bằng scheme bạn đã cấu hình
    private ActivityResultLauncher<Intent> vnpayLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Initialize views
        ivBack = findViewById(R.id.ivBack);
        ivEditAddressCheckout = findViewById(R.id.ivEditAddressCheckout);
        ivEditContact = findViewById(R.id.ivEdit2);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvTotal = findViewById(R.id.tvTotal);
        rvCartItems = findViewById(R.id.rvCartItems);
        btnProceed = findViewById(R.id.btnProceed);
        btnEditPayment = findViewById(R.id.btnEditPayment);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCOD = findViewById(R.id.rbCOD);
        rbVNPay = findViewById(R.id.rbVNPay);

        sessionManager = new UserSessionManager(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Get selected cart items from Intent
        Object extra = getIntent().getSerializableExtra("selectedCartItems");
        List<CartItemLite> liteItems = new ArrayList<>();
        if (extra instanceof List<?>) {
            for (Object item : (List<?>) extra) {
                if (item instanceof CartItemLite) {
                    liteItems.add((CartItemLite) item);
                }
            }
        }
        if (liteItems.isEmpty()) {
            Toast.makeText(this, "No items selected for checkout", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize selectedCartItems
        selectedCartItems = new ArrayList<>();

        // Setup RecyclerView
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        cartItemAdapter = new CartItemCheckoutAdapter(this, selectedCartItems);
        rvCartItems.setAdapter(cartItemAdapter);

        // Fetch full CartItem details
        fetchCartItems(liteItems);

        // Register ActivityResultLauncher for address selection
        addressListLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedAddress = (Address) result.getData().getSerializableExtra("selectedAddress");
                        if (selectedAddress != null) {
                            String addressText = String.format("%s, %s, %s, %s",
                                    selectedAddress.getAddress(),
                                    selectedAddress.getWard() != null ? selectedAddress.getWard() : "",
                                    selectedAddress.getDistrict() != null ? selectedAddress.getDistrict() : "",
                                    selectedAddress.getProvince() != null ? selectedAddress.getProvince() : "");
                            tvAddress.setText(addressText);
                            tvPhone.setText(selectedAddress.getReceiverPhone() != null ? selectedAddress.getReceiverPhone() : "Chưa có số điện thoại");
                        }
                    }
                });

        // Register ActivityResultLauncher for VNPay
        vnpayLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String action = result.getData().getStringExtra("action");
                        Log.d("VNPayResult", "Action: " + action);
                        if ("SuccessBackAction".equals(action)) {
                            placeOrder("VNPay");
                        } else if ("FaildBackAction".equals(action) || "WebBackAction".equals(action)) {
                            Toast.makeText(this, "Payment failed or cancelled", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Setup click listeners
        ivBack.setOnClickListener(v -> finish());
        ivEditAddressCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutActivity.this, AddressListActivity.class);
            intent.putExtra("selectMode", true);
            addressListLauncher.launch(intent);
        });
        ivEditContact.setOnClickListener(v -> {
            Toast.makeText(this, "Edit contact info not implemented", Toast.LENGTH_SHORT).show();
        });
        btnEditPayment.setOnClickListener(v -> rgPaymentMethod.setVisibility(View.VISIBLE));
        btnProceed.setOnClickListener(v -> proceedToPay());

        // Load data
        loadDefaultAddress();
        loadContactInfo();
    }

    private void fetchCartItems(List<CartItemLite> liteItems) {
        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) {
            Toast.makeText(this, "Please log in to proceed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<Cart> call = apiService.getCart(userId);
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Cart cart = response.body();
                    if (cart != null && cart.getCartItems() != null) {
                        for (CartItemLite liteItem : liteItems) {
                            for (CartItem item : cart.getCartItems()) {
                                if (item.getCartItemId().equals(liteItem.getCartItemId())) {
                                    // Create a new CartItem with the fetched data and liteItem's quantity
                                    CartItem cartItem = new CartItem();
                                    cartItem.setCartItemId(item.getCartItemId());
                                    cartItem.setProduct(item.getProduct());
                                    cartItem.setQuantity(liteItem.getQuantity());
                                    cartItem.setCart(item.getCart());
                                    selectedCartItems.add(cartItem);
                                    break;
                                }
                            }
                        }
                        if (selectedCartItems.isEmpty()) {
                            Toast.makeText(CheckoutActivity.this, "No matching cart items found", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        cartItemAdapter.notifyDataSetChanged();
                        updateTotalPrice();
                    } else {
                        Toast.makeText(CheckoutActivity.this, "No items found in cart", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(CheckoutActivity.this, "Failed to load cart items: " + response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Error loading cart items: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadDefaultAddress() {
        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) {
            Toast.makeText(this, "Please log in to proceed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<Address> call = apiService.getDefaultAddress(userId);
        call.enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {
                if (response.isSuccessful() && response.body() != null) {
                    selectedAddress = response.body();
                    String addressText = String.format("%s, %s, %s, %s",
                            selectedAddress.getAddress(),
                            selectedAddress.getWard(),
                            selectedAddress.getDistrict(),
                            selectedAddress.getProvince());
                    tvAddress.setText(addressText);

                    // Cập nhật tvPhone với receiverPhone từ selectedAddress
                    tvPhone.setText(selectedAddress.getReceiverPhone() != null ? selectedAddress.getReceiverPhone() : "Chưa có số điện thoại");
                    // Sau khi tải địa chỉ, tải email từ user
                    loadContactInfo();

                } else {
                    tvAddress.setText("Chưa có địa chỉ mặc định");
                    tvPhone.setText("Chưa có số điện thoại");
                    Toast.makeText(CheckoutActivity.this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CheckoutActivity.this, AddressListActivity.class);
                    intent.putExtra("selectMode", true);
                    addressListLauncher.launch(intent);
                }
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Error loading address: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadContactInfo() {
        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) return;

        Call<User> call = apiService.getUser(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    tvEmail.setText(user.getEmail() != null ? user.getEmail() : "Chưa có email");
                } else {
                    Toast.makeText(CheckoutActivity.this, "Lỗi khi tải email người dùng", Toast.LENGTH_SHORT).show();
                    tvEmail.setText("Lỗi tải email");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Lỗi khi tải email: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                tvEmail.setText("Lỗi tải email");
            }
        });

    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : selectedCartItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        tvTotal.setText(String.format("VND %.2f", total));
    }

    private void proceedToPay() {
        if (selectedAddress == null) {
            Toast.makeText(this, "Please set a address", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CheckoutActivity.this, AddressListActivity.class);
            intent.putExtra("selectMode", true);
            addressListLauncher.launch(intent);
            return;
        }

        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = selectedId == rbCOD.getId() ? "COD" : "VNPay";
        if (paymentMethod.equals("COD")) {
            Toast.makeText(this, "Proceeding with COD payment", Toast.LENGTH_SHORT).show();
            placeOrder(paymentMethod);
        } else {
            Toast.makeText(this, "Proceeding with VNPay payment", Toast.LENGTH_SHORT).show();
            // Tạo URL thanh toán từ backend (giả sử bạn đã có API tạo URL từ VNPay)
            createVNPayPaymentUrl();
        }
    }

    private void createVNPayPaymentUrl() {
        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) {
            Toast.makeText(this, "Please log in to proceed", Toast.LENGTH_SHORT).show();
            return;
        }

        double total = 0;
        for (CartItem item : selectedCartItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }

        // Giả sử bạn có API tạo URL thanh toán từ backend
        Map<String, String> paymentData = new HashMap<>();
        paymentData.put("amount", String.valueOf(total));
        paymentData.put("orderInfo", "Thanh toan don hang tu CosmeticShopApp");
        paymentData.put("orderType", "other");
        paymentData.put("returnUrl", "http://192.168.0.101:8080/api/vnpay-redirect"); // Thay bằng URL thực tế

        Call<ResponseBody> call = apiService.createVNPayPaymentUrl(userId, paymentData);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String paymentUrl = null;
                    try {
                        paymentUrl = response.body().string(); // <- Đọc raw text
                        openVNPaySDK(paymentUrl);
                    } catch (IOException e) {
                        Toast.makeText(CheckoutActivity.this, "Error reading payment URL", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Log.d("Create", call.toString());
                    Toast.makeText(CheckoutActivity.this, "Failed to create VNPay URL", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("vnpay", t.getMessage());
                Toast.makeText(CheckoutActivity.this, "Error creating VNPay URL: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openVNPaySDK(String paymentUrl) { ///
        Intent intent = new Intent(this, VNP_AuthenticationActivity.class);
        intent.putExtra("url", paymentUrl); // URL thanh toán từ backend
        intent.putExtra("tmn_code", tmnCode); // Mã TMN từ VNPay
        intent.putExtra("scheme", scheme); // Scheme để mở lại app
        intent.putExtra("is_sandbox", true); // Sử dụng môi trường test, đổi thành false cho live
        VNP_AuthenticationActivity.setSdkCompletedCallback(new VNP_SdkCompletedCallback() {
            @Override
            public void sdkAction(String action) {
                Log.wtf("CheckoutActivity", "action: " + action);
                if ("SuccessBackAction".equals(action)) {
                    placeOrder("VNPay"); // Xử lý đặt hàng sau khi thanh toán thành công
                } else if ("FaildBackAction".equals(action) || "WebBackAction".equals(action)) {
                    Toast.makeText(CheckoutActivity.this, "Payment failed or cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        });
        vnpayLauncher.launch(intent);
    }

    private void placeOrder(String paymentMethod) {
        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) {
            Toast.makeText(this, "Please log in to proceed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 1: Prepare order data
        double total = 0;
        for (CartItem item : selectedCartItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        OrderRequest orderRequest = new OrderRequest(
                userId,
                total,
                "PENDING", // Initial order status
                null, // Delivery date can be set later
                paymentMethod
        );

        // Step 2: Prepare order lines with product snapshots
        List<OrderLine> orderLineRequests = new ArrayList<>();
        for (CartItem item : selectedCartItems) {
            OrderLine orderLine = getOrderLineRequest(item);
            orderLineRequests.add(orderLine);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = LocalDateTime.now().format(formatter);

        // Step 3: Prepare payment data
        Payment paymentRequest = new Payment(
                paymentMethod,
                "PENDING", // Initial payment status
                total,
                formattedDate
        );

        // Step 4: Prepare shipping address data
        ShippingAddress shippingAddressRequest = new ShippingAddress(
                selectedAddress.getReceiverName(),
                selectedAddress.getReceiverPhone(),
                selectedAddress.getAddress(),
                selectedAddress.getProvince(),
                selectedAddress.getDistrict(),
                selectedAddress.getWard()
        );

        // Step 5: Execute API calls sequentially
        apiService.createOrder(orderRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("CheckoutActivity", "Order created successfully");
                    apiService.createOrderLines(orderLineRequests).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Log.d("CheckoutActivity", "Order lines created successfully");
                                apiService.createPayment(paymentRequest).enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if (response.isSuccessful()) {
                                            Log.d("CheckoutActivity", "Payment created successfully");
                                            apiService.createShippingAddress(shippingAddressRequest).enqueue(new Callback<Void>() {
                                                @Override
                                                public void onResponse(Call<Void> call, Response<Void> response) {
                                                    if (response.isSuccessful()) {
                                                        Log.d("CheckoutActivity", "Shipping address created successfully");
                                                        Toast.makeText(CheckoutActivity.this, "Order placed successfully with " + paymentMethod, Toast.LENGTH_SHORT).show();
                                                        apiService.clearCart(userId).enqueue(new Callback<Void>() {
                                                            @Override
                                                            public void onResponse(Call<Void> call, Response<Void> response) {
                                                                if (response.isSuccessful()) {
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(CheckoutActivity.this, "Error clearing cart: " + response.message(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure(Call<Void> call, Throwable t) {
                                                                Toast.makeText(CheckoutActivity.this, "Error clearing cart: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(CheckoutActivity.this, "Failed to save shipping address: " + response.message(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<Void> call, Throwable t) {
                                                    Toast.makeText(CheckoutActivity.this, "Error saving shipping address: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            Toast.makeText(CheckoutActivity.this, "Failed to save payment: " + response.message(), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        Toast.makeText(CheckoutActivity.this, "Error saving payment: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(CheckoutActivity.this, "Failed to save order lines: " + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(CheckoutActivity.this, "Error saving order lines: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(CheckoutActivity.this, "Failed to create order: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Error creating order: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    private static OrderLine getOrderLineRequest(CartItem item) {
        Product product = item.getProduct();
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("productName", product.getProductName());
        snapshot.put("productCode", product.getProductCode());
        snapshot.put("price", product.getPrice());
        snapshot.put("image", product.getImage());
        snapshot.put("brand", product.getBrand());
        snapshot.put("origin", product.getOrigin());
        snapshot.put("ingredient", product.getIngredient());
        snapshot.put("how_to_use", product.getHow_to_use());
        snapshot.put("description", product.getDescription());
        snapshot.put("volume", product.getVolume());
        snapshot.put("manufactureDate", product.getManufactureDate());
        snapshot.put("expirationDate", product.getExpirationDate());
        snapshot.put("createdDate", product.getCreatedDate());
        OrderLine orderLine = new OrderLine(
                item.getProduct().getProductId(),
                item.getQuantity(),
                snapshot
        );
        return orderLine;
    }
}