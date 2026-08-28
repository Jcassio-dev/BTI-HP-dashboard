package br.com.btihelpbot.bti_api.sigaa;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Repositorio em memoria para os testes de SessaoService. So os metodos usados sao reais. */
class SessaoSigaaRepositoryFake implements SessaoSigaaRepository {

    private final Map<String, SessaoSigaa> banco;

    SessaoSigaaRepositoryFake(Map<String, SessaoSigaa> banco) {
        this.banco = banco;
    }

    @Override
    public <S extends SessaoSigaa> S save(S s) {
        banco.put(s.getJid(), s);
        return s;
    }

    @Override
    public Optional<SessaoSigaa> findById(String jid) {
        return Optional.ofNullable(banco.get(jid));
    }

    @Override
    public void deleteById(String jid) {
        banco.remove(jid);
    }

    // --- nao usados nos testes ---
    @Override public <S extends SessaoSigaa> List<S> saveAll(Iterable<S> e) { throw new UnsupportedOperationException(); }
    @Override public boolean existsById(String s) { return banco.containsKey(s); }
    @Override public List<SessaoSigaa> findAll() { return new ArrayList<>(banco.values()); }
    @Override public List<SessaoSigaa> findAllById(Iterable<String> ids) { throw new UnsupportedOperationException(); }
    @Override public long count() { return banco.size(); }
    @Override public void delete(SessaoSigaa e) { banco.remove(e.getJid()); }
    @Override public void deleteAllById(Iterable<? extends String> ids) { throw new UnsupportedOperationException(); }
    @Override public void deleteAll(Iterable<? extends SessaoSigaa> e) { throw new UnsupportedOperationException(); }
    @Override public void deleteAll() { banco.clear(); }
    @Override public List<SessaoSigaa> findAll(Sort sort) { throw new UnsupportedOperationException(); }
    @Override public <S extends SessaoSigaa> List<S> saveAllAndFlush(Iterable<S> e) { throw new UnsupportedOperationException(); }
    @Override public void flush() {}
    @Override public <S extends SessaoSigaa> S saveAndFlush(S e) { return save(e); }
    @Override public void deleteAllInBatch() { banco.clear(); }
    @Override public void deleteAllInBatch(Iterable<SessaoSigaa> e) { throw new UnsupportedOperationException(); }
    @Override public void deleteAllByIdInBatch(Iterable<String> ids) { throw new UnsupportedOperationException(); }
    @Override public SessaoSigaa getById(String s) { throw new UnsupportedOperationException(); }
    @Override public SessaoSigaa getReferenceById(String s) { throw new UnsupportedOperationException(); }
    @Override public SessaoSigaa getOne(String s) { throw new UnsupportedOperationException(); }
    @Override public <S extends SessaoSigaa> Optional<S> findOne(Example<S> e) { throw new UnsupportedOperationException(); }
    @Override public <S extends SessaoSigaa> List<S> findAll(Example<S> e) { throw new UnsupportedOperationException(); }
    @Override public <S extends SessaoSigaa> List<S> findAll(Example<S> e, Sort sort) { throw new UnsupportedOperationException(); }
    @Override public <S extends SessaoSigaa> org.springframework.data.domain.Page<S> findAll(Example<S> e, org.springframework.data.domain.Pageable p) { throw new UnsupportedOperationException(); }
    @Override public <S extends SessaoSigaa> long count(Example<S> e) { throw new UnsupportedOperationException(); }
    @Override public <S extends SessaoSigaa> boolean exists(Example<S> e) { throw new UnsupportedOperationException(); }
    @Override public org.springframework.data.domain.Page<SessaoSigaa> findAll(org.springframework.data.domain.Pageable p) { throw new UnsupportedOperationException(); }
    @Override public <S extends SessaoSigaa, R> R findBy(Example<S> e, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> f) { throw new UnsupportedOperationException(); }
}
