package com.estudio.rabbit.headers;

import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

@Component
public class HeadersConsumidor {

    // Recibe el cuerpo (String) y TODAS las cabeceras (Map)
    @RabbitListener(queues = HeadersConfig.QUEUE_REPORTES, ackMode = "AUTO")
    public void procesarReporte(String reporte, @Headers Map<String, Object> cabeceras) {
        System.out.println("  [Archivo] Recibido: " + reporte);
        System.out.println("  [Cabeceras detectadas]: " + cabeceras);
    }
}
