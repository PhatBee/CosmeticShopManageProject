package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Config.OTPGenerator;
import com.phatbee.cosmeticshopbackend.Entity.User;
import com.phatbee.cosmeticshopbackend.Entity.UserOtp;
import com.phatbee.cosmeticshopbackend.Repository.UserOtpRepository;
import com.phatbee.cosmeticshopbackend.Repository.UserRepository;
import com.phatbee.cosmeticshopbackend.Service.UserService;
import com.phatbee.cosmeticshopbackend.dto.*;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private UserOtpRepository otpRepository;

    private static final int MAX_ATTEMPTS = 3;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Override
    public boolean authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(password, user .getPassword()) && user.isActivated())
                .orElse(false);
    }

    @Override
    public String registerUser(String username, String email, String password, String fullName, Date birthday, String gender, String phone, String imageUrl) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already registered");
        }
        String otp = OTPGenerator.generateOTP();
        emailService.sendOtp(email, otp);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setGender(gender);
        user.setPhone(phone);
        user.setImage(imageUrl);
        user.setOtp(otp);
        user.setOtpGeneratedAt(LocalDateTime.now());
        userRepository.save(user);

        return "Registration successful. Please check your email for the OTP.";
    }

    @Override
    public String activateAccount(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid Exception, No user found"));

        if (user.isActivated()){
            throw new RuntimeException("User is already activated");
        }

        if (!otp.equals(user.getOtp())) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            userRepository.save(user);

            if (user.getFailedAttempts() >= MAX_ATTEMPTS) {
                userRepository.delete(user);
                throw new RuntimeException("Too many attempts, Account registration has been canceled");
            }
            throw new RuntimeException("Invalid OTP. Please try again");
        }

        user.setActivated(true);
        user.setOtp(null);
        user.setFailedAttempts(0);
        userRepository.save(user);
        return "Activated successful";
    }

    @Override
    public String resendOtp1(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isActivated()) {
            throw new RuntimeException("Account is already activated");
        }

        LocalDateTime now = LocalDateTime.now();
        if (user.getOtpGeneratedAt() != null && user.getOtpGeneratedAt().plusSeconds(30).isAfter(now)) {
            throw new RuntimeException("Please wait 30 seconds before requesting a new OTP");
        }

        String newOtp = OTPGenerator.generateOTP();
        user.setOtp(newOtp);
        user.setOtpGeneratedAt(now);
        userRepository.save(user);

        emailService.sendOtp(user.getEmail(), newOtp);
        return "A new OTP has been sent to your email.";
    }

    @Override
    public String sendOtpForPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isActivated()) {
            throw new RuntimeException("User is not activated, Please contact administrator");
        }

        String otp = OTPGenerator.generateOTP();
        emailService.sendOtp(email, otp);
        user.setOtp(otp);
        user.setOtpGeneratedAt(LocalDateTime.now());
        userRepository.save(user);
        return "A new OTP has been sent to your email.";

    }

    @Override
    public String resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid Exception, No user found"));

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP. Please try again");
        }

        LocalDateTime now = LocalDateTime.now();
        if (user.getOtpGeneratedAt() != null && user.getOtpGeneratedAt().plusSeconds(30).isAfter(now)) {
            throw new RuntimeException("Please wait 30 seconds before requesting a new OTP");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtp(null);
        userRepository.save(user);

        return "Password reset successful";
    }

    @Override
    public LoginResponse authenticate(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (!userOptional.isPresent()) {
            return new LoginResponse(false, "User not found", null);
        }

        User user = userOptional.get();

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // Create a copy of user without the password for security
            User userResponse = new User();
            userResponse.setUserId(user.getUserId());
            userResponse.setUsername(user.getUsername());
            userResponse.setEmail(user.getEmail());
            userResponse.setGender(user.getGender());
            userResponse.setImage(user.getImage());
            // Don't set the password!

            return new LoginResponse(true, "Login successful", userResponse);
        } else {
            return new LoginResponse(false, "Invalid password", null);
        }
    }

    @Override
    public RegistrationResponse register(RegistrationRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return new RegistrationResponse(false, "Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return new RegistrationResponse(false, "Email already registered");
        }

        // Generate OTP
        String otp = generateOtp();

        // Save OTP and user details temporarily
        UserOtp userOtp = otpRepository.findByEmail(request.getEmail()).orElse(new UserOtp());
        userOtp.setEmail(request.getEmail());
        userOtp.setOtp(otp);
        userOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10)); // OTP valid for 10 minutes
        userOtp.setUsername(request.getUsername());
        userOtp.setPassword(passwordEncoder.encode(request.getPassword())); // Encrypt password
        userOtp.setGender(request.getGender());
        otpRepository.save(userOtp);

        // Send OTP via email
        emailService.sendOtpEmail(request.getEmail(), otp);

        return new RegistrationResponse(true, "OTP sent to your email for verification");

    }

    @Override
    public RegistrationResponse verifyOtp(OtpVerificationRequest request) {
        Optional<UserOtp> userOtpOptional = otpRepository.findByEmail(request.getEmail());

        if (!userOtpOptional.isPresent()) {
            return new RegistrationResponse(false, "Invalid request or OTP expired");
        }

        UserOtp userOtp = userOtpOptional.get();

        // Check if OTP is expired
        if (LocalDateTime.now().isAfter(userOtp.getExpiryTime())) {
            otpRepository.delete(userOtp);
            return new RegistrationResponse(false, "OTP has expired. Please register again.");
        }

        // Verify OTP
        if (!userOtp.getOtp().equals(request.getOtp())) {
            return new RegistrationResponse(false, "Invalid OTP");
        }

        // Create new user
        User user = new User();
        user.setUsername(userOtp.getUsername());
        user.setPassword(userOtp.getPassword()); // Already encrypted
        user.setEmail(userOtp.getEmail());
        user.setGender(userOtp.getGender());

        // Save user
        userRepository.save(user);

        // Delete OTP entry
        otpRepository.delete(userOtp);

        return new RegistrationResponse(true, "Registration successful. Please login.");

    }

    private String generateOtp() {
        // Generate 6-digit OTP
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    public RegistrationResponse resendOtpRegistration(String email) {
        Optional<UserOtp> userOtpOptional = otpRepository.findByEmail(email);

        if (!userOtpOptional.isPresent()) {
            return new RegistrationResponse(false, "No registration in progress for this email");
        }

        UserOtp userOtp = userOtpOptional.get();
        String newOtp = generateOtp();
        userOtp.setOtp(newOtp);
        userOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        otpRepository.save(userOtp);

        // Send new OTP via email
        emailService.sendOtpEmail(email, newOtp);

        return new RegistrationResponse(true, "New OTP sent to your email");
    }

    @Override
    public PasswordResetResponse requestPasswordReset(String email) {
        // Check if user with email exists
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            // For security reasons, don't reveal that email doesn't exist
            return new PasswordResetResponse(true, "If your email is registered, you will receive a password reset code");
        }

        // Generate OTP
        String otp = generateOtp();

        // Save OTP
        UserOtp passwordResetOtp = otpRepository.findByEmail(email).orElse(new UserOtp());
        passwordResetOtp.setEmail(email);
        passwordResetOtp.setOtp(otp);
        passwordResetOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10)); // OTP valid for 10 minutes
        otpRepository.save(passwordResetOtp);

        // Send OTP via email
        emailService.sendPasswordResetOtp(email, otp);

        return new PasswordResetResponse(true, "Password reset code sent to your email");
    }

    @Override
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getOtp() == null || request.getNewPassword() == null) {
            return new PasswordResetResponse(false, "Invalid request");
        }

        // Check if OTP exists
        Optional<UserOtp> otpOptional = otpRepository.findByEmail(request.getEmail());
        if (!otpOptional.isPresent()) {
            return new PasswordResetResponse(false, "Invalid or expired reset code");
        }

        UserOtp passwordResetOtp = otpOptional.get();

        // Check if OTP is expired
        if (LocalDateTime.now().isAfter(passwordResetOtp.getExpiryTime())) {
            otpRepository.delete(passwordResetOtp);
            return new PasswordResetResponse(false, "Reset code has expired. Please request a new one.");
        }

        // Verify OTP
        if (!passwordResetOtp.getOtp().equals(request.getOtp())) {
            return new PasswordResetResponse(false, "Invalid reset code");
        }

        // Find user
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (!userOptional.isPresent()) {
            return new PasswordResetResponse(false, "User not found");
        }

        User user = userOptional.get();

        // Validate new password
        if (request.getNewPassword().length() < 6) {
            return new PasswordResetResponse(false, "Password must be at least 6 characters");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete OTP entry
        otpRepository.delete(passwordResetOtp);

        return new PasswordResetResponse(true, "Password has been reset successfully");
    }

    @Override
    public PasswordResetResponse resendOtpPasswordReset(String email) {
        // Check if user with email exists
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            // For security reasons, don't reveal that email doesn't exist
            return new PasswordResetResponse(true, "If your email is registered, you will receive a password reset code");
        }

        // Check if existing OTP request exists
        Optional<UserOtp> otpOptional = otpRepository.findByEmail(email);
        if (!otpOptional.isPresent()) {
            return new PasswordResetResponse(false, "No password reset request found for this email");
        }

        // Generate new OTP
        String newOtp = generateOtp();

        // Update OTP
        UserOtp passwordResetOtp = otpOptional.get();
        passwordResetOtp.setOtp(newOtp);
        passwordResetOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        otpRepository.save(passwordResetOtp);

        // Send new OTP via email
        emailService.sendPasswordResetOtp(email, newOtp);

        return new PasswordResetResponse(true, "New password reset code sent to your email");

    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserUpdateResponse updateUser(Long userId, UserUpdateDTO userUpdateDTO) {
        // Validate input fields
        if (userUpdateDTO.getFullName() == null || userUpdateDTO.getFullName().trim().isEmpty()) {
            throw new ValidationException("Full name is required");
        }
        if (userUpdateDTO.getPhone() == null || userUpdateDTO.getPhone().trim().isEmpty()) {
            throw new ValidationException("Phone number is required");
        }
        if (userUpdateDTO.getGender() == null || userUpdateDTO.getGender().trim().isEmpty()) {
            throw new ValidationException("Gender is required");
        }

        // Find and update the user
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ValidationException("User not found"));
        user.setFullName(userUpdateDTO.getFullName());
        user.setPhone(userUpdateDTO.getPhone());
        if (!isValidGender(userUpdateDTO.getGender())) {
            throw new ValidationException("Invalid gender value: " + userUpdateDTO.getGender());
        }
        user.setGender(userUpdateDTO.getGender());
        user.setImage(userUpdateDTO.getImage());

        // Save the updated user
        userRepository.save(user);

        // Return a response
        return new UserUpdateResponse(true, "User profile updated successfully");
    }

    private boolean isValidGender(String gender) {
        return gender != null && (gender.equals("MALE") || gender.equals("FEMALE") || gender.equals("OTHER"));
    }
}
