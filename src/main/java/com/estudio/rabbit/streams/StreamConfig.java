package com.estudio.rabbit.streams;

import java.util.Map;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StreamConfig {
    public static final String STREAM_EVENTOS = "stream_eventos";

    @Bean
    public Queue streamEventos() {
        return QueueBuilder.durable(STREAM_EVENTOS)
                .stream()                                           // x-queue-type = stream
                .withArgument("x-max-length-bytes", 100_000_000L)   // retención: ~100 MB...
                .withArgument("x-max-age", "7D")                    // ...o 7 días
                .build();
    }

    // fábrica propia: los streams exigen ACK manual y un prefetch definido
    @Bean
    public SimpleRabbitListenerContainerFactory streamFactory(ConnectionFactory cf) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(10);
        // los argumentos del consumidor van en el contenedor; el customizer te da acceso a cada uno que crea la fábrica
        factory.setContainerCustomizer(container ->
                container.setConsumerArguments(Map.of("x-stream-offset", "first"))); // desde el primer mensaje ("next" = solo nuevos)
        return factory;
    }
}
