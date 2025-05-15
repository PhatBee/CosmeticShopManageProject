package vn.phatbee.cosmesticshopapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
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
import vn.phatbee.cosmesticshopapp.model.Banner;
import vn.phatbee.cosmesticshopapp.model.Category;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class MainActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {
    private TextView tvUsername;
    private SharedPreferences sharedPreferences;
    private ViewPager2 viewPagerSlider;
    private DotsIndicator dotsIndicator;
    private ProgressBar progressBarBanner;
    private List<Banner> banners = new ArrayList<>();
    private BannerAdapter bannerAdapter;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;

    private RecyclerView rvCategories;
    private ProgressBar progressBarCategory;
    private CategoryAdapter categoryAdapter;
    private ImageView ivGioHang, ivWishList;
    private ImageView ivProfile;
    private ImageView ivSearch;

    private ImageView ivDonHang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        dotsIndicator = findViewById(R.id.dotsIndicator);
        viewPagerSlider = findViewById(R.id.viewPager2);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        progressBarBanner = findViewById(R.id.progressBar2);
        tvUsername = findViewById(R.id.tvUsername);
        ivGioHang = findViewById(R.id.ivGioHang);
        ivProfile = findViewById(R.id.ivProfile);
        ivSearch = findViewById(R.id.ivSearch);
        ivWishList = findViewById(R.id.ivYeuThich);
        ivDonHang = findViewById(R.id.ivDonHang);

        rvCategories = findViewById(R.id.rvDanhMuc);
        progressBarCategory = findViewById(R.id.progressBar3);

        // Set up adapter
        bannerAdapter = new BannerAdapter(this, banners);
        viewPagerSlider.setAdapter(bannerAdapter);

        // Connect dots indicator with ViewPager2
        dotsIndicator.setViewPager2(viewPagerSlider);

        // Load banners data
        loadBanners();

        // Auto-scroll feature
        setupAutoScroll();

        // Setup RecyclerView
        setupRecyclerView();

        // Load categories
        loadCategories();

        // Update username from SharedPreferences
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        tvUsername.setText(username);

        ivGioHang.setOnClickListener( v -> {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        ivProfile.setOnClickListener( v -> {
//            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
//            startActivity(intent);
        });

        ivSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        ivWishList.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, WishlistActivity.class);
//            startActivity(intent);
        });

        ivDonHang.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, OrderListActivity.class);
//            startActivity(intent);
        });

    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter(this, this);

        // Use GridLayoutManager for grid display (typically for categories)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(layoutManager);
        rvCategories.setAdapter(categoryAdapter);

//        // Add item decoration for spacing if needed
//        rvCategories.addItemDecoration(new GridSpacingItemDecoration(1,
//                dpToPx(16), true));
    }

    private void loadCategories() {
        progressBarCategory.setVisibility(View.VISIBLE);

        // Make API call
        Call<List<Category>> call = RetrofitClient.getClient().create(ApiService.class).getCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                // Hide progress bar
                progressBarCategory.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    // Update adapter with fetched categories
                    categoryAdapter.setCategories(response.body());
                } else {
                    // Handle error response
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errorObject = new JSONObject(response.errorBody().string());
                            Toast.makeText(MainActivity.this, errorObject.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to load categories",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                // Hide progress bar
                progressBarCategory.setVisibility(View.GONE);

                // Show error message
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCategoryClick(Category category) {
        // Handle category click
        // For example, navigate to products list filtered by this category
        Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
        intent.putExtra("CATEGORY_ID", category.getCategoryId());
        intent.putExtra("CATEGORY_NAME", category.getCategoryName());
        startActivity(intent);
    }

    private void loadBanners() {
        progressBarBanner.setVisibility(View.VISIBLE);

        // Use Retrofit to fetch banner data from your Spring Boot backend
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBanners().enqueue(new Callback<List<Banner>>() {
            @Override
            public void onResponse(Call<List<Banner>> call, Response<List<Banner>> response) {
                progressBarBanner.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    banners.clear();
                    banners.addAll(response.body());
                    bannerAdapter.notifyDataSetChanged();

                    // Show the indicator if we have more than one banner
                    dotsIndicator.setVisibility(banners.size() > 1 ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Banner>> call, Throwable t) {
                progressBarBanner.setVisibility(View.GONE);
                // Handle error
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
                    autoScrollHandler.postDelayed(this, 3000); // Change banner every 3 seconds
                }
            }
        };

        autoScrollHandler.postDelayed(autoScrollRunnable, 3000);

        // Remember to reset timer when user manually swipes
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
    protected void onPause() {
        super.onPause();
        // Stop auto-scrolling when activity is paused
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume auto-scrolling when activity is resumed
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.postDelayed(autoScrollRunnable, 3000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (viewPagerSlider != null) {
            viewPagerSlider.unregisterOnPageChangeCallback(null);
        }

        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    // Utility method to convert dp to pixels
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // Utility class for adding spacing between grid items
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