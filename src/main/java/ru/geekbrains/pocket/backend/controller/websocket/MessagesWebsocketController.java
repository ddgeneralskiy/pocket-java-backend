package ru.geekbrains.pocket.backend.controller.websocket;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.util.HtmlUtils;
import ru.geekbrains.pocket.backend.domain.User;
import ru.geekbrains.pocket.backend.domain.UserMessage;
import ru.geekbrains.pocket.backend.service.UserMessageService;
import ru.geekbrains.pocket.backend.service.UserService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

//https://o7planning.org/ru/10719/create-a-simple-chat-application-with-spring-boot-and-websocket
//https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#websocket-server

@Controller
@Slf4j
public class MessagesWebsocketController implements ApplicationListener<BrokerAvailabilityEvent> {

    private AtomicBoolean brokerAvailable = new AtomicBoolean(); //доступность брокера

    @Autowired
    private UserService userService;

    private SimpMessagingTemplate simpMessagingTemplate;
    private UserMessageService userMessageService;

    @Autowired
    public MessagesWebsocketController(SimpMessagingTemplate simpMessagingTemplate,
                                       UserMessageService userMessageService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userMessageService = userMessageService;
    }

    @Override
    public void onApplicationEvent(BrokerAvailabilityEvent event) {
        this.brokerAvailable.set(event.isBrokerAvailable());
    }

    @MessageMapping("/test")
    @SendTo("/topic/test")
    public TestMessage testMessage(@Payload TestMessage testMessage) {
        log.debug("testMessage: " + testMessage.getUsername());// principal.getName());
        System.out.println("testMessage: " + testMessage.getUsername() + ", " + testMessage);
        return new TestMessage(testMessage.getUsername(),
                testMessage.getEmail(), testMessage.getText());
    }

    @SubscribeMapping("/testsubscribe")
    @SendTo("/topic/testsubscribe")
    public TestMessage testSubscribe(@Payload TestMessage testMessage) { //Principal principal,
        log.debug("testSubscribe: " + testMessage.getUsername());
        System.out.println("testSubscribe: " + testMessage.getUsername() + ", " + testMessage);
        return new TestMessage(testMessage.getUsername(),
                testMessage.getEmail(), "testSubscribe: " + testMessage.getText());
    }

    //@Scheduled(fixedDelay=7000)
    public void sendTest() {
        TestMessage user = new TestMessage("TestMessage", "email3", "sendTest");
        if (log.isTraceEnabled()) {
            log.trace("sendTest: " + user);
        }
        if (this.brokerAvailable.get()) {
            log.debug("sendTest: " + user);
            System.out.println("sendTest: " + user);

            this.simpMessagingTemplate.convertAndSend("/topic/test." + "sendTest", user);
        }
    }

    //@Scheduled(fixedDelay=10000)
    public void sendToUserTest() {
        Map<String, Object> map = new HashMap<>();
        map.put(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);

        TestMessage user = new TestMessage("TestMessage", "email4", "sendToUserTest");
        if (this.brokerAvailable.get()) {
            log.debug("sendToUserTest: " + user);
            System.out.println("sendToUserTest: " + user);

            String payload = "sendToUserTest: " + user.getUsername();
            this.simpMessagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/sendToUserTest", payload, map);
        }
    }

    public void sendToUserTestError() {
        TestMessage user = new TestMessage("TestMessage", "email5", "sendToUserTestError");
        if (this.brokerAvailable.get()) {
            log.debug("sendToUserTestError: " + user);
            System.out.println("sendToUserTestError: " + user);

            String payload = "sendToUserTestError: " + user;
            this.simpMessagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/errors", payload);
        }
    }

    @MessageMapping("/send") //Отправить новое сообщение
    @SendToUser("/topic/send") //Событие "Новое сообщение"
    public String clientSendMessage(Principal principal, @Payload ClientSendMessage message) {
        if (message.getText().equals("") || message.getGroup().equals("")) {
            //TODO проверка на ошибки
            return "Error";
        } else {
            String messageId = "Error";
            User sender = userService.getUserByUsername(principal.getName());
            if (!message.getGroup().equals("")) {
                //TODO запись сообщения для группы в бд
                //TODO отправить сообщение группе
            } else if (!message.getRecipicent().equals("")) {
                User recepient = userService.findUserByID(new ObjectId(message.getRecipicent()));
                if (recepient != null) {
                    UserMessage userMessage = userMessageService.createMessage(sender, recepient, message.getText());
                    messageId = userMessage.getId().toString();

                    //TODO отправить сообщение получателю
                    //send message Websocket
                    this.simpMessagingTemplate.convertAndSend("/queue/new", message.getText());
                }
            }
            return messageId;
        }
    }

    @MessageMapping("/read") //Прочитал сообщение
    @SendToUser("/queue/read") //Cобытие "Пользователь прочитал сообщение"
    public ServerMessageRead clientMessageRead(@Payload ClientMessageRead message, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", message.getSender());
        //TODO найти сообщение в бд и получить получателя
        String recipient = "";
        System.out.println("test readMessage: " + message.getSender());
        System.out.println("test readMessage: " + HtmlUtils.htmlEscape(message.getSender()));
        return new ServerMessageRead(message.getIdMessage(), recipient);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }


    //=========Models request & response=========
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    private static class TestMessage {
        private String username;
        private String email;
        private String text;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ClientSendMessage {
        private String text;
        private String group;
        private String recipicent;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ClientMessageRead {
        private String idMessage;
        private String sender;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ServerMessageRead {
        private String idMessage;
        private String recipient;
    }
}
