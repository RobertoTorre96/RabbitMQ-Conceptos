package com.estudio.rabbit.limites;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LimitesConfig {
    public static final String QUEUE_PRIORIDAD = "queue_prioridad";
    public static final String QUEUE_LIMITADA = "queue_limitada";
    public static final String QUEUE_TTL = "queue_ttl";
    public static final String EXCHANGE_LIMITES_DLX = "exchange_limites_dlx";
    public static final String QUEUE_EXPIRADOS = "queue_ttl_expirados";

    @Bean
    public Queue limitesPrioridadQueue() {
        return QueueBuilder.durable(QUEUE_PRIORIDAD)
                .maxPriority(5) // prioridades 0..5: usá pocas, cada nivel cuesta
                .build();
    }

    @Bean
    public Queue limitesLimitadaQueue() {
        return QueueBuilder.durable(QUEUE_LIMITADA)
                .maxLength(5)                                      // máximo 5 mensajes
                .overflow(QueueBuilder.Overflow.rejectPublish)     // el 6.º se rechaza (NACK al productor)
                .build();
    }

    // TTL de cola + DLX: lo que no se consume en 10 s pasa a queue_ttl_expirados
    @Bean
    public Queue limitesTtlQueue() {
        return QueueBuilder.durable(QUEUE_TTL)
                .ttl(10_000)
                .withArgument("x-dead-letter-exchange", EXCHANGE_LIMITES_DLX)
                .withArgument("x-dead-letter-routing-key", "expirado")
                .build();
    }

    @Bean public DirectExchange limitesDlxExchange() { return new DirectExchange(EXCHANGE_LIMITES_DLX); }
    @Bean public Queue limitesExpiradosQueue() { return new Queue(QUEUE_EXPIRADOS, true); }
    @Bean public Binding limitesExpiradosBinding() {
        return BindingBuilder.bind(limitesExpiradosQueue()).to(limitesDlxExchange()).with("expirado");
    }
}
