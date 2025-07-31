package cat.itacademy.s05.t02.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, welcome to the Virtual Pet!";
    }
    @GetMapping("/hello-secured")
    public String helloSecured() {
        return "Hello, welcome to the Virtual Pet Secured!";
    }
}
