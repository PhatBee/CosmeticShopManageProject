package vn.phatbee.cosmesticshopapp.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.manager.UserSessionManager;
import vn.phatbee.cosmesticshopapp.model.Address;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class EditAddressActivity extends AppCompatActivity {
    private EditText etReceiverName, etReceiverPhone, etAddress, etProvince, etDistrict, etWard;
    private Switch swDefault;
    private Button btnComplete;
    private ImageView ivBack;
    private ApiService apiService;
    private UserSessionManager sessionManager;
    private Address address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);

        // Khởi tạo các view
        etReceiverName = findViewById(R.id.etReceiverName);
        etReceiverPhone = findViewById(R.id.etReceiverPhone);
        etAddress = findViewById(R.id.etAddress);
        etProvince = findViewById(R.id.etProvince);
        etDistrict = findViewById(R.id.etDistrict);
        etWard = findViewById(R.id.etWard);
        swDefault = findViewById(R.id.swDefault);
        btnComplete = findViewById(R.id.btnComplete);
        ivBack = findViewById(R.id.ivBack);

        sessionManager = new UserSessionManager(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Nhận đối tượng Address từ Intent
        address = (Address) getIntent().getSerializableExtra("address");
        if (address != null) {
            etReceiverName.setText(address.getReceiverName());
            etReceiverPhone.setText(address.getReceiverPhone());
            etAddress.setText(address.getAddress());
            etProvince.setText(address.getProvince());
            etDistrict.setText(address.getDistrict());
            etWard.setText(address.getWard());
            swDefault.setChecked(address.isDefaultAddress());
        } else {
            Toast.makeText(this, "Error: No address data provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Xử lý nút Quay lại
        ivBack.setOnClickListener(v -> finish());

        // Xử lý nút Hoàn Thành
        btnComplete.setOnClickListener(v -> updateAddress());
    }

    private void updateAddress() {
        if (address == null) {
            Toast.makeText(this, "Error: No address to update", Toast.LENGTH_SHORT).show();
            return;
        }

        Long userId = sessionManager.getUserDetails().getUserId();
        if (userId == null || userId == 0) {
            Toast.makeText(this, "Please log in to update address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật thông tin địa chỉ
        address.setReceiverName(etReceiverName.getText().toString().trim());
        address.setReceiverPhone(etReceiverPhone.getText().toString().trim());
        address.setAddress(etAddress.getText().toString().trim());
        address.setProvince(etProvince.getText().toString().trim());
        address.setDistrict(etDistrict.getText().toString().trim());
        address.setWard(etWard.getText().toString().trim());
        address.setDefaultAddress(swDefault.isChecked());

        // Gửi yêu cầu cập nhật tới backend
        Call<Address> call = apiService.updateAddress(userId, address);
        call.enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditAddressActivity.this, "Address updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EditAddressActivity.this, "Failed to update address", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                Toast.makeText(EditAddressActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}