package vn.phatbee.cosmesticshopapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.adapter.AddressAdapter;
import vn.phatbee.cosmesticshopapp.manager.UserSessionManager;
import vn.phatbee.cosmesticshopapp.model.Address;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class AddressListActivity extends AppCompatActivity {

    private RecyclerView rvAddresses;
    private ImageView ivBack;
    private Button btnAddNewAddress;
    private UserSessionManager sessionManager;
    private ApiService apiService;
    private AddressAdapter addressAdapter;
    private ActivityResultLauncher<Intent> editAddressLauncher;
    private ActivityResultLauncher<Intent> addAddressLauncher;
    private boolean isDataLoaded = false;
    private boolean isSelectMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        // Kiểm tra chế độ chọn địa chỉ từ Intent
        isSelectMode = getIntent().getBooleanExtra("selectMode", false);

        // Khởi tạo các view
        rvAddresses = findViewById(R.id.rvAddresses);
        ivBack = findViewById(R.id.ivBack);
        btnAddNewAddress = findViewById(R.id.btnAddAddress);

        sessionManager = new UserSessionManager(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Đăng ký ActivityResultLauncher trước khi tạo AddressAdapter
        editAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadAddresses();
                    }
                });

        addAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadAddresses();
                    }
                });

        // Thiết lập RecyclerView
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        addressAdapter = new AddressAdapter(this, new ArrayList<>(), editAddressLauncher);

        if (isSelectMode) {
            addressAdapter.setOnAddressSelectedListener(address -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedAddress", address);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }

        rvAddresses.setAdapter(addressAdapter);

        // Xử lý sự kiện nút Back
        ivBack.setOnClickListener(v -> finish());

        // Xử lý sự kiện nút Thêm địa chỉ
        btnAddNewAddress.setOnClickListener(v -> {
            Intent intent = new Intent(AddressListActivity.this, AddAddressActivity.class);
            addAddressLauncher.launch(intent);
        });

        // Tải danh sách địa chỉ
        loadAddresses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Chỉ tải lại nếu dữ liệu chưa được tải hoặc cần làm mới
        if (!isDataLoaded) {
            loadAddresses();
        }
    }

    private void loadAddresses() {
        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem danh sách địa chỉ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvAddresses.setVisibility(View.GONE);

        Call<List<Address>> call = apiService.getAddressesByUserId(userId);
        call.enqueue(new Callback<List<Address>>() {
            @Override
            public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                rvAddresses.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Address> addresses = response.body();
                    addressAdapter.updateAddresses(addresses);
                    isDataLoaded = true;
                } else {
                    String errorMessage = "Không thể tải danh sách địa chỉ";
                    if (response.code() == 404) {
                        errorMessage = "Không tìm thấy địa chỉ nào";
                    } else if (response.code() == 400) {
                        errorMessage = "Yêu cầu không hợp lệ";
                    }
                    Toast.makeText(AddressListActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {
                rvAddresses.setVisibility(View.VISIBLE);
                Toast.makeText(AddressListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}