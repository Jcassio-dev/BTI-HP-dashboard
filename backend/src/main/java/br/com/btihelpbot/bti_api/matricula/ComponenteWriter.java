package br.com.btihelpbot.bti_api.matricula;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ComponenteWriter {

    private final ComponenteRepository repository;

    public ComponenteWriter(ComponenteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void replaceAll(List<Componente> componentes) {
        repository.deleteAllInBatch();
        repository.saveAll(componentes);
    }
}
