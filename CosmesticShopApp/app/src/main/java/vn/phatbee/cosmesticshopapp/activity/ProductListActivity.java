package vn.phatbee.cosmesticshopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener{
    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private TextView tvCategoryName;
    private ProgressBar progressBar;
    private SearchView searchView;

    private List<Product> productList = new ArrayList<>();
    private int categoryId;
    private String categoryName;

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
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchView);

        // Set category name
        tvCategoryName.setText(categoryName);

        // Setup RecyclerView
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(this, productList, this);
        recyclerViewProducts.setAdapter(productAdapter);

        // Load products for this category
        loadProductsByCategory(categoryId);

        //Tim kiem san pham
        setupSearchView();
    }

    private void loadProductsByCategory(int categoryId) {
        if (categoryId == -1) {
            Toast.makeText(this, "Invalid category", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Product>> call = apiService.getProductsByCategory(categoryId);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    productList = response.body();
                    productAdapter.updateProductList(productList);

                    if (productList.isEmpty()) {
                        Toast.makeText(ProductListActivity.this,
                                "No products found in this category", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductListActivity.this,
                            "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable throwable) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductListActivity.this,
                        "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        // Handle product click, for example navigate to product detail
        Intent intent = new Intent(this, ProductDetailsActivity.class);
        intent.putExtra("PRODUCT_ID", product.getProductId());
        startActivity(intent);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private Handler handler = new Handler();
            private Runnable searchRunnable;
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //TH nguoi dung nhap qua nhanh
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                if (newText.isEmpty()) {
                    loadProductsByCategory(categoryId); // Reload category products if search is cleared
                } else {
                    searchRunnable = () -> searchProducts(newText);
                    handler.postDelayed(searchRunnable, 300); // Delay 300ms
                }
                return true;
            }
        });
    }

    private void searchProducts(String keyword) {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Product>> call = apiService.searchProducts(keyword);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    productList = response.body();
                    productAdapter.updateProductList(productList);

                    if (productList.isEmpty()) {
                        Toast.makeText(ProductListActivity.this,
                                "No products found for: " + keyword, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductListActivity.this,
                            "Failed to search products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable throwable) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductListActivity.this,
                        "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}