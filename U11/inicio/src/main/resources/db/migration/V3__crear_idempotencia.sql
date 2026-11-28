CREATE TABLE idempotencia (
    clave VARCHAR(80) PRIMARY KEY,
    huella VARCHAR(64) NOT NULL,
    alumno_id BIGINT,
    creado TIMESTAMP NOT NULL,
    CONSTRAINT fk_idempotencia_alumno FOREIGN KEY (alumno_id) REFERENCES alumnos(id)
);
