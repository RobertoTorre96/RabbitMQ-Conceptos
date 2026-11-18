package com.estudio.rabbit.dlq;

import com.rabbitmq.client.Channel;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class DlqConsumidores {

    private final Counter muertos; // cap. 22 (Vol. 2): la DLQ se vacía sola, así que contamos en la app

    public DlqConsumidores(MeterRegistry registry) {
        this.muertos = Counter.builder("app.dlq.recibidos").register(registry);
    }

    @RabbitListener(queues = DlqConfig.QUEUE_NORMAL)
    public void procesarNormal(String mensaje, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            System.out.println("  [Dlq] Intentando procesar: '" + mensaje + "'");
            if (mensaje.toLowerCase().contains("error")) {
                throw new RuntimeException("Excepción en base de datos o formato inválido");
            }
            channel.basicAck(tag, false);                 // 1) todo bien -> ACK
        } catch (Exception e) {
            System.out.println("  [Dlq] Falló: " + e.getMessage() + ". Descartando...");
            try {
                channel.basicNack(tag, false, false);     // 2) falló -> NACK sin re-encolar -> va a la DLX
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    @RabbitListener(queues = DlqConfig.QUEUE_DLQ)
    public void procesarDlq(String mensaje, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        muertos.increment(); // Prometheus ve cuántos murieron, aunque la cola siempre muestre 0
        System.out.println("  [Hospital DLQ] Mensaje rescatado de la muerte: '" + mensaje + "'");
        channel.basicAck(tag, false);
    }
}
