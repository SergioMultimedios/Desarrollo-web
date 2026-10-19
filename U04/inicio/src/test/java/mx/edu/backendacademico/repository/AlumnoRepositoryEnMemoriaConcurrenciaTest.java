package mx.edu.backendacademico.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.domain.EstatusAlumno;
import mx.edu.backendacademico.exception.MatriculaDuplicadaException;
import org.junit.jupiter.api.Test;

class AlumnoRepositoryEnMemoriaConcurrenciaTest {

    @Test
    void dosTareasConLaMismaMatriculaProducenUnRegistroYUnConflicto() throws Exception {
        var repositorio = new AlumnoRepositoryEnMemoria();
        var listos = new CountDownLatch(2);
        var disparo = new CountDownLatch(1);
        var guardados = new AtomicInteger();
        var conflictos = new AtomicInteger();

        Callable<Void> intento = () -> {
            listos.countDown();
            disparo.await();
            try {
                repositorio.guardar(new Alumno(null, "A1001", "Ada Lovelace",
                                               "ada@example.com", EstatusAlumno.ACTIVO));
                guardados.incrementAndGet();
            } catch (MatriculaDuplicadaException e) {
                conflictos.incrementAndGet();
            }
            return null;
        };

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Void> primera = pool.submit(intento);
            Future<Void> segunda = pool.submit(intento);

            listos.await();
            disparo.countDown();

            primera.get();
            segunda.get();
        } finally {
            pool.shutdown();
        }

        assertEquals(1, guardados.get());
        assertEquals(1, conflictos.get());

        List<Alumno> almacenados = repositorio.buscarTodos();
        assertEquals(1, almacenados.size());
        assertEquals("A1001", almacenados.get(0).matricula());
        assertTrue(almacenados.get(0).id() > 0);
    }
}
