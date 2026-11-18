package com.estudio.rabbit.qos;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QosProductor {
    private final RabbitTemplate rabbitTemplate;
    public QosProductor(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @GetMapping("/qos")
    public String enviarRafaga() {
        for (int i = 1; i <= 10; i++) { // una ráfaga de 10 mensajes de golpe
            rabbitTemplate.convertAndSend(QosConfig.EXCHANGE_QOS, QosConfig.ROUTING_QOS, "Tarea " + i);
        }
        return "10 tareas enviadas. Mirá la consola: ¿quién hizo más, el lento o el rápido?";
    }
}
