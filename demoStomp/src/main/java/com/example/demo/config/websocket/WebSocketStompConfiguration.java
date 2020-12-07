package com.example.demo.config.websocket;

import com.example.demo.config.rabbitmq.RabbitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfiguration implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private RabbitConfig rabbitConfig;

    /**
     * 注册stomp端点，主要是起到连接作用
     *
     * @param stompEndpointRegistry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        stompEndpointRegistry
                .addEndpoint("/webSocket")  //端点名称
                //.setHandshakeHandler() 握手处理，主要是连接的时候认证获取其他数据验证等
                //.addInterceptors() 拦截处理，和http拦截类似
                .setAllowedOrigins("*") //跨域
                .withSockJS(); //使用sockJS

    }

    /**
     * 注册相关服务
     *
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //基于RabbitMQ 的STOMP消息代理
        registry.enableStompBrokerRelay("/queue", "/topic")
                .setRelayHost(rabbitConfig.getHost())
                .setRelayPort(rabbitConfig.getPort())
                .setClientLogin(rabbitConfig.getUserName())
                .setClientPasscode(rabbitConfig.getPassword());
        //这里使用的是内存模式。
        //这里注册两个，主要是目的是将广播和队列分开。
        //registry.enableSimpleBroker("/topic", "/queue");
        //这是给客户端推送消息到服务器使用 ，推送的接口加上/app
        registry.setApplicationDestinationPrefixes("/app");
        //这是给sendToUser使用,前端订阅需要加上/user
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 1、设置拦截器
     * 2、首次连接的时候，获取其Header信息，利用Header里面的信息进行权限认证
     * 3、通过认证的用户，使用 accessor.setUser(user); 方法，将登陆信息绑定在该 StompHeaderAccessor 上，在Controller方法上可以获取 StompHeaderAccessor 的相关信息
     *
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptorAdapter() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                //1、判断是否首次连接
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    //2、判断用户名和密码
                    String name = accessor.getNativeHeader("username").get(0);
                    String pas = accessor.getNativeHeader("password").get(0);

                    Principal principal = new UserPrincipal(name);
                    accessor.setUser(principal);
                    return message;
                }
                //不是首次连接，已经登陆成功
                return message;
            }
        });

    }
}