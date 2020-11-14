package com.example.demo.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@RestController
public class msController {

    @RequestMapping("/helloWorld")
    private String HelloWorld(){
        return "helloWorld";
    }


    //这个bean的注册,用于扫描带有@ServerEndpoint的注解成为websocket  ,如果你使用外置的tomcat就不需要该配置文件
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
