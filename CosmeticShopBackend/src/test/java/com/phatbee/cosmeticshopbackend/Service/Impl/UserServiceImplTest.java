package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Entity.UserOtp;
import com.phatbee.cosmeticshopbackend.dto.*;
import com.phatbee.cosmeticshopbackend.Entity.User;
import com.phatbee.cosmeticshopbackend.Repository.UserOtpRepository;
import com.phatbee.cosmeticshopbackend.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserOtpRepository userOtpRepository;

    @Mock
    private EmailServiceImpl emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Đăng nhập (3.4.1)
    @Test
    void authenticate_withValidCredentials_returnsSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEmail("test@example.com");
        user.setActivated(true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // Act
        LoginResponse response = userService.authenticate(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getUserId());
        assertEquals("testuser", response.getUser().getUsername());
        assertNull(response.getUser().getPassword()); // Password không được trả về
    }

    @Test
    void authenticate_withInvalidUsername_returnsFailure() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act
        LoginResponse response = userService.authenticate(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());
        assertNull(response.getUser());
    }

    @Test
    void authenticate_withWrongPassword_returnsFailure() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setActivated(true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act
        LoginResponse response = userService.authenticate(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid password", response.getMessage());
        assertNull(response.getUser());
    }

    // Đăng ký (3.4.2)
    @Test
    void register_withValidData_sendsOtpAndReturnsSuccess() {
        // Arrange
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setGender("MALE");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // Act
        RegistrationResponse response = userService.register(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("OTP sent to your email for verification", response.getMessage());
        verify(userOtpRepository, times(1)).save(any(UserOtp.class));
        verify(emailService, times(1)).sendOtpEmail(eq("newuser@example.com"), anyString());
    }

    @Test
    void register_withExistingUsername_returnsFailure() {
        // Arrange
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("existinguser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act
        RegistrationResponse response = userService.register(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Username already exists", response.getMessage());
        verify(userOtpRepository, never()).save(any(UserOtp.class));
        verify(emailService, never()).sendOtpEmail(anyString(), anyString());
    }

    @Test
    void register_withExistingEmail_returnsFailure() {
        // Arrange
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act
        RegistrationResponse response = userService.register(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Email already registered", response.getMessage());
        verify(userOtpRepository, never()).save(any(UserOtp.class));
        verify(emailService, never()).sendOtpEmail(anyString(), anyString());
    }

    @Test
    void verifyOtp_withValidOtp_createsUserAndReturnsSuccess() {
        // Arrange
        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setEmail("newuser@example.com");
        request.setOtp("123456");
        UserOtp userOtp = new UserOtp();
        userOtp.setEmail("newuser@example.com");
        userOtp.setOtp("123456");
        userOtp.setUsername("newuser");
        userOtp.setPassword("encodedPassword");
        userOtp.setGender("MALE");
        userOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(userOtpRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(userOtp));

        // Act
        RegistrationResponse response = userService.verifyOtp(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Registration successful. Please login.", response.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userOtpRepository, times(1)).delete(userOtp);
    }

    @Test
    void verifyOtp_withInvalidOtp_returnsFailure() {
        // Arrange
        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setEmail("newuser@example.com");
        request.setOtp("wrongotp");
        UserOtp userOtp = new UserOtp();
        userOtp.setEmail("newuser@example.com");
        userOtp.setOtp("123456");
        userOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(userOtpRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(userOtp));

        // Act
        RegistrationResponse response = userService.verifyOtp(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid OTP", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(userOtpRepository, never()).delete(any(UserOtp.class));
    }

    @Test
    void verifyOtp_withExpiredOtp_returnsFailure() {
        // Arrange
        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setEmail("newuser@example.com");
        request.setOtp("123456");
        UserOtp userOtp = new UserOtp();
        userOtp.setEmail("newuser@example.com");
        userOtp.setOtp("123456");
        userOtp.setExpiryTime(LocalDateTime.now().minusMinutes(5));

        when(userOtpRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(userOtp));

        // Act
        RegistrationResponse response = userService.verifyOtp(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("OTP has expired. Please register again.", response.getMessage());
        verify(userOtpRepository, times(1)).delete(userOtp);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resendOtpRegistration_withValidEmail_sendsNewOtp() {
        // Arrange
        String email = "newuser@example.com";
        UserOtp userOtp = new UserOtp();
        userOtp.setEmail(email);
        userOtp.setOtp("oldotp");
        userOtp.setExpiryTime(LocalDateTime.now());

        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.of(userOtp));

        // Act
        RegistrationResponse response = userService.resendOtpRegistration(email);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("New OTP sent to your email", response.getMessage());
        verify(userOtpRepository, times(1)).save(any(UserOtp.class));
        verify(emailService, times(1)).sendOtpEmail(eq(email), anyString());
    }

    @Test
    void resendOtpRegistration_withInvalidEmail_returnsFailure() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        RegistrationResponse response = userService.resendOtpRegistration(email);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("No registration in progress for this email", response.getMessage());
        verify(userOtpRepository, never()).save(any(UserOtp.class));
        verify(emailService, never()).sendOtpEmail(anyString(), anyString());
    }

    // Quên mật khẩu (3.4.3)
    @Test
    void requestPasswordReset_withValidEmail_sendsOtp() {
        // Arrange
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        user.setActivated(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        PasswordResetResponse response = userService.requestPasswordReset(email);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Password reset code sent to your email", response.getMessage());
        verify(userOtpRepository, times(1)).save(any(UserOtp.class));
        verify(emailService, times(1)).sendPasswordResetOtp(eq(email), anyString());
    }

    @Test
    void requestPasswordReset_withInvalidEmail_returnsGenericSuccess() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        PasswordResetResponse response = userService.requestPasswordReset(email);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("If your email is registered, you will receive a password reset code", response.getMessage());
        verify(userOtpRepository, never()).save(any(UserOtp.class));
        verify(emailService, never()).sendPasswordResetOtp(anyString(), anyString());
    }

    @Test
    void resetPassword_withValidOtp_updatesPassword() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@example.com");
        request.setOtp("123456");
        request.setNewPassword("newpassword123");
        User user = new User();
        user.setEmail("user@example.com");
        UserOtp userOtp = new UserOtp();
        userOtp.setEmail("user@example.com");
        userOtp.setOtp("123456");
        userOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(userOtpRepository.findByEmail("user@example.com")).thenReturn(Optional.of(userOtp));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword123")).thenReturn("encodedNewPassword");

        // Act
        PasswordResetResponse response = userService.resetPassword(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Password has been reset successfully", response.getMessage());
        verify(userRepository, times(1)).save(user);
        verify(userOtpRepository, times(1)).delete(userOtp);
        assertEquals("encodedNewPassword", user.getPassword());
    }

    @Test
    void resetPassword_withInvalidOtp_returnsFailure() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("user@example.com");
        request.setOtp("wrongotp");
        request.setNewPassword("newpassword123");
        UserOtp userOtp = new UserOtp();
        userOtp.setEmail("user@example.com");
        userOtp.setOtp("123456");
        userOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(userOtpRepository.findByEmail("user@example.com")).thenReturn(Optional.of(userOtp));

        // Act
        PasswordResetResponse response = userService.resetPassword(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid reset code", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(userOtpRepository, never()).delete(any(UserOtp.class));
    }

    @Test
    void resetPassword_withShortPassword_returnsFailure() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("vinhphatst1235@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("phatb");
        UserOtp userOtp = new UserOtp();
        userOtp.setEmail("vinhphatst1235@gmail.com");
        userOtp.setOtp("123456");
        userOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        User user = new User(); // Thêm user để mock
        user.setEmail("vinhphatst1235@gmail.com");

        when(userOtpRepository.findByEmail("vinhphatst1235@gmail.com")).thenReturn(Optional.of(userOtp));
        when(userRepository.findByEmail("vinhphatst1235@gmail.com")).thenReturn(Optional.of(user));

        // Act
        PasswordResetResponse response = userService.resetPassword(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Password must be at least 6 characters", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(userOtpRepository, never()).delete(any(UserOtp.class));
    }

    @Test
    void resendOtpPasswordReset_withValidEmail_sendsNewOtp() {
        // Arrange
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        UserOtp userOtp = new UserOtp();
        userOtp.setEmail(email);
        userOtp.setOtp("oldotp");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.of(userOtp));

        // Act
        PasswordResetResponse response = userService.resendOtpPasswordReset(email);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("New password reset code sent to your email", response.getMessage());
        verify(userOtpRepository, times(1)).save(any(UserOtp.class));
        verify(emailService, times(1)).sendPasswordResetOtp(eq(email), anyString());
    }

    @Test
    void resendOtpPasswordReset_withNoExistingRequest_returnsFailure() {
        // Arrange
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userOtpRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        PasswordResetResponse response = userService.resendOtpPasswordReset(email);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("No password reset request found for this email", response.getMessage());
        verify(userOtpRepository, never()).save(any(UserOtp.class));
        verify(emailService, never()).sendPasswordResetOtp(anyString(), anyString());
    }

    // Đăng xuất (3.4.4) - Giả định
//    @Test
//    void logout_withValidUserId_returnsSuccess() {
//        // Arrange
//        Long userId = 1L;
//        User user = new User();
//        user.setUserId(userId);
//
//        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
//
//        // Act
//        String response = userService.logout(userId);
//
//        // Assert
//        assertEquals("Logout successful", response);
//    }
//
//    @Test
//    void logout_withInvalidUserId_returnsFailure() {
//        // Arrange
//        Long userId = 999L;
//        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());
//
//        // Act
//        String response = userService.logout(userId);
//
//        // Assert
//        assertEquals("User not found", response);
//    }
}