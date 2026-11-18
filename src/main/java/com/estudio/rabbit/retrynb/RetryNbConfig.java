package com.estudio.rabbit.retrynb;

import java.util.ArrayList;
import java.util.List;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryNbConfig {
    public static final String EXCHANGE_MAIN = "exchange_retrynb";
    public static final String QUEUE_MAIN = "queue_retrynb";
    public static final String RK_MAIN = "rutina.retrynb";
    public static final String EXCHANGE_WAIT = "exchange_retrynb_wait";
    public static final String EXCHANGE_PARKING = "exchange_retrynb_parking";
    public static final String QUEUE_PARKING = "queue_retrynb_parking";

    // esperas en ms (en producción: 5 s, 30 s, 5 min)
    public static final int[] ESPERAS_MS = {2_000, 5_000, 10_000};

    @Bean public DirectExchange retryNbMainExchange() { return new DirectExchange(EXCHANGE_MAIN); }
    @Bean public DirectExchange retryNbWaitExchange() { return new DirectExchange(EXCHANGE_WAIT); }
    @Bean public DirectExchange retryNbParkingExchange() { return new DirectExchange(EXCHANGE_PARKING); }

    @Bean public Queue retryNbMainQueue() { return new Queue(QUEUE_MAIN, true); }
    @Bean public Queue retryNbParkingQueue() { return new Queue(QUEUE_PARKING, true); }

    @Bean public Binding retryNbMainBinding() {
        return BindingBuilder.bind(retryNbMainQueue()).to(retryNbMainExchange()).with(RK_MAIN);
    }

    @Bean public Binding retryNbParkingBinding() {
        return BindingBuilder.bind(retryNbParkingQueue()).to(retryNbParkingExchange()).with("parking");
    }

    // Una cola de espera por cada duración. TODAS devuelven el mensaje a la principal.
    @Bean
    public Declarables retryNbColasDeEspera() {
        List<Declarable> items = new ArrayList<>();
        for (int i = 0; i < ESPERAS_MS.length; i++) {
            Queue espera = QueueBuilder.durable("queue_retrynb_wait_" + (i + 1))
                    .ttl(ESPERAS_MS[i])                                        // TTL de COLA, no de mensaje
                    .withArgument("x-dead-letter-exchange", EXCHANGE_MAIN)     // al vencer, vuelve...
                    .withArgument("x-dead-letter-routing-key", RK_MAIN)        // ...a la principal
                    .build();
            items.add(espera);
            items.add(BindingBuilder.bind(espera).to(retryNbWaitExchange()).with("wait." + (i + 1)));
        }
        return new Declarables(items);
    }
}
