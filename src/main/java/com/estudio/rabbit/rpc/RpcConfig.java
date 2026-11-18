package com.estudio.rabbit.rpc;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RpcConfig {
    public static final String EXCHANGE_RPC = "exchange_rpc";
    public static final String QUEUE_RPC = "queue_rpc";
    public static final String ROUTING_RPC = "rutina.rpc";

    @Bean public DirectExchange rpcExchange() { return new DirectExchange(EXCHANGE_RPC); }
    @Bean public Queue rpcQueue() { return new Queue(QUEUE_RPC, true); }
    @Bean public Binding rpcBinding() { return BindingBuilder.bind(rpcQueue()).to(rpcExchange()).with(ROUTING_RPC); }
}
