package vn.phatbee.cosmesticshopapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.adapter.ProductAdapter;
import vn.phatbee.cosmesticshopapp.model.Product;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class SearchActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {
    private SearchView searchView;
    private RecyclerView recyclerViewSearchResults;
    private ProgressBar progressBar;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize views
        searchView = findViewById(R.id.searchView);
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        recyclerViewSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(this, productList, this);
        recyclerViewSearchResults.setAdapter(productAdapter);

        // Setup SearchView
        setupSearchView();

        // Load last search query
        loadLastSearchQuery();

        // Focus on SearchView and show keyboard
        searchView.setIconified(false);
        searchView.requestFocus();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private Runnable searchRunnable;

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> searchProducts(newText);
                handler.postDelayed(searchRunnable, 300); // Debounce 300ms
                return true;
            }
        });
    }

    private void searchProducts(String keyword) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Product>> call;

        if (keyword.trim().isEmpty()) {
            saveSearchQuery(keyword);
            call = apiService.getProducts(); // Load all products
        } else {
            call = apiService.searchProducts(keyword); // Search with keyword
        }

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    productList = response.body();
                    productAdapter.updateProductList(productList);
                    if (productList.isEmpty()) {
                        Toast.makeText(SearchActivity.this,
                                keyword.trim().isEmpty() ? "No products available" : "No products found for: " + keyword,
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SearchActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable throwable) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailsActivity.class);
        intent.putExtra("PRODUCT_ID", product.getProductId());
        startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }
    //Luu lich su tim kiem
    private void saveSearchQuery(String query) {
        SharedPreferences prefs = getSharedPreferences("SearchHistory", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_search", query);
        editor.apply();
    }

    private void loadLastSearchQuery() {
        SharedPreferences prefs = getSharedPreferences("SearchHistory", MODE_PRIVATE);
        String lastSearch = prefs.getString("last_search", "");
        if (!lastSearch.isEmpty()) {
            searchView.setQuery(lastSearch, false);
        }
    }

    private void loadAllProducts() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Product>> call = apiService.getProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    productList = response.body();
                    productAdapter.updateProductList(productList);
                    if (productList.isEmpty()) {
                        Toast.makeText(SearchActivity.this, "No products available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SearchActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable throwable) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}