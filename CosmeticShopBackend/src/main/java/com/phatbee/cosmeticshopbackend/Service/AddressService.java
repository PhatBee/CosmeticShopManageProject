package com.phatbee.cosmeticshopbackend.Service;

import com.phatbee.cosmeticshopbackend.Entity.Address;

import java.util.List;

public interface AddressService {
    List<Address> getAddressesByUserId(Long userId);
    Address addAddress(Long userId, Address address);
    Address updateAddress(Long userId, Address address);
    void deleteAddress(Long userId, Long addressId);
    Address getDefaultAddress(Long userId);
}
