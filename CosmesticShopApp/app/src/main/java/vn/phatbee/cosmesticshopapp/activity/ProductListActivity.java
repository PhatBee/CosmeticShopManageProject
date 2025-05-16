package vn.phatbee.cosmesticshopapp.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.adapter.ProductAdapter;
import vn.phatbee.cosmesticshopapp.model.Product;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {
    private static final String PREFS_NAME = "SearchHistory";
    private static final String KEY_LAST_SEARCH = "last_search";
    private static final long DEBOUNCE_DELAY_MS = 300;

    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private TextView tvCategoryName;
    private TextView tvNotFound;
    private ProgressBar progressBar;
    private SearchView searchView;
    private List<Product> productList = new ArrayList<>(); // Danh sách gốc của danh mục
    private List<Product> filteredProductList = new ArrayList<>(); // Danh sách đã lọc
    private int categoryId;
    private String categoryName;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Get data from Intent
        Intent intent = getIntent();
        categoryId = intent.getIntExtra("CATEGORY_ID", -1);
        categoryName = intent.getStringExtra("CATEGORY_NAME");

        // Initialize views
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvNotFound = findViewById(R.id.tvNotFound);
        progressBar = findViewById(R.id.progressBarBanner);
        searchView = findViewById(R.id.searchView);

        // Set category name
        tvCategoryName.setText(categoryName);

        // Setup RecyclerView
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(this, filteredProductList, this);
        recyclerViewProducts.setAdapter(productAdapter);

        // Setup SearchView
        setupSearchView();

        // Load products for this category
        loadProductsByCategory(categoryId);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private Runnable searchRunnable;

            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> filterProducts(newText);
                handler.postDelayed(searchRunnable, DEBOUNCE_DELAY_MS);
                return true;
            }
        });
    }

    private void filterProducts(String query) {
        if (!isNetworkAvailable()) {
            clearProductListAndShowNotFound();
            return;
        }

//        saveSearchQuery(query);
        progressBar.setVisibility(View.GONE); // Không cần ProgressBar khi lọc
        tvNotFound.setVisibility(View.GONE);

        if (query == null || query.trim().isEmpty()) {
            filteredProductList.clear();
            filteredProductList.addAll(productList); // Hiển thị toàn bộ danh mục khi từ khóa rỗng
        } else {
            String searchQuery = query.trim().toLowerCase();
            filteredProductList = productList.stream()
                    .filter(product -> product.getProductName().toLowerCase().contains(searchQuery))
                    .collect(Collectors.toList());
        }

        productAdapter.updateProductList(filteredProductList);
        if (filteredProductList.isEmpty()) {
            tvNotFound.setVisibility(View.VISIBLE);
        } else {
            tvNotFound.setVisibility(View.GONE);
        }
    }

    private void loadProductsByCategory(int categoryId) {
        if (categoryId == -1) {
            clearProductListAndShowNotFound();
            return;
        }

        if (!isNetworkAvailable()) {
            clearProductListAndShowNotFound();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvNotFound.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Product>> call = apiService.getProductsByCategory(categoryId);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    productList = response.body(); // Lưu danh sách gốc
                    filteredProductList.clear();
                    filteredProductList.addAll(productList); // Hiển thị toàn bộ danh mục ban đầu
                    productAdapter.updateProductList(filteredProductList);
                    if (productList.isEmpty()) {
                        clearProductListAndShowNotFound();
                    }
                } else {
                    clearProductListAndShowNotFound();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable throwable) {
                progressBar.setVisibility(View.GONE);
                clearProductListAndShowNotFound();
            }
        });
    }

    private void clearProductListAndShowNotFound() {
        productList.clear();
        filteredProductList.clear();
        productAdapter.updateProductList(filteredProductList);
        tvNotFound.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailsActivity.class);
        intent.putExtra("PRODUCT_ID", product.getProductId());
        startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }

//    private void saveSearchQuery(String query) {
//        if (query.trim().isEmpty()) return;
//        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        String history = prefs.getString("search_history", "");
//        List<String> historyList = new ArrayList<>(Arrays.asList(history.isEmpty() ? new String[]{} : history.split(",")));
//        if (!historyList.contains(query)) {
//            historyList.add(0, query);
//            if (historyList.size() > 5) historyList.remove(historyList.size() - 1); // Giới hạn 5 lịch sử
//            prefs.edit().putString("search_history", String.join(",", historyList)).apply();
//        }
//        prefs.edit().putString(KEY_LAST_SEARCH, query).apply();
//    }

//    private void loadLastSearchQuery() {
//        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        String lastSearch = prefs.getString(KEY_LAST_SEARCH, "");
//        if (!lastSearch.isEmpty()) {
//            searchView.setQuery(lastSearch, false);
//            filterProducts(lastSearch);
//        }
//    }
}