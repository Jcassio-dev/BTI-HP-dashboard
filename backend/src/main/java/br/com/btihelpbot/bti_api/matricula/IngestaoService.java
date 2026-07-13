package br.com.btihelpbot.bti_api.matricula;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IngestaoService {

    private static final Logger log = LoggerFactory.getLogger(IngestaoService.class);
    private static final int QTD_SEMESTRES = 10;

    private static final String BTI = "92127264";
    private static final String CIENCIA_COMPUTACAO = "2000013";
    private static final String ENGENHARIA_SOFTWARE = "17848940";
    private static final String BACHARELADO_IA = "178733979";
    private static final String ENGENHARIA_COMPUTACAO = "2000026";

    private static final Set<String> CURSOS = new HashSet<>(List.of(
            BTI, CIENCIA_COMPUTACAO, ENGENHARIA_SOFTWARE, BACHARELADO_IA, ENGENHARIA_COMPUTACAO));

    private static final Pattern MAT_SEM = Pattern.compile("matriculas-(\\d{4})\\.(\\d)");
    private static final Pattern TURMA_SEM = Pattern.compile("turmas-(\\d{4})-(\\d)");

    private final CkanClient ckan;
    private final CsvDownloader csv;
    private final TaxaAprovacaoWriter writer;

    public IngestaoService(CkanClient ckan, CsvDownloader csv, TaxaAprovacaoWriter writer) {
        this.ckan = ckan;
        this.csv = csv;
        this.writer = writer;
    }

    private record Semestre(int ano, int periodo, String matriculasUrl, String turmasUrl) {
        String label() {
            return ano + "." + periodo;
        }
    }

    public int ingerir() {
        log.info("Ingestao de dados de matricula iniciada");

        Map<String, String> docentes = carregarDocentes();
        Map<Long, String[]> componentes = carregarComponentes();
        List<Semestre> semestres = ultimosSemestres();
        log.info("Referencia carregada: {} docentes, {} componentes, {} semestres",
                docentes.size(), componentes.size(), semestres.size());

        AprovacaoAggregator agg = new AprovacaoAggregator(CURSOS);
        for (Semestre sem : semestres) {
            Map<Long, TurmaInfo> turmas = carregarTurmas(sem.turmasUrl());
            csv.stream(sem.matriculasUrl(), row -> {
                Long idTurma = parseLong(row.get("id_turma"));
                if (idTurma == null) return;
                agg.accumulate(turmas, new MatriculaRow(
                        idTurma,
                        trimToNull(row.get("discente")),
                        trimToNull(row.get("id_curso")),
                        row.get("descricao")));
            });
            log.info("Semestre {} processado ({} turmas graduacao consolidadas)", sem.label(), turmas.size());
        }

        List<TaxaAprovacao> rows = montarTaxas(agg, componentes, docentes);
        writer.replaceAll(rows);
        log.info("Ingestao concluida: {} pares (disciplina, professor)", rows.size());
        return rows.size();
    }

    private Map<String, String> carregarDocentes() {
        Map<String, String> docentes = new HashMap<>();
        for (CkanClient.Resource r : CkanClient.onlyCsv(ckan.getResources("docentes"))) {
            csv.stream(r.url(), row -> {
                String siape = trimToNull(row.get("siape"));
                if (siape != null) docentes.put(siape, trimToNull(row.get("nome")));
            });
        }
        return docentes;
    }

    private Map<Long, String[]> carregarComponentes() {
        Map<Long, String[]> componentes = new HashMap<>();
        for (CkanClient.Resource r : CkanClient.onlyCsv(ckan.getResources("componentes-curriculares"))) {
            csv.stream(r.url(), row -> {
                Long id = parseLong(row.get("id_componente"));
                if (id != null) componentes.put(id, new String[]{trimToNull(row.get("codigo")), trimToNull(row.get("nome"))});
            });
        }
        return componentes;
    }

    private Map<Long, TurmaInfo> carregarTurmas(String url) {
        Map<Long, TurmaInfo> turmas = new HashMap<>();
        csv.stream(url, row -> {
            String situacao = row.get("situacao_turma");
            String nivel = row.get("nivel_ensino");
            if (situacao == null || !situacao.trim().toUpperCase().startsWith("CONSOLIDADA")) return;
            if (nivel == null || !nivel.trim().toUpperCase().startsWith("GRADUA")) return;
            Long idTurma = parseLong(row.get("id_turma"));
            Long idComponente = parseLong(row.get("id_componente_curricular"));
            String siape = trimToNull(row.get("siape"));
            if (idTurma != null && idComponente != null && siape != null) {
                turmas.put(idTurma, new TurmaInfo(idComponente, siape));
                return;
            }
            if (idTurma != null && idComponente == null) {
                log.warn("Turma consolidada sem id_componente: id_turma={}, siape={}, situacao={}, nivel={}",
                        idTurma, siape, situacao, nivel);
            }
        });
        return turmas;
    }

    private List<TaxaAprovacao> montarTaxas(AprovacaoAggregator agg,
                                            Map<Long, String[]> componentes,
                                            Map<String, String> docentes) {
        List<TaxaAprovacao> rows = new ArrayList<>();
        for (Map.Entry<AprovacaoAggregator.Key, long[]> e : agg.getCounts().entrySet()) {
            long aprovados = e.getValue()[0];
            long reprovados = e.getValue()[1];
            long total = aprovados + reprovados;
            if (total == 0) continue;

            AprovacaoAggregator.Key key = e.getKey();
            String[] comp = componentes.getOrDefault(key.componenteId(), new String[]{null, null});
            String docenteNome = docentes.get(key.siape());

            if (comp[1] == null || docenteNome == null) {
                log.warn("Taxa sem nome de referencia: componenteId={}, componenteCodigo={}, siape={}, componenteNome={}, docenteNome={}, aprovados={}, reprovados={}, total={}",
                        key.componenteId(), comp[0], key.siape(), comp[1], docenteNome, aprovados, reprovados, total);
            }

            TaxaAprovacao t = new TaxaAprovacao();
            t.setComponenteId(key.componenteId());
            t.setComponenteCodigo(comp[0]);
            t.setComponenteNome(comp[1]);
            t.setSiape(key.siape());
            t.setDocenteNome(docenteNome);
            t.setAprovados(aprovados);
            t.setReprovados(reprovados);
            t.setTotal(total);
            t.setTaxa((double) aprovados / total);
            rows.add(t);
        }
        return rows;
    }

    private List<Semestre> ultimosSemestres() {
        Map<Integer, String> mat = mapearSemestres(ckan.getResources("matriculas-componentes"), MAT_SEM);
        Map<Integer, String> turmas = mapearSemestres(ckan.getResources("turmas"), TURMA_SEM);

        List<Integer> chaves = new ArrayList<>(mat.keySet());
        chaves.retainAll(turmas.keySet());
        chaves.sort(Comparator.reverseOrder());

        List<Semestre> semestres = new ArrayList<>();
        for (int i = 0; i < Math.min(QTD_SEMESTRES, chaves.size()); i++) {
            int k = chaves.get(i);
            semestres.add(new Semestre(k / 10, k % 10, mat.get(k), turmas.get(k)));
        }
        return semestres;
    }

    private Map<Integer, String> mapearSemestres(List<CkanClient.Resource> resources, Pattern pattern) {
        Map<Integer, String> out = new HashMap<>();
        for (CkanClient.Resource r : resources) {
            if (r.url() == null) continue;
            Matcher m = pattern.matcher(r.url());
            if (m.find()) {
                int chave = Integer.parseInt(m.group(1)) * 10 + Integer.parseInt(m.group(2));
                out.put(chave, r.url());
            }
        }
        return out;
    }

    private static Long parseLong(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try {
            return Long.parseLong(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
