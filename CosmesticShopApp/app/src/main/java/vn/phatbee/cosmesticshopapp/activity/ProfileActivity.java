package vn.phatbee.cosmesticshopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.manager.UserSessionManager;
import vn.phatbee.cosmesticshopapp.model.User;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvMyAccount;
    private ImageView ivMyAccount, editAccount, ivMyOrder, profile_ímage;
    private ImageView btnLogout, btnBack;
    private ImageView ivAddress;
    private TextView tvAddress, tvName, tvUsername, tvMyOrder;
    private UserSessionManager sessionManager;

    //Bottom nav
    private ImageView ivGioHang, ivDonHang, ivWishList,ivHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        anhXa();

        tvMyAccount = findViewById(R.id.tvMyAccount);
        ivMyAccount = findViewById(R.id.ivAccount);

        // Initialize UserSessionManager
        sessionManager = new UserSessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please log in to view your orders", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvMyAccount.setOnClickListener(v ->{
            Intent intent = new Intent(ProfileActivity.this, EditAccountActivity.class);
            startActivity(intent);
        });

        ivMyAccount.setOnClickListener(v ->{
            Intent intent = new Intent(ProfileActivity.this, EditAccountActivity.class);
            startActivity(intent);
        });


        ivAddress = findViewById(R.id.ivAddress);
        ivAddress.setOnClickListener(v ->{
            Intent intent = new Intent(ProfileActivity.this, AddressListActivity.class);
            startActivity(intent);
        });

        tvAddress = findViewById(R.id.tvAddress);
        tvAddress.setOnClickListener(v ->{
            Intent intent = new Intent(ProfileActivity.this, AddressListActivity.class);
            startActivity(intent);
        });

        editAccount = findViewById(R.id.ivEdit);
        editAccount.setOnClickListener(v ->{
            Intent intent = new Intent(ProfileActivity.this, EditAccountActivity.class);
            startActivity(intent);
        });

        ivMyOrder = findViewById(R.id.ivMyOrder);
        ivMyOrder.setOnClickListener(v ->{
            Intent intent = new Intent(ProfileActivity.this, OrderListActivity.class);
            startActivity(intent);
        });

        //Bottom navigation
        ivGioHang = findViewById(R.id.ivGioHang);
        ivWishList = findViewById(R.id.ivYeuThich);
        ivDonHang = findViewById(R.id.ivDonHang);
        ivHome  = findViewById(R.id.ivHome);

        // Set up click listeners
        ivGioHang.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, CartActivity.class);
            startActivity(intent);
        });

        ivWishList.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, WishlistActivity.class);
            startActivity(intent);
        });

        ivHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ivDonHang.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OrderListActivity.class);
            startActivity(intent);
        });

        btnBack = findViewById(R.id.ivBack);
        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(view -> {
            new AlertDialog.Builder(ProfileActivity.this)
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        sessionManager.logoutUser();
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

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
                    // Update UI
                    tvName.setText(user.getFullName() != null ? user.getFullName() : "");
                    tvUsername.setText(user.getUsername() != null ? user.getUsername() : "");
                    Glide.with(ProfileActivity.this).load(user.getImage()).placeholder(R.drawable.ic_account).apply(RequestOptions.circleCropTransform()).into(profile_ímage);

                    // Update session
                    sessionManager.createLoginSession(user);
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to fetch user data: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void anhXa() {
        btnLogout = findViewById(R.id.ivLogout);
        tvName = findViewById(R.id.tv_name);
        tvUsername = findViewById(R.id.tv_username);
        profile_ímage = findViewById(R.id.profile_image);
    }

}