package mx.edu.backendacademico.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import mx.edu.backendacademico.config.SecurityConfig;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.domain.EstatusAlumno;
import mx.edu.backendacademico.service.AlumnoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExpedienteController.class)
@Import(SecurityConfig.class)
class ExpedienteTest {
    @Autowired MockMvc mvc;
    @MockitoBean AlumnoService service;

    @BeforeEach
    void expedienteDeAlumno() {
        when(service.buscar(1L)).thenReturn(
            new Alumno(1L, "alumno", "Ada Lovelace", "ada@u.mx", EstatusAlumno.ACTIVO));
    }

    @Test void elPropietarioLeeSuExpediente() throws Exception {
        mvc.perform(get("/api/v1/alumnos/1/expediente").with(httpBasic("alumno", "alumno123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.matricula").value("ALUMNO"));
    }

    @Test void unUsuarioAjenoRecibe403() throws Exception {
        mvc.perform(get("/api/v1/alumnos/1/expediente").with(httpBasic("docente", "docente123")))
            .andExpect(status().isForbidden());
    }

    @Test void elAdministradorLeeCualquierExpediente() throws Exception {
        mvc.perform(get("/api/v1/alumnos/1/expediente").with(httpBasic("admin", "admin123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }
}
