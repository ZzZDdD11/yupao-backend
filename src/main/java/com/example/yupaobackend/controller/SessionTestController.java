package com.example.yupaobackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/session")
public class SessionTestController {

    @GetMapping("/set")
    public String setSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("testKey", "testValue");
        return "Session set successfully!";
    }

    @GetMapping("/get")
    public String getSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object value = session.getAttribute("testKey");
        return value != null ? "Session value: " + value : "No session value found.";
    }
}