package vn.phatbee.cosmesticshopapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.cloudinary.CloudinaryConfig;
import vn.phatbee.cosmesticshopapp.manager.UserSessionManager;
import vn.phatbee.cosmesticshopapp.model.User;
import vn.phatbee.cosmesticshopapp.model.UserUpdateDTO;
import vn.phatbee.cosmesticshopapp.model.UserUpdateResponse;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class EditAccountActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etPhone;
    private AutoCompleteTextView genderSpinner;
    private AppCompatButton btnUpdateProfile;
    private ImageView ivBack, ivProfileImage;
    private Button btnSelectImage;
    private TextView tvFullName, tvEmail;
    private UserSessionManager sessionManager;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private String currentImageUrl; // Store existing user image URL
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        // Initialize Cloudinary
        try {
            CloudinaryConfig.init(this);
        } catch (Exception e) {
            Log.e("EditAccountActivity", "Cloudinary init failed: " + e.getMessage());
            Toast.makeText(this, "Error initializing image upload service", Toast.LENGTH_SHORT).show();
        }

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        genderSpinner = findViewById(R.id.gender_spinner);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        ivBack = findViewById(R.id.ivback);
        ivProfileImage = findViewById(R.id.ivAvatar);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);

        // Initialize session manager
        sessionManager = new UserSessionManager(this);

        // Set up image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null && ivProfileImage != null) {
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_background)
                                    .apply(RequestOptions.circleCropTransform()) // Circular transformation
                                    .into(ivProfileImage);
                            uploadImageToCloudinary(null); // Upload immediately after selection
                        } else {
                            Log.e("EditAccountActivity", "Selected image URI is null or ImageView is null");
                            Toast.makeText(this, "Failed to load selected image", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w("EditAccountActivity", "Image selection cancelled or failed");
                    }
                });

        // Set up gender dropdown
        String[] genders = {"MALE", "FEMALE", "OTHER"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, genders);
        genderSpinner.setAdapter(genderAdapter);

        // Button listeners
        ivBack.setOnClickListener(v -> finish());
        btnSelectImage.setOnClickListener(v -> requestStoragePermission());
        btnUpdateProfile.setOnClickListener(v -> updateProfile());

        // Fetch user data
        fetchUserData();
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                selectImage();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                selectImage();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else {
            Toast.makeText(this, "Permission denied to access gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        try {
            imagePickerLauncher.launch(intent);
        } catch (Exception e) {
            Log.e("EditAccountActivity", "Failed to launch image picker: " + e.getMessage());
            Toast.makeText(this, "Unable to open gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToCloudinary(Runnable onComplete) {
        if (selectedImageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            if (onComplete != null) onComplete.run();
            return;
        }

        Map<String, Object> options = new HashMap<>();
        options.put("folder", "user_profiles");
        options.put("resource_type", "image");

        try {
            MediaManager.get().upload(selectedImageUri)
                    .option("folder", "user_profiles")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Toast.makeText(EditAccountActivity.this, "Uploading image...", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // Optional: Show upload progress
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            uploadedImageUrl = (String) resultData.get("secure_url");
                            Log.d("EditAccountActivity", "Image uploaded: " + uploadedImageUrl);
                            Toast.makeText(EditAccountActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                            if (onComplete != null) onComplete.run();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e("EditAccountActivity", "Upload failed: " + error.getDescription());
                            Toast.makeText(EditAccountActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            uploadedImageUrl = null;
                            if (onComplete != null) onComplete.run();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.w("EditAccountActivity", "Upload rescheduled: " + error.getDescription());
                        }
                    })
                    .dispatch();
        } catch (Exception e) {
            Log.e("EditAccountActivity", "Cloudinary upload error: " + e.getMessage());
            Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
            if (onComplete != null) onComplete.run();
        }
    }

    private void fetchUserData() {
        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<User> call = apiService.getUser(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    tvFullName.setText(user.getFullName() != null ? user.getFullName() : "");
                    tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                    etFullName.setText(user.getFullName() != null ? user.getFullName() : "");
                    etPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                    genderSpinner.setText(user.getGender() != null ? user.getGender() : "", false);
                    currentImageUrl = user.getImage(); // Store existing image URL
                    if (user.getImage() != null && !user.getImage().isEmpty() && ivProfileImage != null) {
                        Glide.with(EditAccountActivity.this)
                                .load(user.getImage())
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background)
                                .apply(RequestOptions.circleCropTransform()) // Circular transformation
                                .into(ivProfileImage);
                    }
                    sessionManager.createLoginSession(user);
                } else {
                    Log.e("EditAccountActivity", "Failed to fetch user data: " + response.message());
                    Toast.makeText(EditAccountActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("EditAccountActivity", "Error fetching user data: " + t.getMessage());
                Toast.makeText(EditAccountActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String gender = genderSpinner.getText().toString();

        // Validate inputs
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Full name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, "Phone number is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (gender.isEmpty() || (!gender.equals("MALE") && !gender.equals("FEMALE") && !gender.equals("OTHER"))) {
            Toast.makeText(this, "Please select a valid gender", Toast.LENGTH_SHORT).show();
            return;
        }

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setFullName(fullName);
        userUpdateDTO.setPhone(phone);
        userUpdateDTO.setGender(gender);

        // Handle image upload if selected
        if (selectedImageUri != null && uploadedImageUrl == null) {
            uploadImageToCloudinary(() -> {
                userUpdateDTO.setImage(uploadedImageUrl != null ? uploadedImageUrl : currentImageUrl);
                submitProfileUpdate(userUpdateDTO);
            });
        } else {
            userUpdateDTO.setImage(uploadedImageUrl != null ? uploadedImageUrl : currentImageUrl);
            submitProfileUpdate(userUpdateDTO);
        }
    }

    private void submitProfileUpdate(UserUpdateDTO userUpdateDTO) {
        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<UserUpdateResponse> call = apiService.updateUser(userId, userUpdateDTO);
        call.enqueue(new Callback<UserUpdateResponse>() {
            @Override
            public void onResponse(Call<UserUpdateResponse> call, Response<UserUpdateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserUpdateResponse updateResponse = response.body();
                    if (updateResponse.isSuccess()) {
                        Toast.makeText(EditAccountActivity.this, updateResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        User user = sessionManager.getUserDetails();
                        user.setFullName(userUpdateDTO.getFullName());
                        user.setPhone(userUpdateDTO.getPhone());
                        user.setGender(userUpdateDTO.getGender());
                        if (userUpdateDTO.getImage() != null) {
                            user.setImage(userUpdateDTO.getImage());
                        }
                        sessionManager.createLoginSession(user);
                        finish();
                    } else {
                        Log.e("UpdateUser", "Update failed: " + updateResponse.getMessage());
                        Toast.makeText(EditAccountActivity.this, "Update failed: " + updateResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMessage = response.message();
                    String errorBody = null;
                    try {
                        errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                    } catch (IOException e) {
                        Log.e("UpdateUser", "Failed to read error body: " + e.getMessage());
                    }
                    Log.e("UpdateUser", "Error: HTTP " + response.code() + ", Message: " + errorMessage + ", Body: " + errorBody);
                    Toast.makeText(EditAccountActivity.this, "Failed to update profile: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UserUpdateResponse> call, Throwable t) {
                Log.e("UpdateUser", "Failure: " + t.getMessage());
                Toast.makeText(EditAccountActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}