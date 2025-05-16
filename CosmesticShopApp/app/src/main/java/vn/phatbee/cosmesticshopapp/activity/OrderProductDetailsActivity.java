package vn.phatbee.cosmesticshopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

import vn.phatbee.cosmesticshopapp.R;

public class OrderProductDetailsActivity extends AppCompatActivity {

    private ImageView ivBack, iProduct;
    private TextView tvProductName, tvCategory, tvPrice, tvBrand, tvVolume, tvOrigin;
    private TextView tvManufactureDate, tvExpirationDate, tvDescription, tvHowToUse, tvIngredients;
    private Button btnViewCurrentProduct;
    private Map<String, Object> productSnapshot;
    private Long productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_product_details);

        // Initialize views
        ivBack = findViewById(R.id.ivBackOrderProductDetail);
        iProduct = findViewById(R.id.iProduct);
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
        btnViewCurrentProduct = findViewById(R.id.btnViewCurrentProduct);

        // Get data from Intent
        productSnapshot = (HashMap<String, Object>) getIntent().getSerializableExtra("productSnapshot");
        productId = getIntent().getLongExtra("productId", -1L);

        if (productSnapshot == null) {
            Toast.makeText(this, "Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display product details from snapshot
        displayProductDetails();

        // Back button
        ivBack.setOnClickListener(v -> finish());

        // View current product button
        btnViewCurrentProduct.setOnClickListener(v -> {
            if (productId == -1L) {
                Toast.makeText(this, "Không thể xem thông tin sản phẩm hiện tại", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ProductDetailsActivity.class);
            intent.putExtra("PRODUCT_ID", productId);
            startActivity(intent);
        });
    }

    private void displayProductDetails() {
        tvProductName.setText(productSnapshot.get("productName") != null ? productSnapshot.get("productName").toString() : "N/A");
        tvCategory.setText(productSnapshot.get("categoryName") != null ? "Danh mục: " + productSnapshot.get("categoryName").toString() : "Danh mục: N/A");
        tvPrice.setText(productSnapshot.get("price") != null ? String.format("Giá: %,.0f VND", Double.parseDouble(productSnapshot.get("price").toString())) : "Giá: N/A");
        tvBrand.setText(productSnapshot.get("brand") != null ? "Thương hiệu: " + productSnapshot.get("brand").toString() : "Thương hiệu: N/A");
        tvVolume.setText(productSnapshot.get("volume") != null ? "Dung tích: " + productSnapshot.get("volume").toString() : "Dung tích: N/A");
        tvOrigin.setText(productSnapshot.get("origin") != null ? "Xuất xứ: " + productSnapshot.get("origin").toString() : "Xuất xứ: N/A");
        tvManufactureDate.setText(productSnapshot.get("manufactureDate") != null ? "Ngày sản xuất: " + productSnapshot.get("manufactureDate").toString() : "Ngày sản xuất: N/A");
        tvExpirationDate.setText(productSnapshot.get("expirationDate") != null ? "Hạn sử dụng: " + productSnapshot.get("expirationDate").toString() : "Hạn sử dụng: N/A");
        tvDescription.setText(productSnapshot.get("description") != null ? "Mô tả: " + productSnapshot.get("description").toString() : "Mô tả: N/A");
        tvHowToUse.setText(productSnapshot.get("how_to_use") != null ? "Cách dùng: " + productSnapshot.get("how_to_use").toString() : "Cách dùng: N/A");
        tvIngredients.setText(productSnapshot.get("ingredient") != null ? "Thành phần: " + productSnapshot.get("ingredient").toString() : "Thành phần: N/A");

        String imageUrl = productSnapshot.get("image") != null ? productSnapshot.get("image").toString() : "";
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(iProduct);
    }
}