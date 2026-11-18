package com.estudio.rabbit.limites;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LimitesProductor {

    private final RabbitTemplate rabbitTemplate;

    public LimitesProductor(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @GetMapping("/prioridad/llenar")
    public String llenar() {
        int[] nivel = {0, 0, 0, 5, 0}; // el 4.º es URGENTE
        for (int i = 0; i < nivel.length; i++) {
            final int prio = nivel[i];
            rabbitTemplate.convertAndSend("", LimitesConfig.QUEUE_PRIORIDAD, "Mensaje " + (i + 1), m -> {
                m.getMessageProperties().setPriority(prio);
                return m;
            });
        }
        return "5 mensajes enviados. Ahora llamá /prioridad/vaciar";
    }

    @GetMapping("/prioridad/vaciar") // sin listener: leemos con receive()
    public String vaciar() {
        StringBuilder sb = new StringBuilder();
        Object m;
        while ((m = rabbitTemplate.receiveAndConvert(LimitesConfig.QUEUE_PRIORIDAD)) != null) {
            sb.append(m).append(" | ");
        }
        return sb.toString(); // "Mensaje 4 | Mensaje 1 | ..."
    }

    @GetMapping("/limite")
    public String limite(@RequestParam int cantidad) {
        for (int i = 1; i <= cantidad; i++) {
            rabbitTemplate.convertAndSend("", LimitesConfig.QUEUE_LIMITADA, "m" + i, new CorrelationData("m" + i));
        }
        return "Enviados " + cantidad + ". Mirá los ACK/NACK en la consola (cap. 6)";
    }

    @GetMapping("/ttl")
    public String ttl(@RequestParam(defaultValue = "oferta") String texto) {
        rabbitTemplate.convertAndSend("", LimitesConfig.QUEUE_TTL, texto);
        return "Enviado a queue_ttl (sin consumidor): a los 10 s pasa a queue_ttl_expirados";
    }

    @RabbitListener(queues = LimitesConfig.QUEUE_EXPIRADOS, ackMode = "AUTO")
    public void expirado(String texto) {
        System.out.println("  [Limites] mensaje EXPIRADO por TTL: " + texto);
    }
}
