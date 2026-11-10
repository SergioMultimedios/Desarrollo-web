package mx.edu.backendacademico.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.domain.EstatusAlumno;
import mx.edu.backendacademico.exception.OperacionAcademicaException;
import mx.edu.backendacademico.repository.jpa.AlumnoEntity;
import mx.edu.backendacademico.repository.jpa.AlumnoSpringDataRepository;
import mx.edu.backendacademico.repository.jpa.InscripcionRepository;
import mx.edu.backendacademico.repository.jpa.MateriaEntity;
import mx.edu.backendacademico.repository.jpa.MateriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InscripcionConcurrenciaTest {

    @Autowired
    private InscripcionService servicio;
    @Autowired
    private AlumnoSpringDataRepository alumnos;
    @Autowired
    private MateriaRepository materias;
    @Autowired
    private InscripcionRepository inscripciones;

    @Test
    void soloUnaAdmisionObtieneElUnicoLugar() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Long materiaId = materias.save(new MateriaEntity("LAB" + sufijo, "Cupo de un lugar", 1, 1)).getId();
        Long primero = alumnos.save(new AlumnoEntity(
            new Alumno(null, "P" + sufijo, "Alumno Primero", "primero@universidad.mx", EstatusAlumno.ACTIVO))).getId();
        Long segundo = alumnos.save(new AlumnoEntity(
            new Alumno(null, "S" + sufijo, "Alumno Segundo", "segundo@universidad.mx", EstatusAlumno.ACTIVO))).getId();

        CountDownLatch listos = new CountDownLatch(2);
        CountDownLatch disparo = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        Callable<String> admision1 = tarea(primero, materiaId, listos, disparo);
        Callable<String> admision2 = tarea(segundo, materiaId, listos, disparo);

        var futuro1 = pool.submit(admision1);
        var futuro2 = pool.submit(admision2);
        listos.await();
        disparo.countDown();

        List<String> resultados = List.of(futuro1.get(), futuro2.get());
        pool.shutdown();

        assertEquals(1, resultados.stream().filter(r -> r.equals("ADMITIDO")).count());
        assertEquals(1, resultados.stream().filter(r -> r.equals("SIN_CUPO")).count());
        assertEquals(1, inscripciones.countByMateriaId(materiaId));
    }

    private Callable<String> tarea(Long alumnoId, Long materiaId,
                                   CountDownLatch listos, CountDownLatch disparo) {
        return () -> {
            listos.countDown();
            disparo.await();
            try {
                servicio.inscribir(alumnoId, materiaId);
                return "ADMITIDO";
            } catch (OperacionAcademicaException ex) {
                return ex.getCodigo();
            }
        };
    }
}
