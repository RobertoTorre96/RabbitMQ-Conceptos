package com.estudio.rabbit.streams;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StreamProductor {
    private final RabbitTemplate rabbitTemplate;

    public StreamProductor(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @GetMapping("/stream")
    public String enviar(@RequestParam String texto) {
        // exchange por defecto, routing key = nombre del stream
        rabbitTemplate.convertAndSend("", StreamConfig.STREAM_EVENTOS, "evento: " + texto);
        return "Publicado en el stream: " + texto;
    }
}
