package com.estudio.rabbit.errores;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErroresController {
    private final RabbitTemplate rabbitTemplate;

    public ErroresController(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    /** ok-1 → OK | caido-1 → 3 intentos y queue_errores | invalido-1 → rechazo explícito */
    @GetMapping("/errores")
    public String enviar(@RequestParam String texto) {
        rabbitTemplate.convertAndSend("", ErroresConfig.QUEUE_TRABAJO, texto);
        return "Enviado: " + texto;
    }
}
