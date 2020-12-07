package com.example.demo.ui;

import com.example.demo.entity.Message;
import com.example.demo.util.JsonUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

public class ChatUI {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 服务端推送给单人的接口
     * @param uid
     * @param content
     */
    @ResponseBody
    @GetMapping("/sendToOne")
    public void sendToOne(@RequestParam("uid") String uid, @RequestParam("content") String content ){

        Message chatMessage=new Message();
        chatMessage.setType(Message.MessageType.CHAT);
        chatMessage.setContent(content);
        chatMessage.setTo(uid);
        chatMessage.setSender("系统消息");
        rabbitTemplate.convertAndSend("topicWebSocketExchange","topic.public", JsonUtil.parseObjToJson(chatMessage));

    }


    /**
     * 接收 客户端传过来的消息 通过setSender和type 来判别时单发还是群发
     * @param chatMessage
     * @param principal
     */
    @MessageMapping("/chat.sendMessageTest")
    public void sendMessageTest(@Payload Message chatMessage, Principal principal) {
        try {

            String name = principal.getName();
            chatMessage.setSender(name);
            rabbitTemplate.convertAndSend("topicWebSocketExchange","topic.public", JsonUtil.parseObjToJson(chatMessage));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * 接收 客户端传过来的消息 上线消息
     * @param chatMessage
     */
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload Message chatMessage) {

        System.out.println("有用户加入到了websocket 消息室" + chatMessage.getSender());
        try {

            System.out.println(chatMessage.toString());
            rabbitTemplate.convertAndSend("topicWebSocketExchange","topic.public", JsonUtil.parseObjToJson(chatMessage));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


}
