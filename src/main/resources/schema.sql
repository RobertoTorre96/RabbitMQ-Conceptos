-- cap. 13 (idempotencia)
CREATE TABLE IF NOT EXISTS mensajes_procesados (
  message_id   VARCHAR(100) PRIMARY KEY,
  procesado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- el "efecto de negocio" del cap. 13: un cobro por mensaje
CREATE TABLE IF NOT EXISTS cobros (
  id         BIGSERIAL PRIMARY KEY,
  message_id VARCHAR(100) NOT NULL,
  texto      VARCHAR(200),
  creado     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- cap. 16 (outbox)
CREATE TABLE IF NOT EXISTS pedidos (
  id       INT PRIMARY KEY,
  producto VARCHAR(100) NOT NULL,
  precio   DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS outbox (
  id            VARCHAR(36) PRIMARY KEY,      -- también es el messageId (cap. 13)
  exchange_name VARCHAR(100) NOT NULL,
  routing_key   VARCHAR(100) NOT NULL,
  payload       TEXT NOT NULL,                -- el JSON del evento
  creado        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  enviado       BOOLEAN NOT NULL DEFAULT FALSE
);
