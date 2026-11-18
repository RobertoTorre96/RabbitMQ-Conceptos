package com.estudio.rabbit.concurrency;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConcurrencyConsumidor {

    // concurrency = "min-max" (mínimo de hilos - máximo de hilos)
    @RabbitListener(queues = ConcurrencyConfig.QUEUE_CONCURRENCY, concurrency = "3-5", ackMode = "AUTO")
    public void trabajadorClonado(String tarea) throws InterruptedException {
        String nombreHilo = Thread.currentThread().getName();
        System.out.println("  [" + nombreHilo + "] Iniciando: " + tarea);
        Thread.sleep(2000); // tarea pesada de 2 segundos
        System.out.println("  [" + nombreHilo + "] Terminó: " + tarea);
    }
}
