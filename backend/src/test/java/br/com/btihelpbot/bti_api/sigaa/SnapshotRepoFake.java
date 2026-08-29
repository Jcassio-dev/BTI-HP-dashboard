package br.com.btihelpbot.bti_api.sigaa;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Repositorio em memoria para os testes de SessaoService. So os metodos usados sao reais. */
class SnapshotRepoFake implements SnapshotSigaaRepository {

    private final Map<String, SnapshotSigaa> banco;

    SnapshotRepoFake(Map<String, SnapshotSigaa> banco) {
        this.banco = banco;
    }

    @Override
    public <S extends SnapshotSigaa> S save(S s) {
        banco.put(s.getJid(), s);
        return s;
    }

    @Override
    public Optional<SnapshotSigaa> findById(String jid) {
        return Optional.ofNullable(banco.get(jid));
    }

    @Override
    public void deleteById(String jid) {
        banco.remove(jid);
    }

    // --- nao usados nos testes ---
    @Override public <S extends SnapshotSigaa> List<S> saveAll(Iterable<S> e) { throw new UnsupportedOperationException(); }
    @Override public boolean existsById(String s) { return banco.containsKey(s); }
    @Override public List<SnapshotSigaa> findAll() { return new ArrayList<>(banco.values()); }
    @Override public List<SnapshotSigaa> findAllById(Iterable<String> ids) { throw new UnsupportedOperationException(); }
    @Override public long count() { return banco.size(); }
    @Override public void delete(SnapshotSigaa e) { banco.remove(e.getJid()); }
    @Override public void deleteAllById(Iterable<? extends String> ids) { throw new UnsupportedOperationException(); }
    @Override public void deleteAll(Iterable<? extends SnapshotSigaa> e) { throw new UnsupportedOperationException(); }
    @Override public void deleteAll() { banco.clear(); }
    @Override public List<SnapshotSigaa> findAll(Sort sort) { throw new UnsupportedOperationException(); }
    @Override public <S extends SnapshotSigaa> List<S> saveAllAndFlush(Iterable<S> e) { throw new UnsupportedOperationException(); }
    @Override public void flush() {}
    @Override public <S extends SnapshotSigaa> S saveAndFlush(S e) { return save(e); }
    @Override public void deleteAllInBatch() { banco.clear(); }
    @Override public void deleteAllInBatch(Iterable<SnapshotSigaa> e) { throw new UnsupportedOperationException(); }
    @Override public void deleteAllByIdInBatch(Iterable<String> ids) { throw new UnsupportedOperationException(); }
    @Override public SnapshotSigaa getById(String s) { throw new UnsupportedOperationException(); }
    @Override public SnapshotSigaa getReferenceById(String s) { throw new UnsupportedOperationException(); }
    @Override public SnapshotSigaa getOne(String s) { throw new UnsupportedOperationException(); }
    @Override public <S extends SnapshotSigaa> Optional<S> findOne(Example<S> e) { throw new UnsupportedOperationException(); }
    @Override public <S extends SnapshotSigaa> List<S> findAll(Example<S> e) { throw new UnsupportedOperationException(); }
    @Override public <S extends SnapshotSigaa> List<S> findAll(Example<S> e, Sort sort) { throw new UnsupportedOperationException(); }
    @Override public <S extends SnapshotSigaa> org.springframework.data.domain.Page<S> findAll(Example<S> e, org.springframework.data.domain.Pageable p) { throw new UnsupportedOperationException(); }
    @Override public <S extends SnapshotSigaa> long count(Example<S> e) { throw new UnsupportedOperationException(); }
    @Override public <S extends SnapshotSigaa> boolean exists(Example<S> e) { throw new UnsupportedOperationException(); }
    @Override public org.springframework.data.domain.Page<SnapshotSigaa> findAll(org.springframework.data.domain.Pageable p) { throw new UnsupportedOperationException(); }
    @Override public <S extends SnapshotSigaa, R> R findBy(Example<S> e, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> f) { throw new UnsupportedOperationException(); }
}
