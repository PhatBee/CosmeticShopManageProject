package vn.phatbee.cosmesticshopapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.phatbee.cosmesticshopapp.R;
import vn.phatbee.cosmesticshopapp.model.PasswordResetResponse;
import vn.phatbee.cosmesticshopapp.model.ResetPasswordRequest;
import vn.phatbee.cosmesticshopapp.retrofit.ApiService;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText editTextOtp, editTextNewPassword, editTextConfirmPassword;
    private Button buttonSubmit;
    private ProgressBar progressBar;
    private TextView textViewResendOtp;
    private TextView textViewTimer;

    private String email;
    private CountDownTimer countDownTimer;
    private boolean canResendOtp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Get email from intent
        email = getIntent().getStringExtra("email");
        if (email == null) {
            Toast.makeText(this, "Invalid request", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        editTextOtp = findViewById(R.id.editTextOtp);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        progressBar = findViewById(R.id.progressBarBanner);
        textViewResendOtp = findViewById(R.id.textViewResendOtp);
        textViewTimer = findViewById(R.id.textViewTimer);

        // Start countdown timer
        startCountDownTimer();

        // Set click listener for submit button
        buttonSubmit.setOnClickListener(v -> {
            // Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            // Validate input
            if (validateInput()) {
                resetPassword();
            }
        });

        // Set click listener for resend OTP text
        textViewResendOtp.setOnClickListener(v -> {
            if (canResendOtp) {
                resendOtp();
            }
        });
    }

    private void startCountDownTimer() {
        canResendOtp = false;
        textViewResendOtp.setTextColor(ContextCompat.getColor(this, R.color.gray));

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewTimer.setText("Resend in: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                textViewTimer.setText("You can resend code now");
                textViewResendOtp.setTextColor(ContextCompat.getColor(ResetPasswordActivity.this, R.color.purple_500));
                canResendOtp = true;
            }
        }.start();
    }

    private boolean validateInput() {
        String otp = editTextOtp.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validate OTP
        if (TextUtils.isEmpty(otp)) {
            editTextOtp.setError("Verification code is required");
            editTextOtp.requestFocus();
            return false;
        }

        if (otp.length() != 6) {
            editTextOtp.setError("Verification code must be 6 digits");
            editTextOtp.requestFocus();
            return false;
        }

        // Validate new password
        if (TextUtils.isEmpty(newPassword)) {
            editTextNewPassword.setError("New password is required");
            editTextNewPassword.requestFocus();
            return false;
        }

        if (newPassword.length() < 6) {
            editTextNewPassword.setError("Password must be at least 6 characters");
            editTextNewPassword.requestFocus();
            return false;
        }

        // Validate confirm password
        if (!newPassword.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void resetPassword() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        buttonSubmit.setEnabled(false);

        // Get input values
        String otp = editTextOtp.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();

        // Create request
        ResetPasswordRequest request = new ResetPasswordRequest(email, otp, newPassword);

        // Make API call
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PasswordResetResponse> call = apiService.resetPassword(request);
        call.enqueue(new Callback<PasswordResetResponse>() {
            @Override
            public void onResponse(Call<PasswordResetResponse> call, Response<PasswordResetResponse> response) {
                progressBar.setVisibility(View.GONE);
                buttonSubmit.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    PasswordResetResponse resetResponse = response.body();

                    if (resetResponse.isSuccess()) {
                        // Show success message
                        Toast.makeText(ResetPasswordActivity.this, resetResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Cancel timer if running
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }

                        // Navigate to login activity (clear back stack)
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Show error message
                        Toast.makeText(ResetPasswordActivity.this, resetResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errorObject = new JSONObject(response.errorBody().string());
                            Toast.makeText(ResetPasswordActivity.this, errorObject.getString("message"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Reset failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(ResetPasswordActivity.this, "Reset failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<PasswordResetResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                buttonSubmit.setEnabled(true);
                Toast.makeText(ResetPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendOtp() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        textViewResendOtp.setEnabled(false);

        // Make API call
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PasswordResetResponse> call = apiService.resendPasswordResetOtp(email);
        call.enqueue(new Callback<PasswordResetResponse>() {
            @Override
            public void onResponse(Call<PasswordResetResponse> call, Response<PasswordResetResponse> response) {
                progressBar.setVisibility(View.GONE);
                textViewResendOtp.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    PasswordResetResponse resetResponse = response.body();

                    // Show success message
                    Toast.makeText(ResetPasswordActivity.this, resetResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    // Reset countdown timer
                    startCountDownTimer();
                } else {
                    // Handle error response
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errorObject = new JSONObject(response.errorBody().string());
                            Toast.makeText(ResetPasswordActivity.this, errorObject.getString("message"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Failed to resend code", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(ResetPasswordActivity.this, "Failed to resend code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<PasswordResetResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                textViewResendOtp.setEnabled(true);
                Toast.makeText(ResetPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}