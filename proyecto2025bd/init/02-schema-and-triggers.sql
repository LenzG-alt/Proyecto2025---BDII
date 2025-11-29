-- Conéctate a la base correcta
\connect sistema_tickets_db

-- Ahora crea todo
CREATE TABLE tecnicos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE tickets (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'abierto',
    prioridad INTEGER NOT NULL DEFAULT 3,
    creado_en TIMESTAMP NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE asignaciones_tickets (
    id SERIAL PRIMARY KEY,
    id_ticket INT NOT NULL REFERENCES tickets(id),
    id_tecnico INT NOT NULL REFERENCES tecnicos(id),
    asignado_en TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(id_ticket)
);

CREATE TABLE auditoria_tickets (
    id SERIAL PRIMARY KEY,
    id_ticket INT REFERENCES tickets(id),
    estado_anterior VARCHAR(20),
    estado_nuevo VARCHAR(20),
    cambiado_en TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_estado_prioridad_fecha_ticket ON tickets (estado, prioridad, creado_en);

CREATE OR REPLACE FUNCTION registrar_cambio_estado_ticket()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.estado <> OLD.estado THEN
        INSERT INTO auditoria_tickets(id_ticket, estado_anterior, estado_nuevo)
        VALUES (OLD.id, OLD.estado, NEW.estado);
    END IF;
    NEW.actualizado_en = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_cambio_estado_ticket
BEFORE UPDATE ON tickets
FOR EACH ROW
EXECUTE FUNCTION registrar_cambio_estado_ticket();

CREATE OR REPLACE FUNCTION asignar_ticket(p_id_ticket INT, p_id_tecnico INT)
RETURNS BOOLEAN AS $$
DECLARE
    t_estado VARCHAR(20);
BEGIN
    SELECT estado INTO t_estado
    FROM tickets
    WHERE id = p_id_ticket
    FOR UPDATE;
    IF t_estado <> 'abierto' THEN
        RETURN FALSE;
    END IF;
    INSERT INTO asignaciones_tickets(id_ticket, id_tecnico)
    VALUES (p_id_ticket, p_id_tecnico);
    UPDATE tickets SET estado = 'asignado' WHERE id = p_id_ticket;
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
