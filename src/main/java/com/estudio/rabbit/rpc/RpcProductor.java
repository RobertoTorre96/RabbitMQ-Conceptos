package com.estudio.rabbit.rpc;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RpcProductor {
    private final RabbitTemplate rabbitTemplate;
    public RpcProductor(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    @GetMapping("/rpc")
    public String enviarPeticionSincrona(@RequestParam String operacion) {
        System.out.println("  [Cliente HTTP] BLOQUEANDO hasta recibir la respuesta...");
        // convertSendAndReceive: envía y CONGELA el hilo hasta tener el retorno (null tras el reply-timeout)
        Object respuesta = rabbitTemplate.convertSendAndReceive(RpcConfig.EXCHANGE_RPC, RpcConfig.ROUTING_RPC, operacion);
        System.out.println("  [Cliente HTTP] respuesta recibida: " + respuesta);
        return "El servidor procesó tu operación. Resultado: " + respuesta;
    }
}
