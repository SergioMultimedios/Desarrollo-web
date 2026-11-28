package mx.edu.backendacademico.repository.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotencia")
public class IdempotenciaEntity {
    @Id
    private String clave;
    private String huella;
    @Column(name = "alumno_id")
    private Long alumnoId;
    private LocalDateTime creado;

    protected IdempotenciaEntity() { }

    public IdempotenciaEntity(String clave, String huella, LocalDateTime creado) {
        this.clave = clave;
        this.huella = huella;
        this.creado = creado;
    }

    public String getClave() { return clave; }
    public String getHuella() { return huella; }
    public Long getAlumnoId() { return alumnoId; }
    public void setAlumnoId(Long alumnoId) { this.alumnoId = alumnoId; }
}
