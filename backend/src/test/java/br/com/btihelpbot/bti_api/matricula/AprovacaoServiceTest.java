package br.com.btihelpbot.bti_api.matricula;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AprovacaoServiceTest {

    @Mock
    private TaxaAprovacaoRepository repository;

    @InjectMocks
    private AprovacaoService service;

    private static TaxaAprovacao taxa(String comp, String doc,
                                      long aprovados, long reprovadosNota, long trancados) {
        TaxaAprovacao x = new TaxaAprovacao();
        x.setComponenteNome(comp);
        x.setDocenteNome(doc);
        x.setDesfechos(new Desfechos(aprovados, reprovadosNota, 0, trancados));
        return x;
    }

    @Test
    void normalizaTiraAcentoECaseInsensitive() {
        assertEquals("calculo", AprovacaoService.normalizar("CÁLCULO"));
        assertEquals("matematica", AprovacaoService.normalizar("  Matemática "));
    }

    @Test
    void buscaSemAcentoFiltraMinTotalERankeia() {
        when(repository.findAll()).thenReturn(List.of(
                taxa("CÁLCULO I", "PROF A", 80, 20, 0),        // 80%
                taxa("CÁLCULO I", "PROF B", 54, 36, 0),        // 60%
                taxa("CÁLCULO I", "PROF C", 4, 1, 0),          // matriculados 5 -> filtrado
                taxa("MATEMÁTICA ELEMENTAR", "PROF D", 120, 180, 0)));

        // "calculo" sem acento acha "CÁLCULO", ignora matriculados<10, rankeia por taxa desc
        List<AprovacaoDTO> r = service.porDisciplina("calculo", 10, 50);

        assertEquals(2, r.size());
        assertEquals("PROF A", r.get(0).docenteNome()); // 80% antes de 60%
        assertEquals("PROF B", r.get(1).docenteNome());
    }

    @Test
    void minTotalContaQuemTrancou() {
        when(repository.findAll()).thenReturn(List.of(
                taxa("CÁLCULO I", "PROF E", 6, 2, 5)));  // 8 avaliados, 13 matriculados

        List<AprovacaoDTO> r = service.porDisciplina("calculo", 10, 50);

        assertEquals(1, r.size());
        assertEquals(13, r.get(0).totalMatriculados());
        assertEquals(8, r.get(0).totalAvaliados());
        assertEquals(6d / 8d, r.get(0).taxaAprovacao(), 1e-9);
    }

    @Test
    void buscaComNumeroArabicoCasaRomano() {
        when(repository.findAll()).thenReturn(List.of(
                taxa("CÁLCULO DIFERENCIAL E INTEGRAL I", "PROF A", 70, 30, 0),
                taxa("CÁLCULO DIFERENCIAL E INTEGRAL II", "PROF B", 90, 10, 0)));

        List<AprovacaoDTO> um = service.porDisciplina("calculo 1", 10, 50);
        assertEquals(1, um.size());
        assertEquals("PROF A", um.get(0).docenteNome());

        List<AprovacaoDTO> dois = service.porDisciplina("calculo 2", 10, 50);
        assertEquals(1, dois.size());
        assertEquals("PROF B", dois.get(0).docenteNome());
    }
}
