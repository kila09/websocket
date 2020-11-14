package com.example.demo.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket/{pageCode}")
@Component
public class webSocket {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    public static Map<String, List<Session>> electricSocketMap = new ConcurrentHashMap<String, List<Session>>();

    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(@PathParam("pageCode") String pageCode, Session session) {
        List<Session> sessions = electricSocketMap.get(pageCode);
        if(null==sessions){
            List<Session> sessionList = new ArrayList<>();
            sessionList.add(session);
            electricSocketMap.put(pageCode,sessionList);
        }else{
            sessions.add(session);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("pageCode") String pageCode,Session session) {
        if (electricSocketMap.containsKey(pageCode)){
            electricSocketMap.get(pageCode).remove(session);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("websocket received message:"+message);
        try {
            session.getBasicRemote().sendText("这是推送测试数据！您刚发送的消息是："+message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("发生错误");;
    }

    public void sendAll(String message) {
        for (List<Session> items : electricSocketMap.values()) {
            for (Session item:items) {
                item.getAsyncRemote().sendText(message);
            }
        }
    }

    public void sendTo(String userId, String message) {
        List<Session> sessions = electricSocketMap.get(userId);
        for (Session item:sessions) {
            item.getAsyncRemote().sendText(message);
        }
    }
}
