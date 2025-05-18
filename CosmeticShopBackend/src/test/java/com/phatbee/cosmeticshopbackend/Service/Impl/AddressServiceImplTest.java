package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Entity.Address;
import com.phatbee.cosmeticshopbackend.Entity.User;
import com.phatbee.cosmeticshopbackend.Repository.AddressRepository;
import com.phatbee.cosmeticshopbackend.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User user;
    private Address address;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setUserId(1L);

        address = new Address();
        address.setAddressId(1L);
        address.setReceiverName("John Doe");
        address.setReceiverPhone("1234567890");
        address.setAddress("123 Main St");
        address.setProvince("Province");
        address.setDistrict("District");
        address.setWard("Ward");
        address.setDefaultAddress(false);
        address.setCustomer(user);
    }

    @Test
    void getAddressesByUserId_withAddresses_returnsList() {
        // Arrange
        List<Address> addresses = Arrays.asList(address);
        when(addressRepository.findByCustomerUserId(1L)).thenReturn(addresses);

        // Act
        List<Address> result = addressService.getAddressesByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getReceiverName());
        verify(addressRepository, times(1)).findByCustomerUserId(1L);
    }

    @Test
    void getAddressesByUserId_withNoAddresses_returnsEmptyList() {
        // Arrange
        when(addressRepository.findByCustomerUserId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<Address> result = addressService.getAddressesByUserId(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(addressRepository, times(1)).findByCustomerUserId(1L);
    }

    @Test
    void addAddress_firstAddress_setsDefault() {
        // Arrange
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByCustomerUserId(1L)).thenReturn(Collections.emptyList());
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        // Act
        Address result = addressService.addAddress(1L, address);

        // Assert
        assertNotNull(result);
        assertTrue(result.isDefaultAddress());
        assertEquals(user, result.getCustomer());
        verify(userRepository, times(1)).findByUserId(1L);
        verify(addressRepository, times(1)).findByCustomerUserId(1L);
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void addAddress_nonFirstAddressWithDefault_clearsOtherDefaults() {
        // Arrange
        Address existingAddress = new Address();
        existingAddress.setAddressId(2L);
        existingAddress.setDefaultAddress(true);
        List<Address> existingAddresses = Arrays.asList(existingAddress);
        address.setDefaultAddress(true);

        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByCustomerUserId(1L)).thenReturn(existingAddresses);
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        // Act
        Address result = addressService.addAddress(1L, address);

        // Assert
        assertNotNull(result);
        assertTrue(result.isDefaultAddress());
        assertFalse(existingAddress.isDefaultAddress());
        verify(userRepository, times(1)).findByUserId(1L);
        verify(addressRepository, times(1)).findByCustomerUserId(1L);
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void addAddress_nonFirstAddressWithoutDefault_keepsExisting() {
        // Arrange
        Address existingAddress = new Address();
        existingAddress.setAddressId(2L);
        existingAddress.setDefaultAddress(true);
        List<Address> existingAddresses = Arrays.asList(existingAddress);

        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByCustomerUserId(1L)).thenReturn(existingAddresses);
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        // Act
        Address result = addressService.addAddress(1L, address);

        // Assert
        assertNotNull(result);
        assertFalse(result.isDefaultAddress());
        assertTrue(existingAddress.isDefaultAddress());
        verify(userRepository, times(1)).findByUserId(1L);
        verify(addressRepository, times(1)).findByCustomerUserId(1L);
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void addAddress_withNonExistentUser_throwsException() {
        // Arrange
        when(userRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> addressService.addAddress(1L, address), "User not found");
        verify(userRepository, times(1)).findByUserId(1L);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void updateAddress_withValidAddress_updatesSuccessfully() {
        // Arrange
        Address updatedAddress = new Address();
        updatedAddress.setAddressId(1L);
        updatedAddress.setReceiverName("Jane Doe");
        updatedAddress.setDefaultAddress(true);

        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(addressRepository.findDefaultAddresses(1L)).thenReturn(Collections.emptyList());
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        // Act
        Address result = addressService.updateAddress(1L, updatedAddress);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Doe", result.getReceiverName());
        assertTrue(result.isDefaultAddress());
        verify(userRepository, times(1)).findByUserId(1L);
        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).findDefaultAddresses(1L);
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void updateAddress_withNonExistentAddress_throwsException() {
        // Arrange
        Address updatedAddress = new Address();
        updatedAddress.setAddressId(1L);

        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> addressService.updateAddress(1L, updatedAddress), "Address not found");
        verify(userRepository, times(1)).findByUserId(1L);
        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void updateAddress_withNonOwnedAddress_throwsException() {
        // Arrange
        Address updatedAddress = new Address();
        updatedAddress.setAddressId(1L);
        User otherUser = new User();
        otherUser.setUserId(2L);
        address.setCustomer(otherUser);

        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> addressService.updateAddress(1L, updatedAddress), "Address not found");
        verify(userRepository, times(1)).findByUserId(1L);
        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void deleteAddress_withValidAddress_deletesSuccessfully() {
        // Arrange
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        // Act
        addressService.deleteAddress(1L, 1L);

        // Assert
        verify(userRepository, times(1)).findByUserId(1L);
        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteAddress_withNonExistentAddress_throwsException() {
        // Arrange
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> addressService.deleteAddress(1L, 1L), "Address not found");
        verify(userRepository, times(1)).findByUserId(1L);
        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteAddress_withNonOwnedAddress_throwsException() {
        // Arrange
        User otherUser = new User();
        otherUser.setUserId(2L);
        address.setCustomer(otherUser);

        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> addressService.deleteAddress(1L, 1L), "Address not found");
        verify(userRepository, times(1)).findByUserId(1L);
        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, never()).deleteById(anyLong());
    }

    @Test
    void getDefaultAddress_withDefaultAddress_returnsAddress() {
        // Arrange
        address.setDefaultAddress(true);
        List<Address> addresses = Arrays.asList(address);
        when(addressRepository.findByCustomerUserId(1L)).thenReturn(addresses);

        // Act
        Address result = addressService.getDefaultAddress(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isDefaultAddress());
        assertEquals("John Doe", result.getReceiverName());
        verify(addressRepository, times(1)).findByCustomerUserId(1L);
    }

    @Test
    void getDefaultAddress_withNoDefaultAddress_returnsNull() {
        // Arrange
        address.setDefaultAddress(false);
        List<Address> addresses = Arrays.asList(address);
        when(addressRepository.findByCustomerUserId(1L)).thenReturn(addresses);

        // Act
        Address result = addressService.getDefaultAddress(1L);

        // Assert
        assertNull(result);
        verify(addressRepository, times(1)).findByCustomerUserId(1L);
    }

    @Test
    void getDefaultAddress_withNoAddresses_returnsNull() {
        // Arrange
        when(addressRepository.findByCustomerUserId(1L)).thenReturn(Collections.emptyList());

        // Act
        Address result = addressService.getDefaultAddress(1L);

        // Assert
        assertNull(result);
        verify(addressRepository, times(1)).findByCustomerUserId(1L);
    }
}