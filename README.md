# springboot-rabbitmq-vol2

Proyecto de práctica de **RabbitMQ con Java y Spring Boot**. Junta en un solo lugar los temas de las dos guías en PDF:

- **Vol. 1:** los 12 patrones de mensajería (exchanges, JSON, confirms, retry, DLQ, QoS, delayed, concurrency, RPC).
- **Vol. 2:** los 11 temas de nivel producción (idempotencia, reintentos no bloqueantes, outbox, quorum, streams, seguridad, clúster, monitoreo, pruebas).

Cada tema vive en su propio paquete Java y se prueba con un endpoint en el navegador. Las explicaciones largas están en los PDF: acá hay un resumen de cada tema, cómo probarlo y qué resultado esperar.

**Stack:** Java 17 · Spring Boot 4.1.1 · Spring AMQP · PostgreSQL · RabbitMQ 4 · Docker Compose · Prometheus y Grafana.

> **Sobre los resultados esperados:** están escritos a partir del código del proyecto. Si en tu máquina algo da distinto, es un dato útil: copiá la consola y lo revisamos.

---

## Índice

1. [Cómo levantar todo](#levantar)
2. [Cómo probar](#probar)
3. [Estructura del proyecto](#estructura)
4. [Vol. 1: los 12 patrones](#vol1)
   - [B1/B2 Básico y ACK](#v1-base) · [1 fanout](#v1-fanout) · [2 direct](#v1-direct) · [3 topic](#v1-topic) · [4 headers](#v1-headers) · [5 json](#v1-json)
   - [6 confirms](#v1-confirms) · [7 retry](#v1-retry) · [8 dlq](#v1-dlq) · [9 qos](#v1-qos) · [10 delayed](#v1-delayed) · [11 concurrency](#v1-concurrency) · [12 rpc](#v1-rpc)
5. [Vol. 2: los 11 temas](#vol2)
   - [13 idempotencia](#v2-idem) · [14 retry no bloqueante](#v2-retrynb) · [15 errores](#v2-errores) · [16 outbox](#v2-outbox) · [17 quorum](#v2-quorum) · [18 streams](#v2-streams)
   - [19 límites](#v2-limites) · [20 seguridad](#v2-seguridad) · [21 clúster](#v2-cluster) · [22 monitoreo](#v2-monitoreo) · [23 pruebas](#v2-pruebas)
6. [Cosas que se cruzan entre capítulos](#cruces)
7. [Problemas comunes](#problemas)

---

<a id="levantar"></a>
## 1. Cómo levantar todo

Un solo `docker-compose.yml` levanta toda la infraestructura. La app Spring **no** está en el compose: la corrés vos desde IntelliJ.

**Requisitos:** Docker Desktop abierto, JDK 17 o superior, IntelliJ (o Maven).

**Paso 1: la infraestructura.** Parado en la carpeta del proyecto (donde está `docker-compose.yml`), en PowerShell o Git Bash (es igual en las dos):

```
docker compose up -d
docker compose ps -a
```

La primera vez tarda porque baja las imágenes. El resultado esperado de `ps -a`:

- `rabbit` y `postgres` en `running (healthy)`.
- `prometheus`, `grafana`, `rabbit1`, `rabbit2` y `rabbit3` en `running`.
- `certs` en `Exited (0)`: está bien, genera los certificados TLS una vez y termina.

**Paso 2: la app.** En IntelliJ abrí la carpeta del proyecto, esperá a que Maven termine de descargar y tocá el ▶ de `RabbitEstudioApplication`. También podés usar `mvn spring-boot:run`. Resultado esperado en la consola: `Tomcat started on port 8080` y `Started RabbitEstudioApplication`.

### Qué levanta cada servicio

| Servicio | Para qué | En tu máquina |
|----------|----------|---------------|
| `rabbit` | Broker principal (caps. 1 a 20 y 22) | AMQP `5672` · TLS `5671` · panel http://localhost:15672 (guest / guest) · métricas `15692` |
| `postgres` | Base del outbox y la idempotencia (caps. 13 y 16) | `localhost:5433`, base `estudio`, usuario `estudio`, clave `estudio` |
| `certs` | Genera los certificados TLS (cap. 20); corre y termina | - |
| `prometheus` | Guarda métricas en el tiempo (cap. 22) | http://localhost:9090 |
| `grafana` | Dibuja las métricas (cap. 22) | http://localhost:3000 (admin / admin) |
| `rabbit1`, `rabbit2`, `rabbit3` | Clúster de 3 nodos (cap. 21), independiente de `rabbit` | AMQP `5682` a `5684` · paneles `15682` a `15684` |

Postgres usa el puerto **5433** en tu máquina (y no el 5432) para no chocar con un Postgres que ya tengas instalado. Para mirarlo con DBeaver: `localhost:5433`, base `estudio`, usuario `estudio`, clave `estudio`. Las tablas las crea `schema.sql` al arrancar la app.

### Comandos útiles (PowerShell o Git Bash)

| Qué | Comando |
|-----|---------|
| Ver el estado | `docker compose ps -a` |
| Parar sin borrar nada | `docker compose stop` |
| Volver a arrancar | `docker compose start` |
| Apagar y borrar contenedores (los datos de Postgres quedan) | `docker compose down` |
| Empezar de cero, borrando también la base | `docker compose down -v` |
| Levantar solo lo liviano (caps. 1 a 19) | `docker compose up -d rabbit postgres` |
| Ver los logs de un servicio | `docker compose logs rabbit` |

Los datos de Postgres persisten en un volumen: sobreviven aunque reinicies la app o los contenedores.

---

<a id="probar"></a>
## 2. Cómo probar

1. Con la app corriendo, abrí los endpoints en el navegador: `http://localhost:8080/...`.
2. Lo que **devuelve el navegador** es solo un texto de confirmación. Lo interesante pasa en la **consola de IntelliJ**, donde cada consumidor imprime lo que recibe.
3. Para ver colas y mensajes, usá el panel de RabbitMQ: http://localhost:15672 → pestaña **Queues and Streams**. Para espiar un mensaje sin consumirlo: abrí la cola → **Get messages** → *Ack mode* `Nack message requeue true`.

---

<a id="estructura"></a>
## 3. Estructura del proyecto

Cada tema sigue el patrón de tres piezas del Vol. 1: **Config** (exchanges, colas y bindings), **Productor** (el endpoint) y **Consumidor(es)** (los `@RabbitListener`).

```
src/main/java/com/estudio/rabbit/
  base/  fanout/  direct/  topic/  headers/  json/  confirms/  retry/  dlq/  qos/  delayed/  concurrency/  rpc/   <- Vol. 1
  idem/  retrynb/  errores/  outbox/  quorum/  streams/  limites/                                              <- Vol. 2
src/main/resources/
  application.yaml          configuración general (confirms, ACK manual, prefetch, retry, base, Actuator)
  application-cluster.yaml  perfil del cap. 21
  application-seguridad.yaml perfil del cap. 20 (usuario con permisos mínimos)
  application-tls.yaml      perfil del cap. 20 (conexión cifrada)
  schema.sql                tablas de Postgres (idempotencia y outbox)
docker-compose.yml          toda la infraestructura
docker/                     configuración de Prometheus, Grafana, TLS, clúster y certificados
scripts/                    seguridad.ps1 (PowerShell) y seguridad.sh (Git Bash)
src/test/                   test unitario y tests de integración con Testcontainers
```

---

<a id="vol1"></a>
## 4. Vol. 1: los 12 patrones

<a id="v1-base"></a>
### B1/B2 · Básico y ACK
**Idea:** el productor deja un mensaje en una cola (usando el exchange por defecto) y un consumidor lo retira. Con ACK manual, RabbitMQ borra el mensaje recién cuando el consumidor confirma.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/enviar?texto=hola` | Publica "hola" en `queue_spring_basico` | Navegador: `Mensaje enviado a RabbitMQ: hola`. Consola: `[Consumidor Spring] Mensaje recibido: 'hola'` |

<a id="v1-fanout"></a>
### 1 · fanout
**Idea:** un exchange que copia el mensaje a todas las colas enlazadas, ignorando la routing key. Es difusión.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/fanout?texto=compra` | Publica una vez en `mi_exchange_fanout` | Consola: dos líneas con el mismo texto, `[Dpto. Contabilidad]` y `[Dpto. Marketing]` |

<a id="v1-direct"></a>
### 2 · direct
**Idea:** el mensaje entra a la cola solo si la routing key coincide exacta con la del binding (distingue mayúsculas).

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/direct?routingKey=info&texto=ok` | Publica con la clave `info` | Consola: `[Monitor INFO] ok` |
| `/direct?routingKey=error&texto=se cayó` | Publica con la clave `error` | Consola: `[Alerta ERROR] se cayó` |
| `/direct?routingKey=warning&texto=x` | Clave sin binding | Consola: `[Devolución] Sin cola para la ruta: 'warning'` (el mensaje no llega a ninguna cola) |

<a id="v1-topic"></a>
### 3 · topic
**Idea:** el binding es un patrón sobre una routing key con palabras separadas por puntos. `*` es exactamente una palabra y `#` es cero o más.

Bindings del proyecto: `usa.#` (cola de USA) y `*.deportes.*` (cola de deportes).

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/topic?routingKey=usa.deportes.nba&texto=gol` | Coincide con los dos patrones | Consola: `[Noticiero USA]` y `[Canal Deportes]` |
| `/topic?routingKey=mx.deportes.futbol&texto=gol` | Solo coincide con deportes | Consola: solo `[Canal Deportes]` |
| `/topic?routingKey=usa.deportes&texto=x` | Dos palabras: el patrón de deportes exige tres | Consola: solo `[Noticiero USA]` |

<a id="v1-headers"></a>
### 4 · headers
**Idea:** el exchange decide según las cabeceras del mensaje y no según la routing key. Acá el binding exige `formato=pdf` **y** `departamento=ventas`.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/headers/valido` | Envía con `formato=pdf` | Consola: `[Archivo] Recibido: Reporte_Mensual` y el mapa de cabeceras |
| `/headers/invalido` | Envía con `formato=excel` | Consola: `[Devolución]` con routing key vacía; no llega a la cola |

<a id="v1-json"></a>
### 5 · json
**Idea:** en vez de armar Strings a mano, se envía un objeto Java (`Pedido`) y un converter Jackson lo pasa a JSON y de vuelta. El converter es **global** para toda la app.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/json?producto=Laptop&precio=1500` | Publica un `Pedido` | Consola: `[Almacén] Nuevo pedido recibido: Pedido{id=..., producto='Laptop', precio=$1500.0}`. En el panel, el payload es JSON |

<a id="v1-confirms"></a>
### 6 · confirms
**Idea:** el broker le avisa al **productor** si recibió el mensaje (confirm) o si no encontró cola (return). Es lo contrario del ACK del consumidor. Los callbacks son globales.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/confirms/exito?texto=x` | Exchange y ruta correctos | Consola: `[Recibo] ¡Éxito! ...` y luego `[Confirms] consumidor recibió: x` |
| `/confirms/error-exchange?texto=x` | Exchange que no existe | Consola: error `NOT_FOUND` y `[Recibo] ERROR GRAVE` (NACK) |
| `/confirms/ruta-mala?texto=x` | Exchange real, routing key sin binding | Consola: `[Devolución]` **y también** `[Recibo] ¡Éxito!` (un mensaje devuelto igual recibe ACK) |

<a id="v1-retry"></a>
### 7 · retry
**Idea:** si un listener `AUTO` lanza excepción, Spring reintenta con espera creciente (3 intentos, esperas de 2 s y 4 s). Es en memoria y bloquea el hilo.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/retry?texto=hola` | Mensaje sin error | Consola: `[Obrero Retry] Procesando` y `[v] Éxito` |
| `/retry?texto=error` | Falla siempre | Consola: tres `Procesando` separados por ~2 s y ~4 s. **Ojo:** por el recuperador global del cap. 15, termina en `queue_errores` y no en `queue_retry_dlq` (ver [cruces](#cruces)) |

<a id="v1-dlq"></a>
### 8 · dlq (Dead Letter Queue)
**Idea:** si un mensaje muere (rechazado sin re-encolar, expirado o cola llena), RabbitMQ lo redirige a otra cola en vez de borrarlo.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/dlq?texto=hola` | Mensaje normal | Consola: `Intentando procesar` y fin |
| `/dlq?texto=error` | El consumidor lo rechaza con NACK sin re-encolar | Consola: `Falló ... Descartando` y luego `[Hospital DLQ] Mensaje rescatado`. La DLQ siempre se ve en 0 porque su consumidor la vacía al instante |

<a id="v1-qos"></a>
### 9 · qos (prefetch)
**Idea:** con `prefetch: 1`, RabbitMQ no entrega un mensaje nuevo hasta que se confirme el anterior. El trabajo se reparte según quién está libre, no por turnos.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/qos` | Envía 10 tareas a dos consumidores (uno lento de 2 s y uno rápido de 0,5 s) | Consola: el `[Rápido]` hace casi todas las tareas y el `[Lento]` solo un par |

<a id="v1-delayed"></a>
### 10 · delayed
**Idea:** mensajes programados con TTL más DLQ: el mensaje espera en una cola sin consumidor y, al vencer su tiempo, pasa a la cola de destino.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/delayed?segundos=10&texto=hola` | Deja el mensaje 10 s en la sala de espera | Consola: `[Delayed] Disparando a las HH:MM:SS` y 10 s después `[Consumidor] Mensaje recibido a las ...` |
| `/delayed?segundos=30&texto=A` y enseguida `/delayed?segundos=5&texto=B` | La "trampa" del TTL por mensaje | B no sale a los 5 s: queda detrás de A y sale cuando A vence (30 s) |

<a id="v1-concurrency"></a>
### 11 · concurrency
**Idea:** `concurrency = "3-5"` crea varios hilos consumidores sobre la misma cola (arranca con 3, puede subir a 5). Se pierde el orden global.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/concurrency` | Envía 15 tareas de 2 s | Consola: varios nombres de hilo trabajando a la vez; el total ronda 10 s en vez de 30 s. Con solo 15 tareas, lo normal es no ver el 4.º y 5.º hilo |

<a id="v1-rpc"></a>
### 12 · rpc
**Idea:** el cliente envía una petición y **espera** la respuesta. El método del listener devuelve un String y Spring lo publica como respuesta. Si nadie responde en 5 s, el cliente recibe `null`.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/rpc?operacion=2%2B2` | Pide un cálculo y espera | La pestaña queda cargando ~3 s y muestra `El servidor procesó tu operación. Resultado: RESULTADO_PROCESADO_DE_[2+2]` |

---

<a id="vol2"></a>
## 5. Vol. 2: los 11 temas

<a id="v2-idem"></a>
### 13 · Idempotencia
**Idea:** RabbitMQ entrega "al menos una vez", así que pueden llegar duplicados. Un consumidor idempotente registra el `messageId` en Postgres (con una clave primaria) y aplica el efecto una sola vez.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/idem?id=pago-42&texto=cobro` | Envía el mismo mensaje dos veces con el mismo id | Consola: un `COBRANDO` y un `Duplicado ignorado` |
| `/idem?id=pago-42&texto=cobro` (otra vez) | Repite el mismo id | Consola: ahora los dos son `Duplicado ignorado` (el id quedó guardado, incluso si reiniciás la app) |
| `/idem/estado` | Muestra el conteo | Texto con cobros en la base, cobros y duplicados ignorados |

<a id="v2-retrynb"></a>
### 14 · Reintentos no bloqueantes
**Idea:** en vez de dormir el hilo, el mensaje fallido espera en una cola con TTL (2, 5 y 10 s) y vuelve solo a la cola principal. El consumidor sigue atendiendo otros mensajes. Si se agotan los reintentos, va a una cola `parking`.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/retrynb?texto=hola` | Mensaje sin error | Consola: un solo intento |
| `/retrynb?texto=error` | Falla siempre | Consola: `intento 1`, +2 s `intento 2`, +5 s `intento 3`, +10 s `intento 4` (unos 17 s). Después queda 1 mensaje en `queue_retrynb_parking` con `x-intentos = 4` |
| `/retrynb?texto=hola` (mientras espera el de `error`) | Prueba que no se bloquea | Se procesa al instante |

<a id="v2-errores"></a>
### 15 · Manejo de errores
**Idea:** no todo error merece un reintento. Hay tres herramientas: rechazo explícito, `RepublishMessageRecoverer` (republica el mensaje con el stacktrace en cabeceras) y un `ErrorHandler` que puede responder un RPC con el error.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/errores?texto=ok-1` | Mensaje sano | Consola: `[Errores] OK: ok-1` |
| `/errores?texto=caido-1` | Error transitorio | Tres intentos (2 s y 4 s) y 1 mensaje en `queue_errores`. En el panel (Get messages) se ven las cabeceras `x-exception-message`, `x-exception-stacktrace`, `x-original-routingKey` |
| `/errores?texto=invalido-1` | Rechazo explícito | También pasa por los 3 intentos antes de rechazarse (ver [cruces](#cruces)) y termina en `queue_errores` |
| `/rpc?operacion=falla` | RPC que lanza excepción | `El servidor procesó tu operación. Resultado: ERROR: Operación no soportada`, de inmediato (sin el `null` a los 5 s) |

<a id="v2-outbox"></a>
### 16 · Outbox
**Idea:** guardar en la base y publicar en RabbitMQ no pueden compartir una transacción. El outbox guarda el evento en una tabla dentro de la misma transacción del negocio y un proceso aparte (el relay, cada 2 s) lo publica y lo marca como enviado cuando el broker confirma.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/outbox/pedido?producto=Laptop&precio=1500` | Guarda un pedido y su evento en Postgres | Navegador: `OK: pedido guardado + evento en la tabla outbox`. A los pocos segundos, consola: `[Almacén] Nuevo pedido recibido: Pedido{...}` |
| `/outbox/estado` | Cuenta los eventos | `pendientes=0 \| enviados=1` (con el broker andando) |

**Para practicarlo de verdad:**

1. Frená solo RabbitMQ: `docker stop rabbit`.
2. Abrí `/outbox/pedido?producto=Laptop&precio=1500`: responde OK igual.
3. En DBeaver: `SELECT id, enviado, creado FROM outbox ORDER BY creado;` → la fila está con `enviado = false`. En la consola aparece `[Outbox] Sin confirmar ...` cada 2 s.
4. Opcional: reiniciá la app con el broker apagado; la fila sigue en la base.
5. `docker start rabbit`: a los pocos segundos el relay publica, el consumidor imprime el pedido y la fila pasa a `enviado = true`.

<a id="v2-quorum"></a>
### 17 · Quorum queues
**Idea:** colas replicadas con consenso Raft: sobreviven a la caída de un nodo. Además traen `x-delivery-limit`, que corta el bucle infinito de un mensaje venenoso. Acá el límite es 3 y después va a una DLQ.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/quorum?texto=hola` | Mensaje sano | Consola: `[Quorum] entrega nº 1: hola` y ACK |
| `/quorum?texto=error` | Mensaje venenoso (NACK con re-encolado) | Consola: varias entregas (`nº 1`, `nº 2`, ...) y después el mensaje aparece en `queue_quorum_dlq` (la cantidad exacta puede variar en uno según la versión) |

En el panel, la cola `queue_quorum` muestra `Type: quorum`.

<a id="v2-streams"></a>
### 18 · Streams
**Idea:** un log de solo-agregar: los mensajes se conservan aunque se lean y cada consumidor elige desde dónde leer. Dos listeners leen todos los mensajes sin competir.

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/stream?texto=hola` | Publica un evento en `stream_eventos` | Consola: el evento aparece dos veces, `[Auditoría] offset N` y `[Analítica] recibió`. En el panel, el stream sigue mostrando el mensaje aunque ya fue confirmado |

Al reiniciar la app, los mensajes vuelven a aparecer porque se lee desde `first`. Para leer solo los nuevos, se cambia a `next` en `StreamConfig`.

<a id="v2-limites"></a>
### 19 · Límites: prioridad, TTL y tamaño
**Idea:** tres controles que se declaran como argumentos de la cola: prioridad (cuál sale antes), TTL (cuánto vive un mensaje) y límite de tamaño (qué pasa cuando se llena).

| Endpoint | Qué hace | Resultado esperado |
|----------|----------|--------------------|
| `/prioridad/llenar` | Envía 5 mensajes (el 4.º con prioridad 5) a una cola sin consumidor | `5 mensajes enviados. Ahora llamá /prioridad/vaciar` |
| `/prioridad/vaciar` | Los lee con `receive` | `Mensaje 4` sale primero, aunque llegó cuarto; después `Mensaje 1`, `2`, `3` y `5` |
| `/limite?cantidad=8` | Envía 8 mensajes a una cola limitada a 5 con `reject-publish` | Consola: 5 `[Recibo] ¡Éxito!` y 3 `[Recibo] ERROR GRAVE` (NACK). En el panel, la cola tiene 5 mensajes. Si repetís sin vaciarla, los 8 salen NACK |
| `/ttl?texto=oferta` | Envía a una cola con TTL de 10 s y sin consumidor | A los 10 s, consola: `[Limites] mensaje EXPIRADO por TTL: oferta` |

<a id="v2-seguridad"></a>
### 20 · Seguridad
**Idea:** RabbitMQ se protege en capas: usuario y clave, virtual host, permisos `configure` / `write` / `read` (expresiones regulares) y TLS. Un usuario por rol, con el mínimo permiso.

**Usuarios y permisos** (con el compose andando):

```
.\scripts\seguridad.ps1          # PowerShell
./scripts/seguridad.sh           # Git Bash
mvn spring-boot:run "-Dspring-boot.run.profiles=seguridad"     # PowerShell
```

El script crea el vhost `pedidos` y los usuarios `admin`, `app_productor` y `app_consumidor`. El perfil `seguridad` arranca la app como `app_productor`.

- **Resultado esperado:** la app falla al arrancar con `ACCESS_REFUSED`, porque declara colas y ese usuario no tiene `configure`. Es la seguridad funcionando.
- En el panel entrás con `admin` / `Cambiame-0` y ves los permisos en Admin → Users.

**TLS:** el broker ya escucha en `5671` con los certificados que generó el servicio `certs` (carpeta `docker/certs`). Solo falta arrancar la app con el perfil `tls`:

```
mvn spring-boot:run "-Dspring-boot.run.profiles=tls"           # PowerShell
```

Si no conecta, mirá `docker logs rabbit`. Para regenerar los certificados, borrá los `.pem` y `.key` de `docker/certs` y corré `docker compose up -d`.

<a id="v2-cluster"></a>
### 21 · Clúster
**Idea:** varios nodos RabbitMQ que comparten usuarios, exchanges y bindings. Para que los mensajes también sobrevivan a la caída de un nodo, las colas tienen que ser quorum. Con 2 nodos no hay mayoría: el mínimo útil es 3.

Los nodos `rabbit1`, `rabbit2` y `rabbit3` ya están levantados. Comandos (PowerShell o Git Bash):

```
docker compose exec rabbit1 rabbitmqctl cluster_status
docker compose exec rabbit1 rabbitmq-queues quorum_status queue_quorum
docker compose stop rabbit1          # simula la caída del nodo 1
docker compose start rabbit1         # lo vuelve a levantar
mvn spring-boot:run "-Dspring-boot.run.profiles=cluster"      # PowerShell (con comillas)
mvn spring-boot:run -Dspring-boot.run.profiles=cluster         # Git Bash
```

- **Resultado esperado:** con el perfil `cluster` la app se conecta a los puertos 5682 a 5684. `/quorum?texto=hola` funciona y en http://localhost:15682 la cola `queue_quorum` muestra 3 miembros y un líder.
- Si frenás el nodo que era líder, otro toma el cargo y `/quorum?texto=otra` sigue funcionando (mirá el panel en `15683`).

<a id="v2-monitoreo"></a>
### 22 · Monitoreo
**Idea:** una cola que crece, consumidores que desaparecen o un disco lleno se notan en métricas y no en los logs. Se miran en tres lugares:

| Herramienta | Qué es | Cuándo usarla |
|-------------|--------|---------------|
| **Actuator** (dentro de tu app) | Endpoints que muestran la salud y las métricas de la app **ahora** | Saber si la app está viva y mirar un número puntual |
| **Prometheus** | Guarda las métricas en el tiempo (consulta cada 15 s a la app y a RabbitMQ) | Consultar historia y tendencias |
| **Grafana** | Dibuja las métricas de Prometheus en dashboards y permite armar alertas | Ver tendencias de un vistazo |

Camino de los datos: tu app (Actuator) y RabbitMQ (`:15692`) → Prometheus (guarda) → Grafana (dibuja).

| Endpoint o URL | Qué hace | Resultado esperado |
|----------------|----------|--------------------|
| `/actuator/health` | Estado de la app | `"status":"UP"` y un componente `rabbit` también en `UP` |
| `/actuator/metrics` | Lista de métricas de la app | Una lista de nombres de métricas |
| `/actuator/prometheus` | Métricas en formato Prometheus | Texto plano con muchas líneas (incluye `app_dlq_recibidos_total`) |
| http://localhost:9090 → Status → Targets | Fuentes que Prometheus consulta | `rabbitmq` y `rabbitmq-cluster` en `UP`; `app` en `UP` solo si la app está corriendo |
| http://localhost:9090 → Query: `rabbitmq_queue_messages_ready` | Mensajes esperando en colas | Un valor por broker; sube si hay colas sin consumidor |
| http://localhost:3000 (admin / admin) → Explore → Prometheus | Gráficos | La misma consulta, dibujada en el tiempo |

Para generar movimiento: `/limite?cantidad=8` (deja mensajes en Ready) o `/dlq?texto=error` (suma al contador `app_dlq_recibidos_total`).

Este proyecto **no incluye alertas** configuradas: tenés los gráficos, pero nada avisa solo si una cola se descontrola.

<a id="v2-pruebas"></a>
### 23 · Pruebas
**Idea:** dos niveles. Tests unitarios (sin broker) para la lógica pura y tests de integración con un RabbitMQ y un PostgreSQL reales en contenedores descartables (Testcontainers). Se espera con Awaitility, nunca con `Thread.sleep`.

```
mvn test
```

- **Resultado esperado:** `RetryNbRutasTest` (unitario) y los tests de integración `IdemIT`, `LimitesIT`, `QuorumIT` y `OutboxIT` en verde. Mientras corren, `docker ps` muestra contenedores temporales que desaparecen al terminar.
- Necesitan Docker corriendo y **no** usan los contenedores de tu compose: levantan los suyos.

---

<a id="cruces"></a>
## 6. Cosas que se cruzan entre capítulos

- **El recuperador del cap. 15 es global.** Con él activo, `/retry?texto=error` (cap. 7) ya no termina en `queue_retry_dlq` sino en `queue_errores`. Para ver el comportamiento original del cap. 7, comentá el `@Bean recuperador` en `ErroresConfig`.
- **El converter JSON es global** (cap. 5): los Strings de los demás paquetes también viajan como JSON. Los eventos del outbox (cap. 16) caen en `queue_json` y los recibe el mismo consumidor que `/json`.
- **Los callbacks de confirms y returns** (cap. 6) son globales: también se ven con `/direct`, `/headers`, `/limite` y el outbox.
- **`AmqpRejectAndDontRequeueException`** (cap. 15) pasa por los 3 reintentos del yaml antes de rechazarse, porque el reintento de Boot no distingue tipos de excepción.
- **Outbox e idempotencia van de a pares:** el outbox puede publicar dos veces si el relay cae justo antes de marcar el envío, y la idempotencia (cap. 13) es lo que lo hace inofensivo.

---

<a id="problemas"></a>
## 7. Problemas comunes

| Síntoma | Causa y solución |
|---------|------------------|
| `port is already allocated` al levantar Postgres | Ya tenés otro Postgres usando el puerto. Este proyecto usa el `5433`; si también está ocupado, cambiá el primer número de `"5433:5432"` en `docker-compose.yml` y el puerto en `application.yaml` |
| `PRECONDITION_FAILED` al arrancar la app | Una cola vieja con otros argumentos. Borrala desde el panel de RabbitMQ y reiniciá |
| La app no conecta a la base | Chequeá que `postgres` esté `healthy` con `docker compose ps -a` y que el puerto de `application.yaml` coincida con el del compose |
| `ACCESS_REFUSED` | Esperado con el perfil `seguridad`. Si aparece sin ese perfil, revisá el usuario del `application.yaml` (guest / guest) |
| `app` aparece `DOWN` en Prometheus | La app no está corriendo, o corre en otro puerto que el 8080 |
| El panel muestra `queue_dlq` siempre en 0 | Es normal: su consumidor la vacía al instante. Para ver el mensaje, comentá el listener de la DLQ |
| `rabbit` no arranca con TLS | Mirá `docker logs rabbit` y `docker compose logs certs` (permisos o rutas de los certificados) |
| Los tests de integración fallan al arrancar | Docker no está corriendo |
| La máquina anda justa de memoria | `docker compose up -d rabbit postgres` levanta solo lo necesario para los caps. 1 a 19 |

Para empezar de cero (borra también la base): `docker compose down -v` y volvé a levantar.