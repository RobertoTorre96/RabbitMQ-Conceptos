package com.estudio.rabbit.base;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** B1/B2: productor + consumidor básico con ACK manual. El trabajo va DENTRO del try (ver "Ojo" del Vol. 1). */
@RestController
public class BaseController {

    private final RabbitTemplate rabbitTemplate;

    public BaseController(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @GetMapping("/enviar")
    public String enviarMensaje(@RequestParam String texto) {
        rabbitTemplate.convertAndSend(BaseConfig.QUEUE_NAME, texto);
        return "Mensaje enviado a RabbitMQ: " + texto;
    }

    @RabbitListener(queues = BaseConfig.QUEUE_NAME) // manual (el del yaml)
    public void recibirMensaje(String mensaje, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            System.out.println("  [Consumidor Spring] Mensaje recibido: '" + mensaje + "'");
            channel.basicAck(deliveryTag, false);          // ACK manual, lo último que se hace
        } catch (Exception e) {
            try { channel.basicNack(deliveryTag, false, false); } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}
