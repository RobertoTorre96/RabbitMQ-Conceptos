package com.estudio.rabbit.dlq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DlqProductor {
    private final RabbitTemplate rabbitTemplate;
    public DlqProductor(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @GetMapping("/dlq")
    public String enviar(@RequestParam String texto) {
        rabbitTemplate.convertAndSend(DlqConfig.EXCHANGE_NORMAL, DlqConfig.ROUTING_NORMAL, texto);
        return "Enviado a la cola normal (si contiene 'error' termina en la DLQ): " + texto;
    }
}
