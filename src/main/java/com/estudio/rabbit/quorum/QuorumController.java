package com.estudio.rabbit.quorum;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuorumController {
    private final RabbitTemplate rabbitTemplate;

    public QuorumController(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @GetMapping("/quorum")
    public String enviar(@RequestParam String texto) {
        rabbitTemplate.convertAndSend("", QuorumConfig.QUEUE_QUORUM, texto);
        return "Enviado a la quorum queue: " + texto;
    }
}
