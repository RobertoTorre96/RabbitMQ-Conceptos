package com.estudio.rabbit.rpc;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.listener.ListenerExecutionFailedException;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.stereotype.Component;

@Component("manejadorRpc")
public class ManejadorRpc implements RabbitListenerErrorHandler {

    @Override
    public Object handleError(org.springframework.amqp.core.Message amqpMessage,
                              Channel channel,
                              org.springframework.messaging.Message<?> message,
                              ListenerExecutionFailedException exception) {
        // el valor que devolvemos viaja como RESPUESTA al cliente (replyTo)
        return "ERROR: " + exception.getCause().getMessage();
    }
}