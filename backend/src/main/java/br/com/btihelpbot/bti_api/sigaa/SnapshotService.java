package br.com.btihelpbot.bti_api.sigaa;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

/**
 * Guarda a foto dos dados do aluno, cifrada. Nao guarda sessao nem senha.
 * O aluno atualiza reconectando; ate la, os comandos leem daqui, com a data da coleta.
 */
public class SnapshotService {

    private final SnapshotSigaaRepository repository;
    private final CofreSessao cofre;
    private final ObjectMapper mapper;

    public SnapshotService(SnapshotSigaaRepository repository, CofreSessao cofre, ObjectMapper mapper) {
        this.repository = repository;
        this.cofre = cofre;
        this.mapper = mapper;
    }

    public void salvar(String jid, DadosSigaa dados) {
        try {
            SnapshotSigaa s = repository.findById(jid).orElseGet(SnapshotSigaa::new);
            s.setJid(jid);
            s.setDadosCifrados(cofre.cifrar(mapper.writeValueAsString(dados)));
            s.setAtualizadoEm(dados.atualizadoEm());
            repository.save(s);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao guardar os dados do SIGAA", e);
        }
    }

    public Optional<DadosSigaa> obter(String jid) {
        return repository.findById(jid).map(s -> {
            try {
                return mapper.readValue(cofre.decifrar(s.getDadosCifrados()), DadosSigaa.class);
            } catch (Exception e) {
                throw new IllegalStateException("Falha ao ler os dados do SIGAA", e);
            }
        });
    }

    public boolean tem(String jid) {
        return repository.existsById(jid);
    }

    public void esquecer(String jid) {
        repository.deleteById(jid);
    }
}
