package br.com.btihelpbot.bti_api.sigaa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * Endpoints do acesso do aluno ao SIGAA, no modelo one-shot: o login raspa os dados de uma vez,
 * guarda a foto cifrada e descarta a sessao. Os comandos leem da foto, com a data da coleta.
 */
@RestController
@RequestMapping("/api/sigaa")
public class SigaaController {

    private final VinculoService vinculos;
    private final SnapshotService snapshots;
    private final Coletor coletor;
    private final LimitadorLogin limitador;
    private final AceiteService aceites;
    private final String site;
    private final String curl;

    public SigaaController(VinculoService vinculos, SnapshotService snapshots, Coletor coletor,
                           LimitadorLogin limitador, AceiteService aceites,
                           @Value("${sigaa.site:https://bti-hp-dashboard.vercel.app}") String site,
                           @Value("${sigaa.curl:curl_chrome131}") String curl) {
        this.vinculos = vinculos;
        this.snapshots = snapshots;
        this.coletor = coletor;
        this.limitador = limitador;
        this.aceites = aceites;
        this.site = site;
        this.curl = curl;
    }

    public record ConectarReq(String jid) {}
    public record ConectarResp(String link) {}

    /** Bot: gera o token e devolve o link para o aluno abrir. Protegido por X-API-Key. */
    @PostMapping("/conectar")
    public ConectarResp conectar(@RequestBody ConectarReq req) {
        if (req.jid() == null || req.jid().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jid ausente");
        }
        return new ConectarResp(site + "/conectar?token=" + vinculos.gerar(req.jid()));
    }

    public record LoginReq(String token, String usuario, String senha, String versaoTermos) {}

    /**
     * Pagina de login: entra no SIGAA, raspa os dados de uma vez, guarda a foto e descarta a
     * sessao. A senha nao e guardada, e a sessao nao sobrevive a esta chamada.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginReq req, HttpServletRequest http) {
        if (!limitador.permitir(ipDe(http))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas. Espere alguns minutos e tente de novo.");
        }

        if (req.versaoTermos() == null || req.versaoTermos().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "É preciso aceitar os termos de uso para continuar.");
        }

        String jid = vinculos.consumir(req.token())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.GONE,
                        "Este link expirou. Peça outro com !conectar no WhatsApp."));

        try (CurlNavegador nav = new CurlNavegador(curl)) {
            new SigaaLogin(nav).logar(req.usuario(), req.senha());
            DadosSigaa dados = coletor.coletar(jid, nav);
            snapshots.salvar(jid, dados);
            aceites.registrar(jid, req.versaoTermos(), ipDe(http));
            return ResponseEntity.ok(Map.of(
                    "status", "conectado",
                    "turmas", dados.turmas().size()));
        } catch (SigaaLogin.CredenciaisInvalidas e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (SigaaLogin.AcessoBloqueado e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (PrecisaConectar | CurlImpersonateHttp.SigaaIndisponivel e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }

    /** IP real do cliente: atras do Caddy vem no X-Forwarded-For, primeiro salto. */
    private static String ipDe(HttpServletRequest http) {
        String encaminhado = http.getHeader("X-Forwarded-For");
        if (encaminhado != null && !encaminhado.isBlank()) {
            return encaminhado.split(",")[0].trim();
        }
        return http.getRemoteAddr();
    }

    public record StatusResp(boolean conectado) {}

    @GetMapping("/status")
    public StatusResp status(@RequestParam String jid) {
        return new StatusResp(snapshots.tem(jid));
    }

    @DeleteMapping("/sessao")
    public ResponseEntity<Void> desconectar(@RequestParam String jid) {
        snapshots.esquecer(jid);
        return ResponseEntity.noContent().build();
    }

    /** Bot: a foto completa (turmas, indices, institucional, atualizadoEm). */
    @GetMapping("/dados")
    public DadosSigaa dados(@RequestParam String jid) {
        return snapshots.obter(jid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Conecte sua conta do SIGAA com !conectar."));
    }
}
