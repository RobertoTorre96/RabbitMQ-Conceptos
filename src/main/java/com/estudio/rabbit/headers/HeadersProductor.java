package com.estudio.rabbit.headers;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HeadersProductor {
    private final RabbitTemplate rabbitTemplate;
    public HeadersProductor(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @GetMapping("/headers/valido")
    public String enviarValido() {
        enviar("pdf");
        return "Mensaje enviado (Debería ser recibido)";
    }

    @GetMapping("/headers/invalido")
    public String enviarInvalido() {
        enviar("excel");
        return "Mensaje enviado (No debería coincidir: se devuelve por falta de ruta)";
    }

    private void enviar(String formato) {
        // La routing key es una cadena vacía ""
        rabbitTemplate.convertAndSend(HeadersConfig.EXCHANGE_HEADERS, "", "Reporte_Mensual", mensaje -> {
            mensaje.getMessageProperties().setHeader("formato", formato);
            mensaje.getMessageProperties().setHeader("departamento", "ventas");
            return mensaje; // MessagePostProcessor: agrega las cabeceras
        });
    }
}
