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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.model.ForgotPasswordRequest;
import vn.phatbee.cosmesticshopapp.model.PasswordResetResponse;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText editTextEmail;
    private Button buttonResetPassword;
    private ProgressBar progressBar;
    private TextView textViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        progressBar = findViewById(R.id.progressBarBanner);
        textViewLogin = findViewById(R.id.textViewLogin);

        // Set click listener for reset button
        buttonResetPassword.setOnClickListener(v -> {
            // Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            // Validate input
            if (validateInput()) {
                requestPasswordReset();
            }
        });

        // Set click listener for login text
        textViewLogin.setOnClickListener(v -> {
            // Navigate back to login activity
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private boolean validateInput() {
        String email = editTextEmail.getText().toString().trim();

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

        return true;
    }

    private void requestPasswordReset() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        buttonResetPassword.setEnabled(false);

        // Get email
        String email = editTextEmail.getText().toString().trim();

        // Create request
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        // Make API call
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PasswordResetResponse> call = apiService.requestPasswordReset(request);
        call.enqueue(new Callback<PasswordResetResponse>() {
            @Override
            public void onResponse(Call<PasswordResetResponse> call, Response<PasswordResetResponse> response) {
                progressBar.setVisibility(View.GONE);
                buttonResetPassword.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    PasswordResetResponse resetResponse = response.body();

                    if (resetResponse.isSuccess()) {
                        // Show success message
                        Toast.makeText(ForgotPasswordActivity.this, resetResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Navigate to reset password activity
                        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    } else {
                        // Show error message
                        Toast.makeText(ForgotPasswordActivity.this, resetResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errorObject = new JSONObject(response.errorBody().string());
                            Toast.makeText(ForgotPasswordActivity.this, errorObject.getString("message"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Request failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(ForgotPasswordActivity.this, "Request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<PasswordResetResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                buttonResetPassword.setEnabled(true);
                Toast.makeText(ForgotPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}