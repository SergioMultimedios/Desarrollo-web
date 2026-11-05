ALTER TABLE alumnos
    ADD CONSTRAINT ck_nombre_no_vacio CHECK (LENGTH(TRIM(nombre)) > 0);
