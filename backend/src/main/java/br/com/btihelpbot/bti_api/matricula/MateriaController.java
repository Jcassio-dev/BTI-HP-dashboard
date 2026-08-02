package br.com.btihelpbot.bti_api.matricula;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/materia")
public class MateriaController {

    private final ComponenteRepository repository;

    public MateriaController(ComponenteRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MateriaDTO> porId(@PathVariable Long id) {
        return repository.findById(id)
                .map(MateriaDTO::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
