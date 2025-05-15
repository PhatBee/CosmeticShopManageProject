package vn.phatbee.cosmesticshopapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.model.RegistrationRequest;
import vn.phatbee.cosmesticshopapp.model.RegistrationResponse;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextEmail, editTextPassword, editTextConfirmPassword;
    private RadioGroup radioGroupGender;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private TextView textViewLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressBarBanner);
        textViewLogin = findViewById(R.id.textViewLogin);

        // Set click listener for register button
        buttonRegister.setOnClickListener(v -> {
            // Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            // Validate input
            if (validateInput()) {
                register();
            }
        });

        // Set click listener for login text
        textViewLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInput() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validate username
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Username is required");
            editTextUsername.requestFocus();
            return false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return false;
        }

        // Validate confirm password
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            return false;
        }

        // Validate gender selection
        if (radioGroupGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void register(){
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        buttonRegister.setEnabled(false);

        // Get input values
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Get selected gender
        String gender;
        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedId == R.id.radioButtonMale) {
            gender = "Male";
        } else {
            gender = "Female";
        }

        // Create registration request
        RegistrationRequest request = new RegistrationRequest(username, password, email, gender);

        // Make API call
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<RegistrationResponse> call = apiService.register(request);
        call.enqueue(new Callback<RegistrationResponse>() {
            @Override
            public void onResponse(Call<RegistrationResponse> call, Response<RegistrationResponse> response) {
                progressBar.setVisibility(View.GONE);
                buttonRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    RegistrationResponse registrationResponse = response.body();

                    if (registrationResponse.isSuccess()) {
                        // Show success message
                        Toast.makeText(RegisterActivity.this, registrationResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Navigate to OTP verification activity
                        Intent intent = new Intent(RegisterActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    } else {
                        // Show error message
                        Toast.makeText(RegisterActivity.this, registrationResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errorObject = new JSONObject(response.errorBody().string());
                            Toast.makeText(RegisterActivity.this, errorObject.getString("message"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<RegistrationResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                buttonRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}