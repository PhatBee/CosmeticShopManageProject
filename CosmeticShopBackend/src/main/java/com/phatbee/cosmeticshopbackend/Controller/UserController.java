package com.phatbee.cosmeticshopbackend.Controller;

import com.phatbee.cosmeticshopbackend.Entity.User;
import com.phatbee.cosmeticshopbackend.Service.Impl.UserServiceImpl;
import com.phatbee.cosmeticshopbackend.dto.UserUpdateDTO;
import com.phatbee.cosmeticshopbackend.dto.UserUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserServiceImpl userService;
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserUpdateResponse> updateUser(@PathVariable Long userId, @RequestBody UserUpdateDTO userUpdateDTO) {
        UserUpdateResponse response = userService.updateUser(userId, userUpdateDTO);
        return ResponseEntity.ok(response);
    }

}
