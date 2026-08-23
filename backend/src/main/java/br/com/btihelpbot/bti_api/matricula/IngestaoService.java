package br.com.btihelpbot.bti_api.matricula;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    private static final Pattern CODIGO = Pattern.compile("[A-Z]{2,}[0-9]{3,}");

    private final CkanClient ckan;
    private final CsvDownloader csv;
    private final TaxaAprovacaoWriter writer;
    private final ComponenteWriter componenteWriter;

    public IngestaoService(CkanClient ckan, CsvDownloader csv,
                           TaxaAprovacaoWriter writer, ComponenteWriter componenteWriter) {
        this.ckan = ckan;
        this.csv = csv;
        this.writer = writer;
        this.componenteWriter = componenteWriter;
    }

    private record Semestre(int ano, int periodo, String matriculasUrl, String turmasUrl) {
        String label() {
            return ano + "." + periodo;
        }
    }

    private record ComponenteInfo(String codigo, String nome, String setor, Integer carga,
                                  String equivalencia, String preReq, String coReq) {}

    public int ingerir() {
        log.info("Ingestao de dados de matricula iniciada");

        Map<String, Docente> docentes = carregarDocentes();
        Map<Long, String> nomePorTurma = carregarAvaliacoes();
        Map<Long, ComponenteInfo> componentes = canonizar(carregarComponentes());
        List<Semestre> semestres = ultimosSemestres();
        log.info("Referencia carregada: {} docentes, {} nomes por turma, {} componentes, {} semestres",
                docentes.size(), nomePorTurma.size(), componentes.size(), semestres.size());

        AprovacaoAggregator agg = new AprovacaoAggregator(CURSOS);
        for (Semestre sem : semestres) {
            Map<Long, TurmaInfo> turmas = carregarTurmas(sem.turmasUrl(), docentes, nomePorTurma);
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

        canonizarDocentes(docentes);
        List<TaxaAprovacao> rows = montarTaxas(agg, componentes, docentes);
        writer.replaceAll(rows);
        log.info("Ingestao concluida: {} pares (disciplina, professor)", rows.size());

        Map<Long, Desfechos> porComponente = agg.getDesfechosPorComponente();
        Map<String, String> codigoNome = mapearCodigoNome(componentes);
        Map<Long, String> ementas = carregarEmentas(porComponente.keySet());
        List<Componente> comps = montarComponentes(porComponente, componentes, ementas, codigoNome);
        componenteWriter.replaceAll(comps);
        log.info("Componentes persistidos: {}", comps.size());

        return rows.size();
    }

    private static Map<Long, ComponenteInfo> canonizar(Map<Long, ComponenteInfo> componentes) {
        NomesCanonicos nomes = NomesCanonicos.de(
                componentes.values().stream().map(ComponenteInfo::nome).toList());
        Map<Long, ComponenteInfo> out = new HashMap<>();
        componentes.forEach((id, c) -> out.put(id, new ComponenteInfo(
                c.codigo(), nomes.melhor(c.nome()), c.setor(), c.carga(),
                c.equivalencia(), c.preReq(), c.coReq())));
        return out;
    }

    private static void canonizarDocentes(Map<String, Docente> docentes) {
        NomesCanonicos nomes = NomesCanonicos.de(
                docentes.values().stream().map(Docente::nome).toList());
        docentes.replaceAll((siape, d) -> new Docente(nomes.melhor(d.nome()), d.lotacao()));
    }

    private Map<String, Docente> carregarDocentes() {
        Map<String, Docente> docentes = new HashMap<>();
        for (CkanClient.Resource r : CkanClient.onlyCsv(ckan.getResources("docentes"))) {
            csv.stream(r.url(), row -> {
                String siape = trimToNull(row.get("siape"));
                if (siape != null) {
                    docentes.put(siape, new Docente(
                            trimToNull(row.get("nome")),
                            trimToNull(row.get("id_unidade_lotacao"))));
                }
            });
        }
        return docentes;
    }

    private Map<Long, String> carregarAvaliacoes() {
        Map<Long, String> nomePorTurma = new HashMap<>();
        for (CkanClient.Resource r : CkanClient.onlyCsv(ckan.getResources("avaliacoes-de-docencia"))) {
            csv.stream(r.url(), row -> {
                Long idTurma = parseLong(row.get("id_turma"));
                String nome = trimToNull(row.get("nome_docente"));
                if (idTurma != null && nome != null) nomePorTurma.put(idTurma, nome);
            });
        }
        return nomePorTurma;
    }

    private Map<Long, ComponenteInfo> carregarComponentes() {
        Map<Long, ComponenteInfo> componentes = new HashMap<>();
        for (CkanClient.Resource r : CkanClient.onlyCsv(ckan.getResources("componentes-curriculares"))) {
            csv.stream(r.url(), row -> {
                Long id = parseLong(row.get("id_componente"));
                if (id == null) return;
                componentes.put(id, new ComponenteInfo(
                        trimToNull(row.get("codigo")),
                        trimToNull(row.get("nome")),
                        trimToNull(row.get("unidade_responsavel")),
                        parseInteger(row.get("ch_total")),
                        trimToNull(row.get("equivalencia")),
                        trimToNull(row.get("pre_requisito")),
                        trimToNull(row.get("co_requisito"))));
            });
        }
        return componentes;
    }

    private Map<Long, String> carregarEmentas(Set<Long> alvos) {
        Map<Long, String> ementas = new HashMap<>();
        for (CkanClient.Resource r : CkanClient.onlyCsv(ckan.getResources("componentes-curriculares"))) {
            csv.stream(r.url(), row -> {
                Long id = parseLong(row.get("id_componente"));
                if (id == null || !alvos.contains(id)) return;
                String ementa = trimToNull(row.get("ementa"));
                if (ementa != null) ementas.put(id, ementa);
            });
        }
        return ementas;
    }

    private Map<Long, TurmaInfo> carregarTurmas(String url,
                                                Map<String, Docente> siapeNome,
                                                Map<Long, String> nomePorTurma) {
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
                if (!siapeNome.containsKey(siape) && nomePorTurma.containsKey(idTurma)) {
                    siapeNome.put(siape, new Docente(nomePorTurma.get(idTurma), null));
                }
            }
        });
        return turmas;
    }

    private List<TaxaAprovacao> montarTaxas(AprovacaoAggregator agg,
                                            Map<Long, ComponenteInfo> componentes,
                                            Map<String, Docente> docentes) {
        Map<String, String> slugs = SlugDocente.porSiape(docentes);
        List<TaxaAprovacao> rows = new ArrayList<>();
        for (Map.Entry<AprovacaoAggregator.Key, Desfechos> e : agg.getDesfechos().entrySet()) {
            Desfechos desfechos = e.getValue();
            if (desfechos.totalAvaliados() == 0) continue;

            AprovacaoAggregator.Key key = e.getKey();
            ComponenteInfo comp = componentes.get(key.componenteId());
            String codigo = comp != null ? comp.codigo() : null;
            String nome = comp != null ? comp.nome() : null;
            Docente docente = docentes.get(key.siape());
            String docenteNome = docente != null ? docente.nome() : null;

            TaxaAprovacao t = new TaxaAprovacao();
            t.setComponenteId(key.componenteId());
            t.setComponenteCodigo(codigo);
            t.setComponenteNome(nome);
            t.setSiape(key.siape());
            t.setDocenteNome(docenteNome);
            t.setDocenteSlug(slugs.get(key.siape()));
            t.setDesfechos(desfechos);
            rows.add(t);
        }
        return rows;
    }

    private List<Componente> montarComponentes(Map<Long, Desfechos> porComponente,
                                               Map<Long, ComponenteInfo> componentes,
                                               Map<Long, String> ementas,
                                               Map<String, String> codigoNome) {
        List<Componente> out = new ArrayList<>();
        for (Map.Entry<Long, Desfechos> e : porComponente.entrySet()) {
            Long id = e.getKey();
            ComponenteInfo info = componentes.get(id);
            if (info == null || info.nome() == null) continue;

            Componente c = new Componente();
            c.setId(id);
            c.setCodigo(info.codigo());
            c.setNome(info.nome());
            c.setSetor(info.setor());
            c.setCargaHoraria(info.carga());
            c.setEmenta(ementas.get(id));
            c.setEquivalencias(resolverEquivalencias(info.equivalencia(), codigoNome));
            c.setPreRequisito(info.preReq());
            c.setCoRequisito(info.coReq());
            c.setDesfechos(e.getValue());
            out.add(c);
        }
        return out;
    }

    private static Map<String, String> mapearCodigoNome(Map<Long, ComponenteInfo> componentes) {
        Map<String, String> out = new HashMap<>();
        for (ComponenteInfo ci : componentes.values()) {
            if (ci.codigo() != null && ci.nome() != null) {
                out.putIfAbsent(ci.codigo().toUpperCase(), ci.nome());
            }
        }
        return out;
    }

    private static String resolverEquivalencias(String expr, Map<String, String> codigoNome) {
        if (expr == null) return null;
        LinkedHashSet<String> codigos = new LinkedHashSet<>();
        Matcher m = CODIGO.matcher(expr.toUpperCase());
        while (m.find()) codigos.add(m.group());
        if (codigos.isEmpty()) return null;
        List<String> partes = new ArrayList<>();
        for (String cod : codigos) {
            String nome = codigoNome.get(cod);
            partes.add(nome != null ? cod + " - " + nome : cod);
        }
        return String.join("\n", partes);
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

    private static Integer parseInteger(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try {
            return Integer.parseInt(t);
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
