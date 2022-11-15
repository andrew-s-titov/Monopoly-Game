package com.monopolynew.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SocketJSController {

    @GetMapping("/connect/{username}/info")
    public void checkSocketJS(@PathVariable String username) {

    }
}
