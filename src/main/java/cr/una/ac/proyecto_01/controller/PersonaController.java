package cr.una.ac.proyecto_01.controller;


import cr.una.ac.proyecto_01.entity.Persona;
import cr.una.ac.proyecto_01.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api")
public class PersonaController {

    @Autowired
    PersonaRepository personaRepository;

    @GetMapping("/persona")
    ResponseEntity <List<Persona>> getPersonas(){
        try{
            return ResponseEntity.ok(personaRepository.findAll());
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }

    }
    @PostMapping("/persona")
     ResponseEntity<Persona> savePersona(@RequestBody Persona persona){
        try {
            return ResponseEntity.ok(personaRepository.save(persona));
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
    @PutMapping ("persona")
    ResponseEntity<Persona> updatePersona(@RequestBody  Persona persona){
        try {
            return ResponseEntity.ok(personaRepository.save(persona));
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }

    }
    @DeleteMapping("/persona/{id}")
    ResponseEntity<Void> deletePersona(@PathVariable("id") Long id ){
        try {
            personaRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("/persona/{id}")
    ResponseEntity<Persona> getPersona(@PathVariable("id") Long id) {
        try {
            Optional<Persona> optional = personaRepository.findById(id);
            return optional.map((persona)->ResponseEntity.ok(persona))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }


    }

}
