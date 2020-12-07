package com.example.demo.ui;

import com.example.demo.config.rabbitmq.RabbitConfig;
import com.example.demo.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
public class MessageUI {

    @Autowired
    private RabbitConfig rabbitConfig;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private static final Logger logger = LoggerFactory.getLogger(MessageUI.class);

    @ResponseBody
    @GetMapping("/getConfig")
    public String getConfig(){
        return rabbitConfig.getUserName();
    }

    @MessageMapping("/test")
    @SendTo("/topic/test")
    public Message test(Message message){
        logger.info("服务器端接收到消息：" + message.getContent());
        Message m = new Message();
        m.setContent("接收到消息：" + message.getContent());
        return m;
    }

    /**
     * 订阅模式，只是在订阅的时候触发，可以理解为：访问——>返回数据
     */
    @SubscribeMapping("/getMessage")
    public Message getMessage(){
        logger.info("服务器端接收到消息：");
        Message m = new Message();
        m.setContent("Hello world!");
        return m;
    }

    /**
     * 广播消息，不指定用户，所有订阅此的用户都能收到消息
     */
    @MessageMapping("/broadcast")
    public void broadcast(Message message) {
        logger.info("服务器端接收到广播消息：" + message.getContent());
        simpMessagingTemplate.convertAndSend("/topic/msg", message.getContent());
    }

    /**
     * 用户模式
     */
    @MessageMapping("/singleUser")
    public void singleUser(Message message, StompHeaderAccessor stompHeaderAccessor) {
        String m = message.getContent();
        logger.info("服务器端接收到单播消息：" + m);
        Principal user = stompHeaderAccessor.getUser();
        simpMessagingTemplate.convertAndSendToUser(user.getName(), "/queue/single", message.getContent());
    }

}
