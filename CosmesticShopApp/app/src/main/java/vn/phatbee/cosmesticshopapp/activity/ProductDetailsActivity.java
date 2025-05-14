package vn.phatbee.cosmesticshopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.manager.UserSessionManager;
import vn.phatbee.cosmesticshopapp.model.Cart;
import vn.phatbee.cosmesticshopapp.model.CartItem;
import vn.phatbee.cosmesticshopapp.model.CartItemRequest;
import vn.phatbee.cosmesticshopapp.model.Product;
import vn.phatbee.cosmesticshopapp.model.Wishlist;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class ProductDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailsActivity";
    private ImageView btnback;
    private ImageView btnWish, btnCart;
    private ImageView iProduct;
    private TextView tvProductName, tvCategory, tvPrice, tvBrand, tvVolume;
    private TextView tvOrigin, tvManufactureDate, tvExpirationDate;
    private TextView tvDescription, tvHowToUse, tvIngredients;
    private AppCompatButton btnBuy;
    private Toolbar toolbar;
    private Product currentProduct;
    private Long productId;
    private UserSessionManager sessionManager;
    private boolean isInWishlist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Initialize UserSessionManager
        sessionManager = new UserSessionManager(this);

        anhXa();

        productId = getIntent().getLongExtra("PRODUCT_ID", -1);
        if (productId == -1) {
            Toast.makeText(this, "Error: Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnback.setOnClickListener(view -> finish());

        // Add to cart listener
        btnCart.setOnClickListener(view -> {
            if (currentProduct == null) {
                Toast.makeText(this, "Product details not loaded", Toast.LENGTH_SHORT).show();
                return;
            }
            addToCart(1L, false); // Add to cart without navigating
        });

        // Buy now listener
        btnBuy.setOnClickListener(view -> {
            if (currentProduct == null) {
                Toast.makeText(this, "Product details not loaded", Toast.LENGTH_SHORT).show();
                return;
            }
            addToCart(1L, true); // Add to cart and navigate to CheckoutActivity
        });

        btnWish = findViewById(R.id.ivWishlist);
        btnWish.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailsActivity.this, WishlistActivity.class);
            startActivity(intent);
        });

        btnWish.setOnClickListener(view -> toggleWishlist());

        getProductDetails();
        checkWishlistStatus();
    }

    void anhXa() {
        btnback = findViewById(R.id.ivBack);
        iProduct = findViewById(R.id.iProduct);
        btnWish = findViewById(R.id.ibtnWish);
        btnCart = findViewById(R.id.ibtnCart);
        btnBuy = findViewById(R.id.btnBuy);
        tvProductName = findViewById(R.id.tvProductName);
        tvCategory = findViewById(R.id.tvCategory);
        tvPrice = findViewById(R.id.tvPrice);
        tvBrand = findViewById(R.id.tvBrand);
        tvVolume = findViewById(R.id.tvVolume);
        tvOrigin = findViewById(R.id.tvOrigin);
        tvManufactureDate = findViewById(R.id.tvManufactureDate);
        tvExpirationDate = findViewById(R.id.tvExpirationDate);
        tvDescription = findViewById(R.id.tvDescription);
        tvHowToUse = findViewById(R.id.tvHowToUse);
        tvIngredients = findViewById(R.id.tvIngredients);
//        toolbar = findViewById(R.id.toolbar);
    }

    private void getProductDetails() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Product> productCall = apiService.getProductDetails(productId);
        productCall.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body();
                    displayProductDetails(currentProduct);
                } else {
                    Log.e(TAG, "Failed to load product details: HTTP " + response.code() + ", message: " + response.message());
                    Toast.makeText(ProductDetailsActivity.this, "Failed to load product details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable throwable) {
                Log.e(TAG, "Error loading product details: " + throwable.getMessage(), throwable);
                Toast.makeText(ProductDetailsActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProductDetails(Product product) {
        tvProductName.setText(product.getProductName());
        tvCategory.setText(product.getCategory() != null ? product.getCategory().getCategoryName() : "Unknown");
        tvPrice.setText(String.format("%.2f VND", product.getPrice()));
        tvBrand.setText(product.getBrand());
        tvVolume.setText(product.getVolume());
        tvOrigin.setText(product.getOrigin());
        tvManufactureDate.setText(product.getManufactureDate());
        tvExpirationDate.setText(product.getExpirationDate());
        tvDescription.setText(product.getDescription());
        tvHowToUse.setText(product.getHow_to_use());
        tvIngredients.setText(product.getIngredient());
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(this).load(product.getImage()).placeholder(R.drawable.ic_launcher_background).into(iProduct);
        } else {
            iProduct.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    private void addToCart(Long quantity, boolean navigateToCheckout) {
        // Check if user is logged in
        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) {
            Toast.makeText(this, "Please log in to add items to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create CartItemRequest
        CartItemRequest cartItemRequest = new CartItemRequest(userId, currentProduct.getProductId(), quantity);
        Log.d(TAG, "Adding to cart: userId=" + userId + ", productId=" + currentProduct.getProductId() + ", quantity=" + quantity);

        // Make API call to add to cart
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Cart> call = apiService.addToCart(cartItemRequest);
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Cart added successfully: cartId=" + response.body().getCartId());
                    Toast.makeText(ProductDetailsActivity.this, currentProduct.getProductName() + " added to cart", Toast.LENGTH_SHORT).show();
                    if (navigateToCheckout) {
                        CartItem cartItem = createCartItem(quantity);
                        if (cartItem != null) {
                            Intent intent = new Intent(ProductDetailsActivity.this, CheckoutActivity.class);
                            ArrayList<CartItem> cartItems = new ArrayList<>();
                            cartItems.add(cartItem);
                            intent.putExtra("selectedCartItems", cartItems);
                            startActivity(intent);
                            Toast.makeText(ProductDetailsActivity.this, "Proceeding to checkout", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to add to cart: HTTP " + response.code() + ", message: " + response.message());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                    Toast.makeText(ProductDetailsActivity.this, "Failed to add to cart: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Log.e(TAG, "Error adding to cart: " + t.getMessage(), t);
                Toast.makeText(ProductDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private CartItem createCartItem(Long quantity) {
        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) {
            Toast.makeText(this, "Please log in to proceed", Toast.LENGTH_SHORT).show();
            return null;
        }

        CartItem cartItem = new CartItem();
        cartItem.setProduct(currentProduct);
        cartItem.setQuantity(quantity);
        cartItem.setCartItemId(System.currentTimeMillis()); // Temporary ID for local use
        return cartItem;
    }
    private void checkWishlistStatus() {
        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) {
            btnWish.setImageResource(R.drawable.ic_heart);
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Boolean> call = apiService.isProductInWishlist(userId, productId);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isInWishlist = response.body();
                    btnWish.setImageResource(isInWishlist ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(TAG, "Error checking wishlist status: " + t.getMessage());
            }
        });
    }
    private void toggleWishlist() {
        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) {
            Toast.makeText(this, "Please log in to manage wishlist", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        if (isInWishlist) {
            Call<Void> call = apiService.removeFromWishlist(userId, productId);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        isInWishlist = false;
                        btnWish.setImageResource(R.drawable.ic_heart);
                        Toast.makeText(ProductDetailsActivity.this, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductDetailsActivity.this, "Failed to remove from wishlist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Error removing from wishlist: " + t.getMessage());
                    Toast.makeText(ProductDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Call<Wishlist> call = apiService.addToWishlist(userId, productId);
            call.enqueue(new Callback<Wishlist>() {
                @Override
                public void onResponse(Call<Wishlist> call, Response<Wishlist> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        isInWishlist = true;
                        btnWish.setImageResource(R.drawable.ic_heart_filled);
                        Toast.makeText(ProductDetailsActivity.this, "Added to wishlist", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductDetailsActivity.this, "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Wishlist> call, Throwable t) {
                    Log.e(TAG, "Error adding to wishlist: " + t.getMessage());
                    Toast.makeText(ProductDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}