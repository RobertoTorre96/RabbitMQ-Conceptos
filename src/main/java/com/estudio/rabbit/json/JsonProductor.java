package com.estudio.rabbit.json;

import java.util.Random;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsonProductor {
    private final RabbitTemplate rabbitTemplate;
    public JsonProductor(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @GetMapping("/json")
    public String enviarPedidoJson(@RequestParam String producto, @RequestParam double precio) {
        Pedido nuevoPedido = new Pedido(new Random().nextInt(1000), producto, precio);
        rabbitTemplate.convertAndSend(JsonConfig.EXCHANGE_JSON, JsonConfig.ROUTING_JSON, nuevoPedido); // el OBJETO directo
        return "Objeto Pedido enviado a RabbitMQ. Revisa la consola.";
    }
}
