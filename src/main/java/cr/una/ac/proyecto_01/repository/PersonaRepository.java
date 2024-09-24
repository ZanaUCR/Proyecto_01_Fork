package cr.una.ac.proyecto_01.repository;


import cr.una.ac.proyecto_01.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<Persona, Long> {


}
