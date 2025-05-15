package vn.phatbee.cosmesticshopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.adapter.CartAdapter;
import vn.phatbee.cosmesticshopapp.manager.UserSessionManager;
import vn.phatbee.cosmesticshopapp.model.Cart;
import vn.phatbee.cosmesticshopapp.model.CartItem;
import vn.phatbee.cosmesticshopapp.model.CartItemRequest;
import vn.phatbee.cosmesticshopapp.model.User;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();

    private CheckBox checkboxSelectAll;
    private TextView tvTotalPrice;
    private Button btnConfirmCart, btnStartShopping;
    private View layoutEmpty, progressBar;

    private UserSessionManager userSessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize user session manager and get current user
        userSessionManager = new UserSessionManager(this);
        currentUser = userSessionManager.getUserDetails();

        // Check if user is logged in
        if (currentUser == null || currentUser.getUserId() == null) {
            Log.e("CartActivity", "User is not logged in or userId is null");
//            Intent intent = new Intent(this, LoginActivity.class);
//            startActivity(intent);
//            finish();
            return;
        }
        Log.d("CartActivity", "User ID: " + currentUser.getUserId());

        // Initialize UI components
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Setup recycler view
        setupRecyclerView();

        // Setup listeners
        setupListeners();

        // Load cart data
        loadCartData();
    }

    private void initializeViews() {
        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        checkboxSelectAll = findViewById(R.id.checkboxSelectAll);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnConfirmCart = findViewById(R.id.btnConfirmCart);
        btnStartShopping = findViewById(R.id.btnStartShopping);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBarBanner);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Shopping Cart");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupRecyclerView() {
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, cartItems, this);
        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        // Handle "Select All" checkbox
        checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartAdapter.selectAll(isChecked);
            updateTotalPrice();
        });

        // Handle Confirm Cart button
        btnConfirmCart.setOnClickListener(v -> {
            List<CartItem> selectedItems = cartAdapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(CartActivity.this, "Please select at least one item", Toast.LENGTH_SHORT).show();
                return;
            }
//            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
//            intent.putExtra("selectedCartItems", new ArrayList<>(selectedItems)); // Ensure ArrayList
//            startActivity(intent);
        });

        // Handle Start Shopping button (when cart is empty)
        btnStartShopping.setOnClickListener(v -> finish());
    }

    private void loadCartData() {
        showLoading(true);
        Long userId = currentUser.getUserId();
        Log.d("CartActivity", "Fetching cart for userId: " + userId);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Cart> call = apiService.getCart(userId);
        Log.d("CartActivity", "API Call URL: " + call.request().url());
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                showLoading(false);
                Log.d("CartActivity", "Response Code: " + response.code());
                Log.d("CartActivity", "Response Body: " + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    Cart cart = response.body();
                    if (cart != null && cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                        cartItems.clear();
                        cartItems.addAll(cart.getCartItems());
                        Log.d("CartActivity", "CartItems Size: " + cartItems.size());
                        cartAdapter.updateCartItems(cartItems);
                        showEmptyCartView(false);
                        updateTotalPrice();
                    } else {
                        Log.d("CartActivity", "Cart is empty or null");
                        showEmptyCartView(true);
                    }
                } else {
                    Log.e("CartActivity", "API error: " + response.message());
                    handleApiError("Failed to load cart: " + response.message());
                    showEmptyCartView(true);
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                showLoading(false);
                Log.e("CartActivity", "Network error: " + t.getMessage(), t);
                handleApiError("Network error: " + t.getMessage());
                showEmptyCartView(true);
            }
        });
    }

    private void updateCartItem(CartItem item, long quantity) {
        if (item == null || item.getProduct() == null) {
            Log.e("CartActivity", "Invalid cart item or product");
            handleApiError("Cannot update invalid item");
            return;
        }

        showLoading(true);
        CartItemRequest request = new CartItemRequest();
        request.setUserId(currentUser.getUserId());
        request.setProductId(item.getProduct().getProductId());
        request.setQuantity(quantity);
        request.setCartItemId(item.getCartItemId());

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Cart> call = apiService.updateCartItem(request);
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Cart updatedCart = response.body();
                    if (updatedCart != null && updatedCart.getCartItems() != null) {
                        cartItems.clear();
                        cartItems.addAll(updatedCart.getCartItems());
                        cartAdapter.updateCartItems(cartItems);
                        updateTotalPrice();
                        showEmptyCartView(cartItems.isEmpty());
                    } else {
                        showEmptyCartView(true);
                    }
                } else {
                    Log.e("CartActivity", "Failed to update cart item: " + response.message());
                    handleApiError("Failed to update cart item: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                showLoading(false);
                Log.e("CartActivity", "Network error: " + t.getMessage(), t);
                handleApiError("Network error: " + t.getMessage());
            }
        });
    }

    private void removeCartItem(CartItem item) {
        if (item == null || item.getCartItemId() == null) {
            Log.e("CartActivity", "Invalid cart item");
            handleApiError("Cannot remove invalid item");
            return;
        }

        showLoading(true);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Cart> call = apiService.removeFromCart(currentUser.getUserId(), item.getCartItemId());
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Cart updatedCart = response.body();
                    if (updatedCart != null && updatedCart.getCartItems() != null) {
                        cartItems.clear();
                        cartItems.addAll(updatedCart.getCartItems());
                        cartAdapter.updateCartItems(cartItems);
                        updateTotalPrice();
                        showEmptyCartView(cartItems.isEmpty());
                    } else {
                        showEmptyCartView(true);
                    }
                    Toast.makeText(CartActivity.this, "Item removed from cart", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("CartActivity", "Failed to remove cart item: " + response.message());
                    handleApiError("Failed to remove cart item: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                showLoading(false);
                Log.e("CartActivity", "Network error: " + t.getMessage(), t);
                handleApiError("Network error: " + t.getMessage());
            }
        });
    }

    private void updateTotalPrice() {
        double total = 0;
        List<CartItem> selectedItems = cartAdapter.getSelectedItems();
        for (CartItem item : selectedItems) {
            if (item != null && item.getProduct() != null) {
                total += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        tvTotalPrice.setText(String.format("%,.0f đ", total));
    }

    private void showEmptyCartView(boolean isEmpty) {
        if (isEmpty) {
            recyclerViewCart.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            checkboxSelectAll.setEnabled(false);
            btnConfirmCart.setEnabled(false);
        } else {
            recyclerViewCart.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            checkboxSelectAll.setEnabled(true);
            btnConfirmCart.setEnabled(true);
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void handleApiError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onQuantityChanged(int position, CartItem item, long newQuantity) {
        updateCartItem(item, newQuantity);
        updateTotalPrice();
    }

    @Override
    public void onItemCheckedChanged(int position, boolean isChecked) {
        checkboxSelectAll.setChecked(cartAdapter.areAllItemsSelected());
        updateTotalPrice();
    }

    @Override
    public void onItemRemoved(int position, CartItem item) {
        removeCartItem(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}