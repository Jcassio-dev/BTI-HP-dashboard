package br.com.btihelpbot.bti_api.sigaa;

import java.time.Clock;
import java.util.List;

/**
 * Colhe os dados do aluno numa unica passada, usando a sessao recem-aberta no login.
 * O portal ja traz turmas e indices; notas vem de uma navegacao JSF a partir dele.
 * Nao guarda a sessao: quem chama descarta o navegador logo apos.
 */
public class Coletor {

    private static final String PORTAL = "https://sigaa.ufrn.br/sigaa/portais/discente/discente.jsf";
    private static final String MENU_NOTAS = "Consultar Minhas Notas";

    private final FilaSigaa fila;
    private final Clock relogio;

    public Coletor(FilaSigaa fila, Clock relogio) {
        this.fila = fila;
        this.relogio = relogio;
    }

    public DadosSigaa coletar(String jid, Navegador nav) {
        return fila.executar(jid, () -> {
            String portal = nav.get(PORTAL);
            if (!PortalParser.autenticado(portal)) {
                throw new PrecisaConectar();
            }

            IndicesParser.Indices ind = IndicesParser.de(portal);
            List<DadosSigaa.Indice> indices = ind.lista().stream()
                    .map(i -> new DadosSigaa.Indice(i.sigla(), i.valor(), i.nome()))
                    .toList();

            return new DadosSigaa(
                    PortalParser.turmas(portal),
                    indices,
                    ind.institucional(),
                    ind.percentualIntegralizado().orElse(null),
                    notas(nav, portal),
                    relogio.instant());
        });
    }

    private List<NotasParser.Periodo> notas(Navegador nav, String portal) {
        return Navegacao.camposMenu(portal, MENU_NOTAS)
                .map(campos -> NotasParser.de(nav.postForm(PORTAL, campos)))
                .orElse(List.of());
    }
}
