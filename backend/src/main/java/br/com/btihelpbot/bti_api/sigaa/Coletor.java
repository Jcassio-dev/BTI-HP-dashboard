package br.com.btihelpbot.bti_api.sigaa;

import java.time.Clock;
import java.util.List;

/**
 * Colhe os dados do aluno numa unica passada, usando a sessao recem-aberta no login.
 * O portal do discente ja traz turmas e indices juntos, entao e uma requisicao so.
 * Nao guarda a sessao: quem chama descarta o cookie logo apos.
 */
public class Coletor {

    private static final String PORTAL = "https://sigaa.ufrn.br/sigaa/portais/discente/discente.jsf";

    private final SigaaHttp http;
    private final FilaSigaa fila;
    private final Clock relogio;

    public Coletor(SigaaHttp http, FilaSigaa fila, Clock relogio) {
        this.http = http;
        this.fila = fila;
        this.relogio = relogio;
    }

    public DadosSigaa coletar(String jid, String cookie) {
        String html = fila.executar(jid, () -> http.get(PORTAL, cookie));
        if (!PortalParser.autenticado(html)) {
            throw new PrecisaConectar();
        }

        IndicesParser.Indices ind = IndicesParser.de(html);
        List<DadosSigaa.Indice> indices = ind.lista().stream()
                .map(i -> new DadosSigaa.Indice(i.sigla(), i.valor(), i.nome()))
                .toList();

        return new DadosSigaa(
                PortalParser.turmas(html),
                indices,
                ind.institucional(),
                ind.percentualIntegralizado().orElse(null),
                relogio.instant());
    }
}
