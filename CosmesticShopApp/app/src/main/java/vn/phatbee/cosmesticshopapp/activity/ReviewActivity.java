package vn.phatbee.cosmesticshopapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.cloudinary.CloudinaryConfig;
import vn.phatbee.cosmesticshopapp.manager.UserSessionManager;
import vn.phatbee.cosmesticshopapp.model.ProductFeedback;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class ReviewActivity extends AppCompatActivity {

    private ImageView ivBack, ivProductImage, ivSelectedImage;
    private TextView tvProductName;
    private EditText etComment;
    private RatingBar rbRating;
    private Button btnSelectImage, btnSubmit;
    private ApiService apiService;
    private UserSessionManager sessionManager;
    private Long productId;
    private int orderId;
    private Map<String, Object> productSnapshot;
    private ProductFeedback existingFeedback;
    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private boolean isUploading = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        ivSelectedImage.setVisibility(View.VISIBLE);
                        Glide.with(this).load(selectedImageUri).into(ivSelectedImage);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Initialize Cloudinary
        CloudinaryConfig.init(this);

        // Initialize views
        ivBack = findViewById(R.id.ivBack);
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        etComment = findViewById(R.id.etComment);
        rbRating = findViewById(R.id.rbRating);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);
        btnSubmit = findViewById(R.id.btnSubmit);

        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = new UserSessionManager(this);

        // Get data from Intent
        productId = getIntent().getLongExtra("productId", -1);
        orderId = getIntent().getIntExtra("orderId", -1);
        productSnapshot = (HashMap<String, Object>) getIntent().getSerializableExtra("productSnapshot");
        existingFeedback = (ProductFeedback) getIntent().getSerializableExtra("existingFeedback");

        if (productId == -1 || orderId == -1 || productSnapshot == null) {
            Toast.makeText(this, "Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display product info
        tvProductName.setText(productSnapshot.get("productName") != null ? productSnapshot.get("productName").toString() : "N/A");
        String productImageUrl = productSnapshot.get("image") != null ? productSnapshot.get("image").toString() : "";
        Glide.with(this)
                .load(productImageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivProductImage);

        // Pre-populate fields if editing
        if (existingFeedback != null) {
            etComment.setText(existingFeedback.getComment());
            rbRating.setRating(existingFeedback.getRating() != null ? existingFeedback.getRating().floatValue() : 0);
            btnSubmit.setText("Cập nhật đánh giá");
            if (existingFeedback.getImage() != null && !existingFeedback.getImage().isEmpty()) {
                ivSelectedImage.setVisibility(View.VISIBLE);
                Glide.with(this).load(existingFeedback.getImage()).into(ivSelectedImage);
                uploadedImageUrl = existingFeedback.getImage();
            }
        }

        // Handle back button
        ivBack.setOnClickListener(v -> finish());

        // Handle image selection
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Handle submit button
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        String comment = etComment.getText().toString().trim();
        float rating = rbRating.getRating();
        Long customerId = sessionManager.getUserDetails().getUserId();

        // Validate input
        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
            return;
        }
        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        if (customerId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isUploading) {
            Toast.makeText(this, "Đang tải hình ảnh, vui lòng đợi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create feedback object
        ProductFeedback feedback = new ProductFeedback();
        feedback.setComment(comment);
        feedback.setRating((double) rating);
        feedback.setCustomerId(customerId);
        feedback.setOrderId(orderId);
        feedback.setProductId(productId);
        feedback.setProductSnapshotName(productSnapshot.get("productName") != null ? productSnapshot.get("productName").toString() : "N/A");

        // Handle image upload if selected
        if (selectedImageUri != null && uploadedImageUrl == null) {
            uploadImageToCloudinary(feedback);
        } else {
            feedback.setImage(uploadedImageUrl); // Use existing URL if no new image
            submitFeedbackToBackend(feedback);
        }
    }

    private void uploadImageToCloudinary(ProductFeedback feedback) {
        isUploading = true;
        btnSubmit.setEnabled(false);
        Toast.makeText(this, "Đang tải hình ảnh...", Toast.LENGTH_SHORT).show();

        String requestId = MediaManager.get().upload(selectedImageUri)
                .unsigned("cosmesticshop_preset") // Optional: Use an upload preset
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // Upload started
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Upload progress
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        isUploading = false;
                        btnSubmit.setEnabled(true);
                        uploadedImageUrl = (String) resultData.get("secure_url");
                        feedback.setImage(uploadedImageUrl);
                        submitFeedbackToBackend(feedback);
                        Toast.makeText(ReviewActivity.this, "Tải hình ảnh thành công", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        isUploading = false;
                        btnSubmit.setEnabled(true);
                        Toast.makeText(ReviewActivity.this, "Lỗi tải hình ảnh: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Handle reschedule
                    }
                })
                .dispatch();
    }

    private void submitFeedbackToBackend(ProductFeedback feedback) {
        Call<ProductFeedback> call;
        if (existingFeedback != null) {
            feedback.setProductFeedbackId(existingFeedback.getProductFeedbackId());
            call = apiService.updateFeedback(feedback.getProductFeedbackId(), feedback);
        } else {
            call = apiService.createFeedback(feedback);
        }

        call.enqueue(new Callback<ProductFeedback>() {
            @Override
            public void onResponse(Call<ProductFeedback> call, Response<ProductFeedback> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ReviewActivity.this, existingFeedback != null ? "Đã cập nhật đánh giá" : "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ReviewActivity.this, "Không thể gửi đánh giá", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductFeedback> call, Throwable t) {
                Toast.makeText(ReviewActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}