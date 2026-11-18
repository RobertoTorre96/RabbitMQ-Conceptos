package com.estudio.rabbit.retrynb;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class RetryNbConsumidor {

    private static final int MAX_INTENTOS = RetryNbConfig.ESPERAS_MS.length;

    private final AmqpTemplate rabbitTemplate;

    public RetryNbConsumidor(AmqpTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @RabbitListener(queues = RetryNbConfig.QUEUE_MAIN) // modo manual (el del yaml)
    public void procesar(String mensaje, Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                         @Header(name = "x-intentos", defaultValue = "0") int intentos,
                         @Header(name = AmqpHeaders.MESSAGE_ID, required = false) String id)
            throws IOException {
        try {
            System.out.println("  [RetryNB] intento " + (intentos + 1) + ": " + mensaje);
            if (mensaje.toLowerCase().contains("error")) {
                throw new RuntimeException("Servicio externo caído");
            }
            channel.basicAck(tag, false);
        } catch (Exception e) {
            reintentarOEstacionar(mensaje, intentos, id);
            channel.basicAck(tag, false); // sacamos el original: ya tiene reemplazo
        }
    }

    /** Lógica pura: fácil de testear sin broker (cap. 23). */
    static String rutaDe(int intentos) {
        return intentos < MAX_INTENTOS ? "wait." + (intentos + 1) : "parking";
    }

    private void reintentarOEstacionar(String mensaje, int intentos, String id) {
        String exchange = intentos < MAX_INTENTOS ? RetryNbConfig.EXCHANGE_WAIT
                                                  : RetryNbConfig.EXCHANGE_PARKING;
        rabbitTemplate.convertAndSend(exchange, rutaDe(intentos), mensaje, m -> {
            m.getMessageProperties().setHeader("x-intentos", intentos + 1);
            m.getMessageProperties().setMessageId(id); // conservamos el id (cap. 13)
            return m;
        });
    }
}
