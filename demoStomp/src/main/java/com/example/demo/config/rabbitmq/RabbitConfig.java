package com.example.demo.config.rabbitmq;

import com.example.demo.service.ChatService;
import com.example.demo.ui.MessageUI;
import lombok.Data;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
@Data
public class RabbitConfig {

    @Autowired
    MessageUI messageUI;

    @Autowired
    ChatService chatService;

    private String host;

    private int port;

    private String userName;

    private String password;

    //绑定键
    public final static String msgTopicKey = "topic.public";

    //绑定队列
    public final static String msgTopicQueue = "topicQueue";

    @Bean
    public Queue topicQueue() {
        return new Queue(RabbitConfig.msgTopicQueue, true);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(RabbitConfig.msgTopicQueue, true, false);
    }

    @Bean
    public Binding bindingExchangeMessage() {
        return BindingBuilder.bind(topicQueue()).to(topicExchange()).with("topic");
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(this.host, this.port);
        connectionFactory.setUsername(this.userName);
        connectionFactory.setPassword(this.password);
        connectionFactory.setVirtualHost("kila");
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.SIMPLE); // 发送消息回调,必须要设置
        connectionFactory.setPublisherReturns(true);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        //设置开启Mandatory,才能触发回调函数,无论消息推送结果怎么样都强制调用回调函数
        rabbitTemplate.setMandatory(true);

        //确认是否正确到达 Exchange 中
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("ConfirmCallback: " + "相关数据：" + correlationData);
                System.out.println("ConfirmCallback: " + "确认情况：" + ack);
                System.out.println("ConfirmCallback: " + "原因：" + cause);
            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("ReturnCallback: " + "消息：" + message);
                System.out.println("ReturnCallback: " + "回应码：" + replyCode);
                System.out.println("ReturnCallback: " + "回应信息：" + replyText);
                System.out.println("ReturnCallback: " + "交换机：" + exchange);
                System.out.println("ReturnCallback: " + "路由键：" + routingKey);
            }
        });
        return rabbitTemplate;
    }

    @Bean
    public SimpleMessageListenerContainer rabbitListenerContainerFactory() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        //监听接受消息队列topicQueue的消息
        container.setQueues(topicQueue());
        container.setExposeListenerChannel(true);
        container.setMaxConcurrentConsumers(1);
        container.setConcurrentConsumers(1);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //设置确认模式手工确认
        container.setMessageListener(new ChannelAwareMessageListener() {
            public void onMessage(Message message, com.rabbitmq.client.Channel channel) throws Exception {
                byte[] body = message.getBody();
                String msg = new String(body);
                System.out.println("rabbitmq收到消息 : " + msg);
                Boolean sendToWebsocket = chatService.sendMsg(msg);

                if (sendToWebsocket) {
                    System.out.println("消息处理成功！ 已经推送到websocket！");
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), true); //确认消息成功消费

                }
            }

        });
        return container;
    }
}
