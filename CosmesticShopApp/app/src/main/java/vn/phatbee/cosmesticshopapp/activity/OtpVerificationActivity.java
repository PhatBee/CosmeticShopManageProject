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
import vn.phatbee.cosmesticshopapp.model.OtpVerificationRequest;
import vn.phatbee.cosmesticshopapp.model.RegistrationResponse;
import vn.phatbee.cosmesticshopapp.retrofit.RetrofitClient;

public class OtpVerificationActivity extends AppCompatActivity {
    private EditText editTextOtp;
    private Button buttonVerifyOtp;
    private ProgressBar progressBar;
    private TextView textViewResendOtp;
    private TextView textViewTimer;

    private String email;
    private CountDownTimer countDownTimer;
    private boolean canResendOtp = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Get email from intent
        email = getIntent().getStringExtra("email");
        if (email == null) {
            Toast.makeText(this, "Invalid request", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        editTextOtp = findViewById(R.id.editTextOtp);
        buttonVerifyOtp = findViewById(R.id.buttonVerifyOtp);
        progressBar = findViewById(R.id.progressBarBanner);
        textViewResendOtp = findViewById(R.id.textViewResendOtp);
        textViewTimer = findViewById(R.id.textViewTimer);

        // Start countdown timer
        startCountDownTimer();

        // Set click listener for verify button
        buttonVerifyOtp.setOnClickListener(v -> {
            // Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            // Validate input
            String otp = editTextOtp.getText().toString().trim();

            if (TextUtils.isEmpty(otp)) {
                editTextOtp.setError("OTP is required");
                editTextOtp.requestFocus();
                return;
            }

            if (otp.length() != 6) {
                editTextOtp.setError("OTP must be 6 digits");
                editTextOtp.requestFocus();
                return;
            }

            // Verify OTP
            verifyOtp(otp);
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
                textViewTimer.setText("You can resend OTP now");
                    textViewResendOtp.setTextColor(ContextCompat.getColor(OtpVerificationActivity.this, R.color.purple_500));
                canResendOtp = true;
            }
        }.start();
    }

    private void verifyOtp(String otp) {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        buttonVerifyOtp.setEnabled(false);

        // Create OTP verification request
        OtpVerificationRequest request = new OtpVerificationRequest(email, otp);

        // Make API call
        Call<RegistrationResponse> call = RetrofitClient.getInstance().getApiService().verifyOtp(request);
        call.enqueue(new Callback<RegistrationResponse>() {
            @Override
            public void onResponse(Call<RegistrationResponse> call, Response<RegistrationResponse> response) {
                progressBar.setVisibility(View.GONE);
                buttonVerifyOtp.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    RegistrationResponse registrationResponse = response.body();

                    if (registrationResponse.isSuccess()) {
                        // Show success message
                        Toast.makeText(OtpVerificationActivity.this, registrationResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Cancel timer if running
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }

                        // Navigate to login activity (clear back stack)
                        Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Show error message
                        Toast.makeText(OtpVerificationActivity.this, registrationResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errorObject = new JSONObject(response.errorBody().string());
                            Toast.makeText(OtpVerificationActivity.this, errorObject.getString("message"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OtpVerificationActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(OtpVerificationActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegistrationResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                buttonVerifyOtp.setEnabled(true);
                Toast.makeText(OtpVerificationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendOtp() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        textViewResendOtp.setEnabled(false);

        // Make API call
        Call<RegistrationResponse> call = RetrofitClient.getInstance().getApiService().resendOtp(email);
        call.enqueue(new Callback<RegistrationResponse>() {
            @Override
            public void onResponse(Call<RegistrationResponse> call, Response<RegistrationResponse> response) {
                progressBar.setVisibility(View.GONE);
                textViewResendOtp.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    RegistrationResponse registrationResponse = response.body();

                    if (registrationResponse.isSuccess()) {
                        // Show success message
                        Toast.makeText(OtpVerificationActivity.this, registrationResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Reset timer
                        startCountDownTimer();
                    } else {
                        // Show error message
                        Toast.makeText(OtpVerificationActivity.this, registrationResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errorObject = new JSONObject(response.errorBody().string());
                            Toast.makeText(OtpVerificationActivity.this, errorObject.getString("message"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OtpVerificationActivity.this, "Failed to resend OTP", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(OtpVerificationActivity.this, "Failed to resend OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegistrationResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                textViewResendOtp.setEnabled(true);
                Toast.makeText(OtpVerificationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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