package mx.edu.backendacademico.controller;

import jakarta.validation.Valid;
import java.net.URI;
import mx.edu.backendacademico.dto.AlumnoMapper;
import mx.edu.backendacademico.dto.AlumnoResponse;
import mx.edu.backendacademico.dto.CrearAlumnoRequest;
import mx.edu.backendacademico.service.AlumnoIdempotenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alumnos/idempotentes")
public class AlumnoIdempotenteController {
    private final AlumnoIdempotenteService service;

    public AlumnoIdempotenteController(AlumnoIdempotenteService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<AlumnoResponse> crear(@RequestHeader("Idempotency-Key") String clave,
                                                @RequestBody @Valid CrearAlumnoRequest request) {
        var resultado = service.procesar(clave, request.matricula(), request.nombre(), request.correo());
        // Una repeticion con la misma huella reproduce el 201 y el Location originales.
        return ResponseEntity.created(URI.create("/api/v1/alumnos/" + resultado.alumno().id()))
            .body(AlumnoMapper.toResponse(resultado.alumno()));
    }
}
