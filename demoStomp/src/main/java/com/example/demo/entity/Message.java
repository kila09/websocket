package com.example.demo.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Message {

    /**
     * 消息内容
     */
    private String content;

    private MessageType type;

    private String sender;

    private String to;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

    @Override
    public String toString(){
        return "Message{" +
                "type=" + type +
                ", content='" + content + '\'' +
                ", sender='" + sender + '\'' +
                ", to='" + to + '\'' +
                '}';
    }

}
