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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.adapter.ProductAdapter;
import vn.phatbee.cosmesticshopapp.model.Product;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class SearchActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {
    private static final String PREFS_NAME = "SearchHistory";
    private static final String KEY_LAST_SEARCH = "last_search";
    private static final long DEBOUNCE_DELAY_MS = 300;

    private SearchView searchView;
    private RecyclerView recyclerViewSearchResults;
    private ProgressBar progressBar;
    private TextView tvNotFound;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initializeViews();
        setupRecyclerView();
        setupSearchView();
//        loadLastSearchQuery();
        focusSearchView();
    }

    private void initializeViews() {
        searchView = findViewById(R.id.searchView);
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);
        progressBar = findViewById(R.id.progressBarBanner);
        tvNotFound = findViewById(R.id.tvNotFound);
    }

    private void setupRecyclerView() {
        recyclerViewSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(this, productList, this);
        recyclerViewSearchResults.setAdapter(productAdapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private Runnable searchRunnable;

            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> performSearch(newText);
                handler.postDelayed(searchRunnable, DEBOUNCE_DELAY_MS);
                return true;
            }
        });
    }

    private void focusSearchView() {
        searchView.setIconified(false);
        searchView.requestFocus();
    }

    private void performSearch(String keyword) {
        if (!isNetworkAvailable()) {
            clearProductListAndShowNotFound();
            return;
        }

//        saveSearchQuery(keyword);
        progressBar.setVisibility(View.VISIBLE);
        tvNotFound.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Product>> call = keyword.trim().isEmpty() ? apiService.getProducts() : apiService.searchProducts(keyword);

        fetchProducts(call, keyword);
    }

    private void fetchProducts(Call<List<Product>> call, String keyword) {
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    productList = response.body();
                    productAdapter.updateProductList(productList);
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
        productAdapter.updateProductList(productList);
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
//
//    private void loadLastSearchQuery() {
//        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        String lastSearch = prefs.getString(KEY_LAST_SEARCH, "");
//        if (!lastSearch.isEmpty()) {
//            searchView.setQuery(lastSearch, false);
//            performSearch(lastSearch);
//        }
//    }
}