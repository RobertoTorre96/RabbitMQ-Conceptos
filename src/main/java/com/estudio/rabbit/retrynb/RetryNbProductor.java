package com.estudio.rabbit.retrynb;

import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RetryNbProductor {
    private final RabbitTemplate rabbitTemplate;

    public RetryNbProductor(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @GetMapping("/retrynb")
    public String enviar(@RequestParam String texto) {
        rabbitTemplate.convertAndSend(RetryNbConfig.EXCHANGE_MAIN, RetryNbConfig.RK_MAIN, texto, m -> {
            m.getMessageProperties().setMessageId(UUID.randomUUID().toString());
            return m;
        });
        return "Enviado (si contiene 'error' pasa por las colas de espera): " + texto;
    }
}
