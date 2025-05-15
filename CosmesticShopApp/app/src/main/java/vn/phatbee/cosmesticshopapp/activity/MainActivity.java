package vn.phatbee.cosmesticshopapp.activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.adapter.BannerAdapter;
import vn.phatbee.cosmesticshopapp.adapter.CategoryAdapter;
import vn.phatbee.cosmesticshopapp.adapter.ProductRecentAdapter;
import vn.phatbee.cosmesticshopapp.model.Banner;
import vn.phatbee.cosmesticshopapp.model.Category;
import vn.phatbee.cosmesticshopapp.model.Product;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class MainActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener, ProductRecentAdapter.OnProductRecentClickListener {
    private TextView tvUsername;
    private SharedPreferences sharedPreferences;
    private ViewPager2 viewPagerSlider;
    private DotsIndicator dotsIndicator;
    private ProgressBar progressBarBanner, progressBarCategory, progressBarRecent;
    private List<Banner> banners = new ArrayList<>();
    private BannerAdapter bannerAdapter;
    private ProductRecentAdapter productRecentAdapter;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;

    private RecyclerView rvCategories, rvRecent;
    private CategoryAdapter categoryAdapter;
    private ImageView ivGioHang, ivWishList, ivProfile, ivSearch, ivDonHang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        dotsIndicator = findViewById(R.id.dotsIndicator);
        viewPagerSlider = findViewById(R.id.viewPager2);
        progressBarBanner = findViewById(R.id.progressBarBanner);
        tvUsername = findViewById(R.id.tvUsername);
        ivGioHang = findViewById(R.id.ivGioHang);
        ivProfile = findViewById(R.id.ivProfile);
        ivSearch = findViewById(R.id.ivSearch);
        ivWishList = findViewById(R.id.ivYeuThich);
        ivDonHang = findViewById(R.id.ivDonHang);
        rvCategories = findViewById(R.id.rvDanhMuc);
        progressBarCategory = findViewById(R.id.progressBarCategory);
        rvRecent = findViewById(R.id.rvRecent);
        progressBarRecent = findViewById(R.id.progressBarRecent);

        // Verify RecyclerViews are initialized
        if (rvCategories == null) {
            Toast.makeText(this, "rvCategories (rvDanhMuc) not found", Toast.LENGTH_LONG).show();
            return;
        }
        if (rvRecent == null) {
            Toast.makeText(this, "rvRecent not found", Toast.LENGTH_LONG).show();
            return;
        }

        // Set up adapters
        bannerAdapter = new BannerAdapter(this, banners);
        viewPagerSlider.setAdapter(bannerAdapter);
        dotsIndicator.setViewPager2(viewPagerSlider);

        categoryAdapter = new CategoryAdapter(this, this);
        productRecentAdapter = new ProductRecentAdapter(this, this); // Initialize adapter

        // Setup RecyclerView
        setupRecyclerView();

        // Load data
        loadBanners();
        loadCategories();
        loadRecentProducts();

        // Update username from SharedPreferences
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        tvUsername.setText(username);

        // Set up click listeners
        ivGioHang.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        ivSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        ivWishList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WishlistActivity.class);
            startActivity(intent);
        });

        ivDonHang.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OrderListActivity.class);
            startActivity(intent);
        });

        // Auto-scroll feature
        setupAutoScroll();
    }

    private void setupRecyclerView() {
        // Categories RecyclerView
        categoryAdapter = new CategoryAdapter(this, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(layoutManager);
        rvCategories.setAdapter(categoryAdapter);

        // Recent Products RecyclerView
        productRecentAdapter = new ProductRecentAdapter(this, this); // Ensure adapter is initialized
        LinearLayoutManager recentLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvRecent.setLayoutManager(recentLayoutManager);
        rvRecent.setAdapter(productRecentAdapter);
    }

    private void loadCategories() {
        progressBarCategory.setVisibility(View.VISIBLE);
        Call<List<Category>> call = RetrofitClient.getClient().create(ApiService.class).getCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                progressBarCategory.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    categoryAdapter.setCategories(response.body());
                } else {
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errorObject = new JSONObject(response.errorBody().string());
                            Toast.makeText(MainActivity.this, errorObject.getString("message"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                progressBarCategory.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecentProducts() {
        progressBarRecent.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getRecentProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressBarRecent.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    // Log the response
                    Log.d(TAG, "Recent Products Response: " + response.body());
                    for (Product product : response.body()) {
                        Log.d(TAG, "Product: ID=" + product.getProductId() +
                                ", Name=" + product.getProductName() +
                                ", Price=" + product.getPrice() +
                                ", Image=" + product.getImage());
                    }
                    productRecentAdapter.setProducts(response.body());
                } else {
                    Log.e(TAG, "Failed to load recent products: " + response.message() +
                            ", code: " + response.code() +
                            ", errorBody: " + (response.errorBody() != null ? response.errorBody().toString() : "null"));
                    Toast.makeText(MainActivity.this, "Failed to load recent products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                progressBarRecent.setVisibility(View.GONE);
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBanners() {
        progressBarBanner.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBanners().enqueue(new Callback<List<Banner>>() {
            @Override
            public void onResponse(Call<List<Banner>> call, Response<List<Banner>> response) {
                progressBarBanner.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    banners.clear();
                    banners.addAll(response.body());
                    bannerAdapter.notifyDataSetChanged();
                    dotsIndicator.setVisibility(banners.size() > 1 ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Banner>> call, Throwable t) {
                progressBarBanner.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Failed to load banners", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAutoScroll() {
        autoScrollHandler = new Handler();
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPagerSlider.getCurrentItem();
                int totalItems = bannerAdapter.getItemCount();
                if (totalItems > 1) {
                    int nextItem = (currentItem + 1) % totalItems;
                    viewPagerSlider.setCurrentItem(nextItem, true);
                    autoScrollHandler.postDelayed(this, 3000);
                }
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, 3000);
        viewPagerSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                autoScrollHandler.removeCallbacks(autoScrollRunnable);
                autoScrollHandler.postDelayed(autoScrollRunnable, 3000);
            }
        });
    }

    @Override
    public void onCategoryClick(Category category) {
        Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
        intent.putExtra("CATEGORY_ID", category.getCategoryId());
        intent.putExtra("CATEGORY_NAME", category.getCategoryName());
        startActivity(intent);
    }

    @Override
    public void onProductRecentClick(Product product) {
        Intent intent = new Intent(MainActivity.this, ProductDetailsActivity.class);
        intent.putExtra("PRODUCT_ID", product.getProductId());
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.postDelayed(autoScrollRunnable, 3000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewPagerSlider != null) {
            viewPagerSlider.unregisterOnPageChangeCallback(null);
        }
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;
            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;
                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }

}