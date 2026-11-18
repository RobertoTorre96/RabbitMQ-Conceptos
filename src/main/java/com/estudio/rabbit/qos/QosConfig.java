package com.estudio.rabbit.qos;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QosConfig {
    public static final String EXCHANGE_QOS = "exchange_qos";
    public static final String QUEUE_QOS = "queue_qos";
    public static final String ROUTING_QOS = "rutina.qos";

    @Bean public DirectExchange qosExchange() { return new DirectExchange(EXCHANGE_QOS); }
    @Bean public Queue qosQueue() { return new Queue(QUEUE_QOS, true); }
    @Bean public Binding qosBinding() { return BindingBuilder.bind(qosQueue()).to(qosExchange()).with(ROUTING_QOS); }
}
