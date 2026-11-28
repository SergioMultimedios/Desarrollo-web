package mx.edu.backendacademico.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.repository.jpa.IdempotenciaEntity;
import mx.edu.backendacademico.repository.jpa.IdempotenciaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class IdempotenciaRegistro {
    private final AlumnoService alumnos;
    private final IdempotenciaRepository claves;
    private final Clock reloj;

    public IdempotenciaRegistro(AlumnoService alumnos, IdempotenciaRepository claves, Clock reloj) {
        this.alumnos = alumnos;
        this.claves = claves;
        this.reloj = reloj;
    }

    @Transactional
    public Alumno reservarYCrear(String clave, String huella, String matricula, String nombre, String correo) {
        // La reserva va primero y con flush: ahi se serializa la carrera por la llave primaria.
        var registro = claves.saveAndFlush(new IdempotenciaEntity(clave, huella, LocalDateTime.now(reloj)));
        var alumno = alumnos.crear(matricula, nombre, correo);
        registro.setAlumnoId(alumno.id());
        claves.saveAndFlush(registro);
        return alumno;
    }

    @Transactional(readOnly = true)
    public Optional<IdempotenciaEntity> buscar(String clave) {
        return claves.findById(clave);
    }

    @Transactional(readOnly = true)
    public Alumno alumnoDe(IdempotenciaEntity registro) {
        return alumnos.buscar(registro.getAlumnoId());
    }
}
