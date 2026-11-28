package mx.edu.backendacademico.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.exception.OperacionAcademicaException;
import mx.edu.backendacademico.repository.jpa.IdempotenciaEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class AlumnoIdempotenteService {
    private final IdempotenciaRegistro registro;

    public AlumnoIdempotenteService(IdempotenciaRegistro registro) {
        this.registro = registro;
    }

    // Sin @Transactional: la carrera se resuelve atrapando el conflicto ya confirmado.
    public Resultado procesar(String clave, String matricula, String nombre, String correo) {
        String huella = huella(matricula, nombre, correo);
        var previa = registro.buscar(clave);
        if (previa.isPresent()) {
            return new Resultado(reproducir(previa.get(), huella), false);
        }
        try {
            return new Resultado(registro.reservarYCrear(clave, huella, matricula, nombre, correo), true);
        } catch (DataIntegrityViolationException carrera) {
            var ganadora = registro.buscar(clave).orElseThrow(() -> carrera);
            return new Resultado(reproducir(ganadora, huella), false);
        }
    }

    private Alumno reproducir(IdempotenciaEntity previa, String huella) {
        if (!previa.getHuella().equals(huella)) {
            throw new OperacionAcademicaException("CLAVE_REUTILIZADA",
                "La clave de idempotencia ya se usó con otro contenido");
        }
        return registro.alumnoDe(previa);
    }

    private String huella(String matricula, String nombre, String correo) {
        String canonico = matricula.strip().toUpperCase(Locale.ROOT) + "|" + nombre.strip() + "|" + correo.strip();
        try {
            var resumen = MessageDigest.getInstance("SHA-256").digest(canonico.getBytes(StandardCharsets.UTF_8));
            var texto = new StringBuilder(resumen.length * 2);
            for (byte b : resumen) {
                texto.append(String.format("%02x", b));
            }
            return texto.toString();
        } catch (NoSuchAlgorithmException imposible) {
            throw new IllegalStateException("SHA-256 no disponible", imposible);
        }
    }

    public record Resultado(Alumno alumno, boolean creado) { }
}
