package com.estudio.rabbit.json;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfig {
    public static final String EXCHANGE_JSON = "exchange_json";
    public static final String QUEUE_JSON = "queue_json";
    public static final String ROUTING_JSON = "rutina.json";

    // EL MOTOR DE TRADUCCIÓN: Objetos <-> JSON. OJO: es GLOBAL (afecta a todos los templates y listeners).
    @Bean
    public MessageConverter conversorJson() {
        return new JacksonJsonMessageConverter();
    }

    @Bean public DirectExchange jsonExchange() { return new DirectExchange(EXCHANGE_JSON); }
    @Bean public Queue jsonQueue() { return new Queue(QUEUE_JSON, true); }
    @Bean public Binding jsonBinding() {
        return BindingBuilder.bind(jsonQueue()).to(jsonExchange()).with(ROUTING_JSON);
    }
}
