package vn.phatbee.cosmesticshopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.adapter.AllProductsAdapter;
import vn.phatbee.cosmesticshopapp.manager.UserSessionManager;
import vn.phatbee.cosmesticshopapp.model.Product;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class AllProductsActivity extends AppCompatActivity implements AllProductsAdapter.OnProductClickListener {
    private static final String TAG = "AllProductsActivity";
    private RecyclerView rvAllProducts;
    private ProgressBar progressBarAllProducts;
    private AllProductsAdapter allProductsAdapter;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_products);

        // Initialize views
        rvAllProducts = findViewById(R.id.rvAllProducts);
        progressBarAllProducts = findViewById(R.id.progressBarAllProducts);

        sessionManager = new UserSessionManager(this);

        // Set up RecyclerView
        allProductsAdapter = new AllProductsAdapter(this, this, sessionManager);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2); // Sử dụng GridLayoutManager với 2 cột
        rvAllProducts.setLayoutManager(layoutManager);
        rvAllProducts.setAdapter(allProductsAdapter);

        // Load all products
        loadAllProducts();
    }

    private void loadAllProducts() {
        progressBarAllProducts.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressBarAllProducts.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "All Products Response: " + response.body());
                    allProductsAdapter.setProducts(response.body());
                } else {
                    Log.e(TAG, "Failed to load all products: " + response.message() +
                            ", code: " + response.code() +
                            ", errorBody: " + (response.errorBody() != null ? response.errorBody().toString() : "null"));
                    Toast.makeText(AllProductsActivity.this, "Failed to load all products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                progressBarAllProducts.setVisibility(View.GONE);
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                Toast.makeText(AllProductsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        // Chuyển đến màn hình chi tiết sản phẩm
        Intent intent = new Intent(AllProductsActivity.this, ProductDetailsActivity.class);
        intent.putExtra("PRODUCT_ID", product.getProductId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllProducts(); // Cập nhật danh sách để đồng bộ trạng thái wishlist
    }
}