package com.estudio.rabbit.direct;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectConfig {
    public static final String EXCHANGE_NAME = "mi_exchange_direct";
    public static final String QUEUE_INFO = "queue_direct_info";
    public static final String QUEUE_ERROR = "queue_direct_error";

    @Bean public DirectExchange directExchange() { return new DirectExchange(EXCHANGE_NAME); }
    @Bean public Queue colaDirectInfo() { return new Queue(QUEUE_INFO, true); }
    @Bean public Queue colaDirectError() { return new Queue(QUEUE_ERROR, true); }

    // la clave que acepta cada cola se define en el BINDING
    @Bean public Binding bindingInfo() { return BindingBuilder.bind(colaDirectInfo()).to(directExchange()).with("info"); }
    @Bean public Binding bindingError() { return BindingBuilder.bind(colaDirectError()).to(directExchange()).with("error"); }
}
