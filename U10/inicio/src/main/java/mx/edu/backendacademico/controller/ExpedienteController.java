package mx.edu.backendacademico.controller;

import mx.edu.backendacademico.dto.AlumnoMapper;
import mx.edu.backendacademico.dto.AlumnoResponse;
import mx.edu.backendacademico.service.AlumnoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/alumnos")
public class ExpedienteController {
    private final AlumnoService service;

    public ExpedienteController(AlumnoService service) { this.service = service; }

    @GetMapping("/{id}/expediente")
    public AlumnoResponse expediente(@PathVariable Long id, Authentication auth) {
        var alumno = service.buscar(id);
        if (!esAdmin(auth) && !alumno.matricula().equalsIgnoreCase(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El expediente pertenece a otra persona");
        }
        return AlumnoMapper.toResponse(alumno);
    }

    private boolean esAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
            .anyMatch(permiso -> permiso.getAuthority().equals("ROLE_ADMIN"));
    }
}
