package br.com.btihelpbot.bti_api.sigaa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Endpoints do acesso do aluno ao SIGAA. Os do bot (X-API-Key) geram o token e consultam;
 * o /login e chamado pela pagina de login, no navegador do aluno.
 */
@RestController
@RequestMapping("/api/sigaa")
public class SigaaController {

    private final VinculoService vinculos;
    private final SessaoService sessoes;
    private final SigaaClient cliente;
    private final String site;
    private final String curl;

    public SigaaController(VinculoService vinculos, SessaoService sessoes, SigaaClient cliente,
                           @Value("${sigaa.site:https://bti-hp-dashboard.vercel.app}") String site,
                           @Value("${sigaa.curl:curl_chrome110}") String curl) {
        this.vinculos = vinculos;
        this.sessoes = sessoes;
        this.cliente = cliente;
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
        String token = vinculos.gerar(req.jid());
        return new ConectarResp(site + "/conectar?token=" + token);
    }

    public record LoginReq(String token, String usuario, String senha) {}

    /** Pagina de login: troca credenciais por sessao. A senha nao e guardada. */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginReq req) {
        String jid = vinculos.consumir(req.token())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.GONE,
                        "Este link expirou. Peça outro com !conectar no WhatsApp."));

        try (CurlNavegador nav = new CurlNavegador(curl)) {
            String cookie = new SigaaLogin(nav).logar(req.usuario(), req.senha());
            sessoes.salvar(jid, cookie);
            return ResponseEntity.ok(Map.of("status", "conectado"));
        } catch (SigaaLogin.CredenciaisInvalidas e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (SigaaLogin.AcessoBloqueado e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (CurlImpersonateHttp.SigaaIndisponivel e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }

    public record StatusResp(boolean conectado) {}

    /** Bot: o aluno ja tem sessao viva? */
    @GetMapping("/status")
    public StatusResp status(@RequestParam String jid) {
        return new StatusResp(sessoes.temSessao(jid));
    }

    /** Bot: !desconectar. */
    @DeleteMapping("/sessao")
    public ResponseEntity<Void> desconectar(@RequestParam String jid) {
        sessoes.esquecer(jid);
        return ResponseEntity.noContent().build();
    }

    /** Bot: !turmas. */
    @GetMapping("/turmas")
    public List<PortalParser.Turma> turmas(@RequestParam String jid) {
        try {
            return cliente.turmas(jid);
        } catch (SigaaClient.PrecisaConectar e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (CurlImpersonateHttp.SigaaIndisponivel e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }

    /** Bot: !atualizar. */
    @PostMapping("/atualizar")
    public ResponseEntity<Void> atualizar(@RequestParam String jid) {
        cliente.atualizar(jid);
        return ResponseEntity.noContent().build();
    }
}
