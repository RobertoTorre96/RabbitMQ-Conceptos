package com.estudio.rabbit.retry;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RetryProductor {
    private final RabbitTemplate rabbitTemplate;
    public RetryProductor(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @GetMapping("/retry")
    public String enviar(@RequestParam String texto) {
        rabbitTemplate.convertAndSend(RetryConfig.EXCHANGE_RETRY, RetryConfig.ROUTING_RETRY, texto);
        return "Enviado (si contiene 'error', Spring reintenta 3 veces): " + texto;
    }
}
