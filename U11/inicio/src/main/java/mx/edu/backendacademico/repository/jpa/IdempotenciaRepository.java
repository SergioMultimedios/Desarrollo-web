package mx.edu.backendacademico.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotenciaRepository extends JpaRepository<IdempotenciaEntity, String> {
}
