package com.estudio.rabbit.quorum;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuorumConfig {
    public static final String QUEUE_QUORUM = "queue_quorum";
    public static final String EXCHANGE_QUORUM_DLX = "exchange_quorum_dlx";
    public static final String QUEUE_QUORUM_DLQ = "queue_quorum_dlq";

    @Bean
    public Queue quorumQueue() {
        return QueueBuilder.durable(QUEUE_QUORUM)
                .quorum()                                                        // x-queue-type = quorum
                .withArgument("x-delivery-limit", 3)                             // máx. re-entregas por mensaje
                .withArgument("x-dead-letter-exchange", EXCHANGE_QUORUM_DLX)     // al superarlo, va acá
                .withArgument("x-dead-letter-routing-key", "quorum.dlq")
                .build();
    }

    @Bean public DirectExchange quorumDlxExchange() { return new DirectExchange(EXCHANGE_QUORUM_DLX); }
    @Bean public Queue quorumDlqQueue() { return new Queue(QUEUE_QUORUM_DLQ, true); } // sin listener a propósito: así se pueden mirar
    @Bean public Binding quorumDlqBinding() {
        return BindingBuilder.bind(quorumDlqQueue()).to(quorumDlxExchange()).with("quorum.dlq");
    }
}
