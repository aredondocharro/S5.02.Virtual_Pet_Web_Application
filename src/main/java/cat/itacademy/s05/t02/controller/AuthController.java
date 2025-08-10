package cat.itacademy.s05.t02.controller;

import cat.itacademy.s05.t02.controller.dto.AuthLoginRequest;
import cat.itacademy.s05.t02.controller.dto.AuthResponse;
import cat.itacademy.s05.t02.service.UserDetailServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthLoginRequest userRequest) {
         return new ResponseEntity<>(this.userDetailService.loginUser(userRequest),HttpStatus.OK);
    }

}
