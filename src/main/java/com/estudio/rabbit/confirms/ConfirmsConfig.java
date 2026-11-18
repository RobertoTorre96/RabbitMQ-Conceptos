package com.estudio.rabbit.confirms;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfirmsConfig {
    public static final String EXCHANGE_CONFIRMS = "exchange_confirms";
    public static final String QUEUE_CONFIRMS = "queue_confirms";
    public static final String ROUTING_CONFIRMS = "rutina.confirms";

    @Bean public DirectExchange confirmsExchange() { return new DirectExchange(EXCHANGE_CONFIRMS); }
    @Bean public Queue confirmsQueue() { return new Queue(QUEUE_CONFIRMS, true); }
    @Bean public Binding confirmsBinding() {
        return BindingBuilder.bind(confirmsQueue()).to(confirmsExchange()).with(ROUTING_CONFIRMS);
    }
}
