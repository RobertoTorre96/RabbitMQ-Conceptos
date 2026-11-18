package com.estudio.rabbit.dlq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DlqConfig {
    public static final String EXCHANGE_NORMAL = "exchange_normal";
    public static final String QUEUE_NORMAL = "queue_normal";
    public static final String ROUTING_NORMAL = "rutina.normal";
    public static final String EXCHANGE_DLX = "exchange_dlx";
    public static final String QUEUE_DLQ = "queue_dlq";
    public static final String ROUTING_DLQ = "rutina.dlq";

    // La cola de rescate es una cola completamente normal
    @Bean public Queue dlqQueue() { return QueueBuilder.durable(QUEUE_DLQ).build(); }

    // AQUÍ ESTÁ LA MAGIA: argumentos que conectan la cola normal con el rescate
    @Bean
    public Queue normalQueue() {
        return QueueBuilder.durable(QUEUE_NORMAL)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)     // si muere, va a este exchange
                .withArgument("x-dead-letter-routing-key", ROUTING_DLQ)   // con esta etiqueta
                .build();
    }

    @Bean public DirectExchange normalExchange() { return new DirectExchange(EXCHANGE_NORMAL); }
    @Bean public DirectExchange dlxExchange() { return new DirectExchange(EXCHANGE_DLX); }

    @Bean public Binding normalBinding() { return BindingBuilder.bind(normalQueue()).to(normalExchange()).with(ROUTING_NORMAL); }
    @Bean public Binding dlqBinding() { return BindingBuilder.bind(dlqQueue()).to(dlxExchange()).with(ROUTING_DLQ); }
}
