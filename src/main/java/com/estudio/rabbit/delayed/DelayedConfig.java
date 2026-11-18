package com.estudio.rabbit.delayed;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DelayedConfig {
    public static final String EXCHANGE_DESTINO = "exchange_destino";
    public static final String QUEUE_DESTINO = "queue_destino";
    public static final String ROUTING_DESTINO = "rutina.destino";
    public static final String QUEUE_ESPERA = "queue_sala_espera";

    // 1. El destino final (donde trabaja el consumidor)
    @Bean public DirectExchange destinoExchange() { return new DirectExchange(EXCHANGE_DESTINO); }
    @Bean public Queue destinoQueue() { return new Queue(QUEUE_DESTINO, true); }
    @Bean public Binding bindingDestino() {
        return BindingBuilder.bind(destinoQueue()).to(destinoExchange()).with(ROUTING_DESTINO);
    }

    // 2. La sala de espera: al "morir" por tiempo, el mensaje va al EXCHANGE_DESTINO
    @Bean
    public Queue esperaQueue() {
        return QueueBuilder.durable(QUEUE_ESPERA)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DESTINO)
                .withArgument("x-dead-letter-routing-key", ROUTING_DESTINO)
                .build();
    }
}
