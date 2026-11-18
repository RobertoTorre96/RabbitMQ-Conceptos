package com.estudio.rabbit.confirms;

import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cap. 6. Los callbacks son GLOBALES (viven en el RabbitTemplate compartido): también se imprimen
 * para direct, headers, el outbox, etc. Solo se invocan si el envío lleva CorrelationData (confirm).
 */
@RestController
public class ConfirmsProductor {

    private final RabbitTemplate rabbitTemplate;

    public ConfirmsProductor(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @PostConstruct
    public void configurarCallbacks() {
        // Antena 1: ¿Llegó al Exchange?
        rabbitTemplate.setConfirmCallback((correlationData, ack, causaError) -> {
            String id = correlationData != null ? correlationData.getId() : "?";
            if (ack) System.out.println("  [Recibo] ¡Éxito! RabbitMQ guardó el mensaje. (" + id + ")");
            else     System.err.println("  [Recibo] ERROR GRAVE (" + id + "). Causa: " + causaError);
        });
        // Antena 2: llegó al Exchange, pero ¿encontró una ruta válida hacia una cola?
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("  [Devolución] Sin cola para la ruta: '" + returned.getRoutingKey() + "'");
            System.err.println("    Mensaje devuelto: " + new String(returned.getMessage().getBody()));
        });
    }

    @GetMapping("/confirms/exito") // exchange_confirms + "rutina.confirms": ruta perfecta
    public String exito(@RequestParam String texto) {
        System.out.println("  [Confirms] Enviando mensaje correcto...");
        rabbitTemplate.convertAndSend(ConfirmsConfig.EXCHANGE_CONFIRMS, ConfirmsConfig.ROUTING_CONFIRMS,
                texto, new CorrelationData("exito-" + texto));
        return "Enviado: " + texto;
    }

    @GetMapping("/confirms/error-exchange") // un exchange FANTASMA: el broker responde NACK
    public String errorExchange(@RequestParam String texto) {
        try {
            rabbitTemplate.convertAndSend("exchange_que_no_existe", "x", texto, new CorrelationData("fantasma-" + texto));
            return "Enviado (mirá la consola: NACK y NOT_FOUND)";
        } catch (Exception e) {
            return "Excepción al publicar: " + e.getMessage();
        }
    }

    @GetMapping("/confirms/ruta-mala") // el "Ojo" del Vol. 1: exchange real + routing key sin binding => RETURN (y también ACK)
    public String rutaMala(@RequestParam String texto) {
        rabbitTemplate.convertAndSend(ConfirmsConfig.EXCHANGE_CONFIRMS, "ruta.mala", texto, new CorrelationData("mala-" + texto));
        return "Enviado con routing key sin binding (mirá el [Devolución] y el [Recibo])";
    }

    @RabbitListener(queues = ConfirmsConfig.QUEUE_CONFIRMS)
    public void recibir(String texto, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            System.out.println("  [Confirms] consumidor recibió: " + texto);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            try { channel.basicNack(tag, false, false); } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}
