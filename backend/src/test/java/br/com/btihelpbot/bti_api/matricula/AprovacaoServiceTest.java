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

    private static TaxaAprovacao taxa(String comp, String doc, double t, long total) {
        TaxaAprovacao x = new TaxaAprovacao();
        x.setComponenteNome(comp);
        x.setDocenteNome(doc);
        x.setTaxa(t);
        x.setTotal(total);
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
                taxa("CÁLCULO I", "PROF A", 0.80, 100),
                taxa("CÁLCULO I", "PROF B", 0.60, 90),
                taxa("CÁLCULO I", "PROF C", 0.95, 5),   // total baixo -> filtrado
                taxa("MATEMÁTICA ELEMENTAR", "PROF D", 0.40, 300)));

        // "calculo" sem acento acha "CÁLCULO", ignora total<10, rankeia por taxa desc
        List<AprovacaoDTO> r = service.porDisciplina("calculo", 10, 50);

        assertEquals(2, r.size());
        assertEquals("PROF A", r.get(0).docenteNome()); // 80% antes de 60%
        assertEquals("PROF B", r.get(1).docenteNome());
    }

    @Test
    void buscaComNumeroArabicoCasaRomano() {
        when(repository.findAll()).thenReturn(List.of(
                taxa("CÁLCULO DIFERENCIAL E INTEGRAL I", "PROF A", 0.70, 100),
                taxa("CÁLCULO DIFERENCIAL E INTEGRAL II", "PROF B", 0.90, 100)));

        List<AprovacaoDTO> um = service.porDisciplina("calculo 1", 10, 50);
        assertEquals(1, um.size());
        assertEquals("PROF A", um.get(0).docenteNome());

        List<AprovacaoDTO> dois = service.porDisciplina("calculo 2", 10, 50);
        assertEquals(1, dois.size());
        assertEquals("PROF B", dois.get(0).docenteNome());
    }
}
