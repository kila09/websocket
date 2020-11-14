package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @Autowired
    com.example.demo.websocket.webSocket webSocket;

    @ResponseBody
    @GetMapping("/sendAll")
    public String sendAll(@RequestParam("msg") String msg) {
        webSocket.sendAll(msg);
        return "推送成功";
    }

    @ResponseBody
    @GetMapping("/sendTo")
    public String sendTo(@RequestParam("userId") String userId,@RequestParam("msg") String msg) {
        webSocket.sendTo(userId, msg);
        return "推送成功";
    }
}
