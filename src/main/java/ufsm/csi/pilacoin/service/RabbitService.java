package ufsm.csi.pilacoin.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ufsm.csi.pilacoin.Constants;

@Service
public class RabbitService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String topic, String object) {
        this.rabbitTemplate.convertAndSend(topic, object);
    }

    @RabbitListener(queues = {"luiz_felipe"})
    public void rabbitResponse(@Payload Message message) {
        String responseMessage = new String(message.getBody());
        String outputColor = responseMessage.contains("erro") ? Constants.ANSI_RED : Constants.ANSI_GREEN;
        System.out.println(outputColor + responseMessage + Constants.ANSI_RESET);
    }
}
