package com.phatbee.cosmeticshopbackend.Controller;

import com.phatbee.cosmeticshopbackend.Entity.Address;
import com.phatbee.cosmeticshopbackend.Repository.AddressRepository;
import com.phatbee.cosmeticshopbackend.Repository.UserRepository;
import com.phatbee.cosmeticshopbackend.Service.Impl.AddressServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    @Autowired
    private AddressServiceImpl addressService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Address>> getUserAddresses(@PathVariable Long userId) {
        try{
            List<Address> addresses = addressService.getAddressesByUserId(userId);
            return ResponseEntity.ok(addresses);
        }
        catch(RuntimeException e){
           return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Address> addAddress(@RequestParam Long userId, @RequestBody Address address) {
        try {
            Address savedAddress = addressService.addAddress(userId, address);
            return ResponseEntity.status(201).body(savedAddress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Address> updateAddress(@RequestParam Long userId, @RequestBody Address updatedAddress) {
        try {
            Address savedAddress = addressService.updateAddress(userId, updatedAddress);
            return ResponseEntity.ok(savedAddress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId) {
        if (!addressRepository.existsById(addressId)) {
            return ResponseEntity.notFound().build();
        }
        addressRepository.deleteById(addressId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/default/{userId}")
    public ResponseEntity<Address> getDefaultAddress(@PathVariable Long userId) {
        Address defaultAddress = addressService.getDefaultAddress(userId);
        if (defaultAddress == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(defaultAddress);
    }


}
