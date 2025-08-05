package cat.itacademy.s05.t02.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@PreAuthorize("denyAll()") // Deny all access by default
public class AuthController {

    @GetMapping("/hello")
    @PreAuthorize("permitAll()") // Allow public access to this endpoint
    public String hello() {
        return "Hello, welcome to the Virtual Pet!";
    }
    @GetMapping("/hello-secured")
    @PreAuthorize("hasAuthority('READ')") // Allow access only to users with 'READ' authority
    public String helloSecured() {
        return "Hello, welcome to the Virtual Pet Secured!";
    }
}
