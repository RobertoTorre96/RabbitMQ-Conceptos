package com.estudio.rabbit.errores;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErroresConfig {
    public static final String QUEUE_TRABAJO = "queue_errores_trabajo";
    public static final String EXCHANGE_ERRORES = "exchange_errores";
    public static final String QUEUE_ERRORES = "queue_errores";

    @Bean public Queue erroresTrabajoQueue() { return new Queue(QUEUE_TRABAJO, true); }
    @Bean public DirectExchange erroresExchange() { return new DirectExchange(EXCHANGE_ERRORES); }
    @Bean public Queue erroresQueue() { return new Queue(QUEUE_ERRORES, true); }
    @Bean public Binding erroresBinding() {
        return BindingBuilder.bind(erroresQueue()).to(erroresExchange()).with("fallido");
    }

    // OJO: bean GLOBAL. Spring Boot lo usa cuando se agotan los reintentos de TODOS los listeners AUTO.
    @Bean
    public MessageRecoverer recuperador(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, EXCHANGE_ERRORES, "fallido");
    }
}
