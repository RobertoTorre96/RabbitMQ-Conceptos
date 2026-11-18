package com.estudio.rabbit.idem;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdemConfig {
    public static final String EXCHANGE = "exchange_idem";
    public static final String QUEUE = "queue_idem";
    public static final String ROUTING = "rutina.idem";

    @Bean public DirectExchange idemExchange() { return new DirectExchange(EXCHANGE); }
    @Bean public Queue idemQueue() { return new Queue(QUEUE, true); }
    @Bean public Binding idemBinding() {
        return BindingBuilder.bind(idemQueue()).to(idemExchange()).with(ROUTING);
    }
}
