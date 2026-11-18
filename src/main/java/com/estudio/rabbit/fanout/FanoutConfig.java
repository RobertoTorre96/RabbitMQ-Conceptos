package com.estudio.rabbit.fanout;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FanoutConfig {
    public static final String EXCHANGE_NAME = "mi_exchange_fanout";
    public static final String QUEUE_CONTABILIDAD = "queue_fanout_contabilidad";
    public static final String QUEUE_MARKETING = "queue_fanout_marketing";

    @Bean public FanoutExchange fanoutExchange() { return new FanoutExchange(EXCHANGE_NAME); }
    @Bean public Queue colaContabilidad() { return new Queue(QUEUE_CONTABILIDAD, true); }
    @Bean public Queue colaMarketing() { return new Queue(QUEUE_MARKETING, true); }

    // Binding SIN routing key: el fanout no la necesita
    @Bean public Binding bindingContabilidad() { return BindingBuilder.bind(colaContabilidad()).to(fanoutExchange()); }
    @Bean public Binding bindingMarketing() { return BindingBuilder.bind(colaMarketing()).to(fanoutExchange()); }
}
