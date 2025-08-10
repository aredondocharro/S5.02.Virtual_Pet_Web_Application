package cat.itacademy.s05.t02.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app")
public class AppController {

    @GetMapping("/get")
    public String helloGet() {
        return "Hello, World, this is get!";
    }
    @PostMapping("/post")
    public String helloPost() {
        return "Hello, World, this is post!";
    }
    @PutMapping("/put")
    public String helloPut() {
        return "Hello, World, this is put!";
    }
    @DeleteMapping("/delete")
    public String helloDelete() {
        return "Hello, World, this is delete!";
    }
    @PatchMapping("/patch")
    public String helloPatch() {
        return "Hello, World, this is patch!";
    }

}
