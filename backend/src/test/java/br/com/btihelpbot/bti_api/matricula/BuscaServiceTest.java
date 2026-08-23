package br.com.btihelpbot.bti_api.matricula;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscaServiceTest {

    @Mock
    private TaxaAprovacaoRepository repository;

    @Mock
    private ComponenteRepository componentes;

    @InjectMocks
    private AprovacaoService service;

    private static TaxaAprovacao linha(String codigo, String comp, String doc,
                                       long aprovados, long reprovados, long trancados) {
        TaxaAprovacao t = new TaxaAprovacao();
        t.setComponenteCodigo(codigo);
        t.setComponenteNome(comp);
        t.setDocenteNome(doc);
        t.setDocenteSlug(SlugDocente.slug(doc));
        t.setDesfechos(new Desfechos(aprovados, reprovados, 0, trancados));
        return t;
    }

    private void base() {
        when(repository.findAll()).thenReturn(List.of(
                linha("MAT0031", "CALCULO I", "MAXWELL GOMES DA SILVA", 80, 20, 5),
                linha("MAT0031", "CALCULO I", "ANA SOUZA", 60, 40, 0),
                linha("IMD0030", "ALGORITMOS", "MAXWELL GOMES DA SILVA", 90, 10, 0),
                linha("ECT2101", "FISICA", "BRUNO LIMA", 30, 70, 0)));
    }

    @Test
    void umCampoSoCasaCodigoDisciplinaEProfessorAoMesmoTempo() {
        base();

        assertEquals(2, service.buscar("mat0031", 0, "taxa").disciplinas().size());
        assertEquals(2, service.buscar("calculo", 0, "taxa").disciplinas().size());
        assertEquals(2, service.buscar("maxwell", 0, "taxa").professores().size());
    }

    @Test
    void agrupaPorTipoQuandoOTermoCasaOsDois() {
        when(repository.findAll()).thenReturn(List.of(
                linha("IMD0030", "ALGORITMOS", "ANA SOUZA", 90, 10, 0),
                linha("MAT0031", "CALCULO I", "ALGORITMO PEREIRA", 50, 50, 0)));

        BuscaDTO r = service.buscar("algoritmo", 0, "taxa");

        assertEquals(1, r.disciplinas().size());
        assertEquals("ALGORITMOS", r.disciplinas().get(0).componenteNome());
        assertEquals(1, r.professores().size());
        assertEquals("ALGORITMO PEREIRA", r.professores().get(0).docenteNome());
    }

    @Test
    void filtraPeloMinimoDeMatriculados() {
        base();

        assertTrue(service.buscar("maxwell", 0, "taxa").professores().size() == 2);
        assertEquals(2, service.buscar("maxwell", 100, "taxa").professores().size());
        assertEquals(1, service.buscar("maxwell", 101, "taxa").professores().size());
    }

    @Test
    void ordenaPorTaxaAlunosOuNome() {
        base();

        List<AprovacaoDTO> porTaxa = service.buscar("calculo", 0, "taxa").disciplinas();
        assertEquals("MAXWELL GOMES DA SILVA", porTaxa.get(0).docenteNome());

        List<AprovacaoDTO> porAlunos = service.buscar("calculo", 0, "alunos").disciplinas();
        assertEquals(105, porAlunos.get(0).totalMatriculados());

        List<AprovacaoDTO> porNome = service.buscar("calculo", 0, "nome").disciplinas();
        assertEquals("ANA SOUZA", porNome.get(0).docenteNome());
    }

    @Test
    void termoVazioNaoDevolveNada() {
        BuscaDTO r = service.buscar("   ", 0, "taxa");

        assertTrue(r.disciplinas().isEmpty());
        assertTrue(r.professores().isEmpty());
    }
}
