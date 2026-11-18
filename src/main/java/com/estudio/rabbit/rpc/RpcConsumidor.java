package com.estudio.rabbit.rpc;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RpcConsumidor {

    // IMPORTANTE: el método RETORNA un String en lugar de ser void. errorHandler = cap. 15 del Vol. 2.
    @RabbitListener(queues = RpcConfig.QUEUE_RPC, ackMode = "AUTO", errorHandler = "manejadorRpc")
    public String procesarCalculoComplejo(String operacion) throws InterruptedException {
        System.out.println("  [Servidor RPC] Recibida petición de cálculo: " + operacion);
        if (operacion.startsWith("falla")) { // para probar el ManejadorRpc: /rpc?operacion=falla
            throw new IllegalStateException("Operación no soportada");
        }
        Thread.sleep(3000); // procesamiento pesado (IA, OCR...)
        return "RESULTADO_PROCESADO_DE_[" + operacion + "]"; // Spring lo envía como respuesta
    }
}
