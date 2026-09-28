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

    @Mock
    private ComponenteRepository componentes;

    @InjectMocks
    private AprovacaoService service;

    private static Componente componente(Long id, String codigo, String nome, String setor,
                                         long aprovados, long reprovadosNota, long trancados) {
        Componente c = new Componente();
        c.setId(id);
        c.setCodigo(codigo);
        c.setNome(nome);
        c.setSetor(setor);
        c.setDesfechos(new Desfechos(aprovados, reprovadosNota, 0, trancados));
        return c;
    }

    private static TaxaAprovacao taxa(String comp, String doc,
                                      long aprovados, long reprovadosNota, long trancados) {
        return taxa(null, comp, doc, aprovados, reprovadosNota, trancados);
    }

    private static TaxaAprovacao taxa(String codigo, String comp, String doc,
                                      long aprovados, long reprovadosNota, long trancados) {
        TaxaAprovacao x = new TaxaAprovacao();
        x.setComponenteCodigo(codigo);
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
    void buscaPorDisciplinaTambemCasaOCodigo() {
        when(repository.findAll()).thenReturn(List.of(
                taxa("MAT0031", "CÁLCULO I", "PROF A", 80, 20, 0),
                taxa("IMD0030", "ALGORITMOS", "PROF B", 60, 40, 0)));

        List<AprovacaoDTO> r = service.porDisciplina("mat0031", 10, 50);

        assertEquals(1, r.size());
        assertEquals("PROF A", r.get(0).docenteNome());
        assertEquals("MAT0031", r.get(0).componenteCodigo());
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

    @Test
    void listarTurmasOrdenaPorNomePorPadrao() {
        when(componentes.findAll()).thenReturn(List.of(
                componente(1L, "MAT0031", "CÁLCULO I", "DMAT", 80, 20, 0),
                componente(2L, "IMD0030", "ALGORITMOS", "IMD", 60, 40, 0)));

        TurmasPaginadasDTO r = service.listarTurmas(null, null, 0, "nome", false, 0, 20);

        assertEquals(2, r.itens().size());
        assertEquals("ALGORITMOS", r.itens().get(0).nome());
        assertEquals("CÁLCULO I", r.itens().get(1).nome());
        assertEquals(2, r.total());
        assertEquals(1, r.totalPaginas());
    }

    @Test
    void listarTurmasFiltraPorMinTotalUsandoDesfechos() {
        when(componentes.findAll()).thenReturn(List.of(
                componente(1L, "MAT0031", "CÁLCULO I", "DMAT", 4, 1, 0),
                componente(2L, "IMD0030", "ALGORITMOS", "IMD", 60, 40, 0)));

        TurmasPaginadasDTO r = service.listarTurmas(null, null, 10, "nome", false, 0, 20);

        assertEquals(1, r.itens().size());
        assertEquals("ALGORITMOS", r.itens().get(0).nome());
    }

    @Test
    void listarTurmasFiltraPorSetor() {
        when(componentes.findAll()).thenReturn(List.of(
                componente(1L, "MAT0031", "CÁLCULO I", "DMAT", 80, 20, 0),
                componente(2L, "IMD0030", "ALGORITMOS", "IMD", 60, 40, 0)));

        TurmasPaginadasDTO r = service.listarTurmas(null, "imd", 0, "nome", false, 0, 20);

        assertEquals(1, r.itens().size());
        assertEquals("ALGORITMOS", r.itens().get(0).nome());
    }

    @Test
    void listarTurmasFiltraPorQCasandoNomeOuCodigo() {
        when(componentes.findAll()).thenReturn(List.of(
                componente(1L, "MAT0031", "CÁLCULO I", "DMAT", 80, 20, 0),
                componente(2L, "IMD0030", "ALGORITMOS", "IMD", 60, 40, 0)));

        TurmasPaginadasDTO r = service.listarTurmas("mat0031", null, 0, "nome", false, 0, 20);

        assertEquals(1, r.itens().size());
        assertEquals("MAT0031", r.itens().get(0).codigo());
    }

    @Test
    void listarTurmasPaginaResultados() {
        when(componentes.findAll()).thenReturn(List.of(
                componente(1L, "C1", "AAA", "S", 10, 0, 0),
                componente(2L, "C2", "BBB", "S", 10, 0, 0),
                componente(3L, "C3", "CCC", "S", 10, 0, 0)));

        TurmasPaginadasDTO pagina1 = service.listarTurmas(null, null, 0, "nome", false, 0, 2);
        assertEquals(2, pagina1.itens().size());
        assertEquals("AAA", pagina1.itens().get(0).nome());
        assertEquals("BBB", pagina1.itens().get(1).nome());
        assertEquals(2, pagina1.totalPaginas());

        TurmasPaginadasDTO pagina2 = service.listarTurmas(null, null, 0, "nome", false, 1, 2);
        assertEquals(1, pagina2.itens().size());
        assertEquals("CCC", pagina2.itens().get(0).nome());
    }

    @Test
    void listarTurmasOnePageIgnoraPaginacao() {
        when(componentes.findAll()).thenReturn(List.of(
                componente(1L, "C1", "AAA", "S", 10, 0, 0),
                componente(2L, "C2", "BBB", "S", 10, 0, 0),
                componente(3L, "C3", "CCC", "S", 10, 0, 0)));

        TurmasPaginadasDTO r = service.listarTurmas(null, null, 0, "nome", true, 0, 2);

        assertEquals(3, r.itens().size());
        assertEquals(0, r.pagina());
        assertEquals(3, r.tamanho());
        assertEquals(3, r.total());
        assertEquals(1, r.totalPaginas());
    }

    @Test
    void listarTurmasOrdenaPorTaxa() {
        when(componentes.findAll()).thenReturn(List.of(
                componente(1L, "C1", "BAIXA TAXA", "S", 10, 90, 0),
                componente(2L, "C2", "ALTA TAXA", "S", 90, 10, 0)));

        TurmasPaginadasDTO r = service.listarTurmas(null, null, 0, "taxa", false, 0, 20);

        assertEquals("ALTA TAXA", r.itens().get(0).nome());
        assertEquals("BAIXA TAXA", r.itens().get(1).nome());
    }
}
