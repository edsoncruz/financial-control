package com.cruz.financialcontrol.controller;

import com.cruz.financialcontrol.model.dto.user.AuthLoginDTO;
import com.cruz.financialcontrol.model.dto.user.AuthResponseDTO;
import com.cruz.financialcontrol.model.dto.user.CreateUserDTO;
import com.cruz.financialcontrol.model.dto.user.UserResponseDTO;
import com.cruz.financialcontrol.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthLoginDTO authLoginDTO) {
        return ResponseEntity.ok(userService.login(authLoginDTO));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody CreateUserDTO createUserDTO) {
        UserResponseDTO userResponseDTO = userService.create(createUserDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }
}
