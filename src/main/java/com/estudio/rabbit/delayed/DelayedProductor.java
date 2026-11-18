package com.estudio.rabbit.delayed;

import java.time.LocalTime;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DelayedProductor {
    private final RabbitTemplate rabbitTemplate;
    public DelayedProductor(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @GetMapping("/delayed")
    public String enviarAlFuturo(@RequestParam int segundos, @RequestParam String texto) {
        System.out.println("  [Delayed] Disparando a las " + LocalTime.now() + ". Despertará en " + segundos + " seg.");
        // Directo a la sala de espera (su nombre como routing key, exchange "")
        rabbitTemplate.convertAndSend(DelayedConfig.QUEUE_ESPERA, (Object) texto, mensaje -> {
            // segundos -> milisegundos (String): TTL de ESTE mensaje
            mensaje.getMessageProperties().setExpiration(String.valueOf(segundos * 1000));
            return mensaje;
        });
        return "Mensaje en sala de espera. Revisa la consola.";
    }
}
