package com.estudio.rabbit.headers;

import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HeadersConfig {
    public static final String EXCHANGE_HEADERS = "exchange_headers";
    public static final String QUEUE_REPORTES = "queue_reportes";

    @Bean public HeadersExchange headersExchange() { return new HeadersExchange(EXCHANGE_HEADERS); }
    @Bean public Queue reportesQueue() { return new Queue(QUEUE_REPORTES, true); }

    @Bean
    public Binding bindingHeaders() {
        Map<String, Object> reglas = new HashMap<>();
        reglas.put("formato", "pdf");
        reglas.put("departamento", "ventas");
        // whereAll = debe cumplir TODAS (AND) | whereAny = AL MENOS UNA (OR)
        return BindingBuilder.bind(reportesQueue()).to(headersExchange()).whereAll(reglas).match();
    }
}
