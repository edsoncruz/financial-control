package com.cruz.financialcontrol.controller;

import com.cruz.financialcontrol.model.dto.user.ChangePasswordDTO;
import com.cruz.financialcontrol.model.dto.user.UpdateUserDTO;
import com.cruz.financialcontrol.model.dto.user.UserResponseDTO;
import com.cruz.financialcontrol.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Class Name: UserController
 * Description:
 *
 * @author edson
 * @date 17/09/2026
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateLoggedUser(@Valid @RequestBody UpdateUserDTO updateUserDTO) {
        UserResponseDTO updatedUser = userService.update(updateUserDTO);

        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/me/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(changePasswordDTO);

        return ResponseEntity.ok("Password changed successfully");
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteLoggedUser() {
        userService.deleteLoggedUser();

        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> findLoggedUser() {
        UserResponseDTO loggedUser = userService.findLoggedUser();

        return ResponseEntity.ok(loggedUser);
    }
}
