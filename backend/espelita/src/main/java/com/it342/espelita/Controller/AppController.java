package com.it342.espelita.Controller;

import com.it342.espelita.Dto.*;
import com.it342.espelita.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class AppController {

    @Autowired
    private UserService userService;

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto dto) {
        return ResponseEntity.ok(userService.registerNewUser(dto));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto) {
        return ResponseEntity.ok("Login successful");
    }

    @GetMapping("/user/me")
    public ResponseEntity<?> getMe(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(userService.findByUsername(principal.getName()));
    }
}