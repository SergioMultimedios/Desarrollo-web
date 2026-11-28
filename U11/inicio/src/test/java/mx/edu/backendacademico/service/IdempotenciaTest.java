package mx.edu.backendacademico.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import mx.edu.backendacademico.exception.OperacionAcademicaException;
import mx.edu.backendacademico.repository.jpa.AlumnoSpringDataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class IdempotenciaTest {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired AlumnoIdempotenteService servicio;
    @Autowired AlumnoSpringDataRepository alumnos;

    @Test void dosSolicitudesSimultaneasConLaMismaClaveCreanUnAlumno() throws Exception {
        String clave = "K-" + UUID.randomUUID();
        String matricula = "ID" + clave.substring(2, 10).toUpperCase();

        CountDownLatch listos = new CountDownLatch(2);
        CountDownLatch disparo = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Callable<Long> tarea = () -> {
            listos.countDown();
            disparo.await();
            return servicio.procesar(clave, matricula, "Ada Lovelace", "ada@u.mx").alumno().id();
        };

        var primera = pool.submit(tarea);
        var segunda = pool.submit(tarea);
        listos.await();
        disparo.countDown();
        List<Long> ids = List.of(primera.get(), segunda.get());
        pool.shutdown();

        assertEquals(ids.get(0), ids.get(1));
        assertEquals(1, alumnos.findAll().stream()
            .filter(a -> a.toDomain().matricula().equals(matricula)).count());
    }

    @Test void laMismaClaveConOtroContenidoSeRechaza() {
        String clave = "K-" + UUID.randomUUID();
        String matricula = "ID" + clave.substring(2, 10).toUpperCase();
        servicio.procesar(clave, matricula, "Ada Lovelace", "ada@u.mx");

        var conflicto = assertThrows(OperacionAcademicaException.class, () ->
            servicio.procesar(clave, matricula, "Otro Nombre", "otro@u.mx"));
        assertEquals("CLAVE_REUTILIZADA", conflicto.getCodigo());
    }
}
