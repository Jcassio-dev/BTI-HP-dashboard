#!/usr/bin/env python3
"""
Explora o SIGAA com a SUA credencial, na SUA maquina.

A senha vem de variavel de ambiente e nunca aparece na linha de comando.
O HTML cru fica em capturas/ e NAO deve ser compartilhado.
O que da para compartilhar e capturas/*.esqueleto.txt e capturas/*.formularios.txt,
que tem a estrutura das paginas com o texto trocado por marcador de tipo.

    cd backend
    read -rp 'Login: ' SIGAA_USER && export SIGAA_USER
    read -rsp 'Senha: ' SIGAA_PASS && export SIGAA_PASS && echo
    python3 explorar-sigaa.py
    unset SIGAA_PASS

O login da UFRN e CAS (single sign-on) em autenticacao.ufrn.br, nao um POST direto no SIGAA:
  1. GET na tela do CAS, que devolve os campos lt / execution / _eventId e um jsessionid
  2. POST das credenciais nessa mesma tela
  3. CAS redireciona para o SIGAA com ?ticket=ST-...
  4. O SIGAA valida o ticket e abre a sessao dele
"""

import html.parser
import http.cookiejar
import os
import re
import ssl
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

CAS = "https://autenticacao.ufrn.br"
SIGAA = "https://sigaa.ufrn.br"
SERVICO = f"{SIGAA}/sigaa/login/cas"
ENTRADA = f"{CAS}/sso-server/login?service=" + urllib.parse.quote(SERVICO, safe="")
PORTAL = f"{SIGAA}/sigaa/verPortalDiscente.do"
NAVEGADOR = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36"
TEMPO_LIMITE = 25

SAIDA = Path("capturas")

# Marcadores de dado pessoal, do mais especifico ao mais generico.
EMAIL = re.compile(r"[\w.+-]+@[\w.-]+")
LOGIN = re.compile(r"\b[a-zA-ZÀ-ú]+\.[a-zA-ZÀ-ú]+(?:\.\w+)+\b")  # nome.nome.123
CPF = re.compile(r"\b\d{3}\.?\d{3}\.?\d{3}-?\d{2}\b")
DATA = re.compile(r"\b\d{2}/\d{2}/\d{4}\b")
MATRICULA = re.compile(r"\b\d{9,}\b")

# Rotulos de interface que preciso ver e nao sao dado pessoal. Fora desta lista, texto
# com letra vira «TEXTO» (curto) so se estiver aqui; senao vira «REDIGIDO».
ROTULOS_OK = re.compile(
    r"^(componente|curricular|local|hor[aá]rio|turma|per[ií]odo|situa[cç][aã]o|nota|"
    r"falta|m[eé]dia|resultado|c[oó]digo|curso|matr[ií]cula|semestre|status|ativo|"
    r"aprovado|reprovado|trancado|cumpriu|menu|discente|portal|ver|detalhar|continuar|"
    r"in[ií]cio|fim|dia|sala|docente|professor|carga|ch|cr[eé]dito|ementa|"
    r"segunda|ter[cç]a|quarta|quinta|sexta|s[aá]bado|domingo|manh[aã]|tarde|noite|"
    r"[a-z]{1,3}\d{2,}|imd|ect|mat|dim|\d+[mtn]\d+)",
    re.I,
)


def contexto_tls():
    """Hosts da UFRN podem nao mandar o intermediario da cadeia; usa o bundle local se houver."""
    ctx = ssl.create_default_context()
    extra = Path("certs/rnp-icpedu-gr46-ov-tls-ca-2025.pem")
    if extra.exists():
        try:
            ctx.load_verify_locations(cafile=str(extra))
        except Exception:
            pass
    return ctx


class Esqueleto(html.parser.HTMLParser):
    """Guarda a arvore de tags com ids e classes, e troca todo texto por marcador de tipo."""

    IGNORA = {"script", "style"}
    VAZIAS = {"br", "img", "input", "hr", "meta", "link"}

    def __init__(self):
        super().__init__()
        self.linhas = []
        self.nivel = 0
        self.pulando = 0

    def handle_starttag(self, tag, attrs):
        if tag in self.IGNORA:
            self.pulando += 1
            return
        if self.pulando:
            return
        d = dict(attrs)
        partes = []
        for k in ("id", "class", "name", "type", "action", "method"):
            v = d.get(k)
            if v:
                partes.append(f'{k}="{v[:60]}"')
            if k == "name" and v and "viewstate" in v.lower():
                partes.append('value="«VIEWSTATE»"')
        self.linhas.append("  " * self.nivel + f"<{tag}" + (" " + " ".join(partes) if partes else "") + ">")
        if tag not in self.VAZIAS:
            self.nivel += 1

    def handle_endtag(self, tag):
        if tag in self.IGNORA:
            self.pulando = max(0, self.pulando - 1)
            return
        if self.pulando or tag in self.VAZIAS:
            return
        self.nivel = max(0, self.nivel - 1)

    def handle_data(self, data):
        if self.pulando:
            return
        t = data.strip()
        if t:
            self.linhas.append("  " * self.nivel + tipo(t))


def tipo(texto):
    if EMAIL.search(texto) or LOGIN.search(texto) or CPF.search(texto) or MATRICULA.search(texto):
        return "«DADO PESSOAL»"
    if re.fullmatch(r"[\d.,%:h/-]+", texto) or DATA.search(texto):
        return "«NUM»"
    # codigo de horario tipo 24M34, 2M1234: estrutura, nao dado pessoal
    if re.fullmatch(r"\d{1,6}[MTN]\d{1,6}", texto):
        return f"«{texto}»"
    if ROTULOS_OK.match(texto):
        return f"«{texto}»"
    # sobrou texto com letras que nao esta na lista branca: nome, ementa, etc. Redige.
    return "«REDIGIDO»"


def resumo_formularios(corpo):
    saida = []
    for m in re.finditer(r"<form[^>]*>", corpo, re.I):
        tag = m.group(0)
        acao = re.search(r'action="([^"]*)"', tag, re.I)
        nome = re.search(r'(?:id|name)="([^"]*)"', tag, re.I)
        acao_txt = acao.group(1) if acao else "?"
        acao_txt = re.sub(r";jsessionid=[^?]*", ";jsessionid=«SESSAO»", acao_txt)
        saida.append(f"FORM nome={nome.group(1) if nome else '?'} action={acao_txt}")
    campos = set()
    for m in re.finditer(r'<(?:input|select|textarea)[^>]*name="([^"]+)"', corpo, re.I):
        campos.add(m.group(1))
    for c in sorted(campos):
        saida.append(f"  campo: {c}")
    return "\n".join(saida) or "(nenhum formulario)"


def salvar(nome, corpo):
    SAIDA.mkdir(exist_ok=True)
    (SAIDA / f"{nome}.html").write_text(corpo, encoding="utf-8", errors="replace")
    e = Esqueleto()
    e.feed(corpo)
    (SAIDA / f"{nome}.esqueleto.txt").write_text("\n".join(e.linhas), encoding="utf-8")
    (SAIDA / f"{nome}.formularios.txt").write_text(resumo_formularios(corpo), encoding="utf-8")
    print(f"     salvo: {nome}.html (cru) + .esqueleto.txt + .formularios.txt")


def limpar(url):
    url = re.sub(r"ticket=[^&]*", "ticket=«TICKET»", url)
    url = re.sub(r";jsessionid=[^?&]*", ";jsessionid=«SESSAO»", url)
    return url


class TracaRedirect(urllib.request.HTTPRedirectHandler):
    """Registra cada salto do redirecionamento, para eu ver onde o ticket se perde."""

    saltos = []

    def redirect_request(self, req, fp, code, msg, headers, newurl):
        TracaRedirect.saltos.append((code, limpar(newurl)))
        return super().redirect_request(req, fp, code, msg, headers, newurl)


def buscar(abridor, url, dados=None, rotulo=""):
    TracaRedirect.saltos = []
    req = url if isinstance(url, urllib.request.Request) else urllib.request.Request(url, data=dados)
    try:
        with abridor.open(req, timeout=TEMPO_LIMITE) as r:
            return r.read().decode("utf-8", "replace"), r.geturl(), r.status
    except urllib.error.HTTPError as e:
        return e.read().decode("utf-8", "replace"), e.url, e.code
    except TimeoutError:
        sys.exit(f"Timeout de {TEMPO_LIMITE}s em {rotulo or url}. A rede ou o host nao respondeu.")
    except urllib.error.URLError as e:
        sys.exit(f"Nao consegui abrir {rotulo or url}: {e.reason}")


def mostrar_saltos():
    if TracaRedirect.saltos:
        for code, u in TracaRedirect.saltos:
            print(f"     {code} -> {u[:110]}")


def abrir_navegador(jar):
    abridor = urllib.request.build_opener(
        urllib.request.HTTPCookieProcessor(jar),
        urllib.request.HTTPSHandler(context=contexto_tls()),
        TracaRedirect(),
    )
    abridor.addheaders = [
        ("User-Agent", NAVEGADOR),
        ("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
        ("Accept-Language", "pt-BR,pt;q=0.9"),
    ]
    return abridor


def por_cookie(cookie):
    """Reaproveita uma sessao ja aberta no navegador. Contorna qualquer filtro no login."""
    jar = http.cookiejar.CookieJar()
    abridor = abrir_navegador(jar)
    valor = cookie.split("=", 1)[1] if "=" in cookie else cookie
    c = http.cookiejar.Cookie(
        version=0, name="JSESSIONID", value=valor, port=None, port_specified=False,
        domain="sigaa.ufrn.br", domain_specified=True, domain_initial_dot=False,
        path="/sigaa", path_specified=True, secure=True, expires=None, discard=True,
        comment=None, comment_url=None, rest={}, rfc2109=False,
    )
    jar.set_cookie(c)

    print("usando a sessao do cookie; abrindo o portal do discente")
    portal, url, status = buscar(abridor, PORTAL, rotulo="portal")
    print(f"   terminou em: {limpar(url)} (status {status})")
    salvar("02-portal", portal)

    if "sso-server" in url or "login/cas" in url or status == 401:
        print("\n   O cookie nao vale mais (a sessao expirou ou foi copiada errada).")
        print("   Loga de novo no navegador e copia o JSESSIONID atualizado.")
        return
    print("\n   Deu certo. O portal veio autenticado.")
    print("   Agora me diga por qual tela seguir (turmas, notas, faltas) que eu ajusto a navegacao.")


def de_arquivo(caminho):
    """Sanitiza um HTML que voce salvou do navegador. Nao precisa de login nem cookie."""
    dados = Path(caminho).read_bytes()
    for cs in ("utf-8", "latin1"):
        try:
            corpo = dados.decode(cs)
            break
        except UnicodeDecodeError:
            corpo = dados.decode("latin1", "replace")
    nome = Path(caminho).stem
    salvar(nome, corpo)
    print(f"\nPronto. Me mande capturas/{nome}.esqueleto.txt e capturas/{nome}.formularios.txt")
    print("Confira antes de enviar; o esqueleto redige nome, email e matricula.")


def main():
    if len(sys.argv) > 1:
        de_arquivo(sys.argv[1])
        return

    cookie = os.environ.get("SIGAA_COOKIE")
    if cookie:
        por_cookie(cookie)
        return

    usuario = os.environ.get("SIGAA_USER")
    senha = os.environ.get("SIGAA_PASS")
    if not usuario or not senha:
        sys.exit("Defina SIGAA_USER e SIGAA_PASS (login) ou SIGAA_COOKIE (sessao pronta).")

    jar = http.cookiejar.CookieJar()
    abridor = abrir_navegador(jar)

    print("1. abrindo a tela do CAS")
    tela, url_tela, _ = buscar(abridor, ENTRADA, rotulo="tela do CAS")
    salvar("00-cas", tela)

    if re.search(r"captcha", tela, re.I):
        sys.exit("   A tela pediu captcha. Este caminho nao funciona sem intervencao manual.")

    acao = re.search(r'<form[^>]*action="([^"]*)"', tela, re.I)
    if not acao:
        sys.exit("   Nao achei o formulario de login. Veja capturas/00-cas.html")
    destino = urllib.parse.urljoin(url_tela, acao.group(1).replace("&amp;", "&"))

    campos = {}
    for m in re.finditer(r"<(?:input|button)\b[^>]*>", tela, re.I):
        tag = m.group(0)
        tipo_campo = (re.search(r'type="([^"]*)"', tag, re.I) or [None, ""])[1].lower()
        if tipo_campo == "password":
            continue
        n = re.search(r'name="([^"]*)"', tag, re.I)
        v = re.search(r'value="([^"]*)"', tag, re.I)
        if n:
            campos[n.group(1)] = v.group(1) if v else ""
    print(f"   campos do formulario: {', '.join(sorted(campos)) or 'nenhum'}")

    print("2. enviando credenciais ao CAS")
    campos.update({"username": usuario, "password": senha})
    req = urllib.request.Request(
        destino,
        data=urllib.parse.urlencode(campos).encode(),
        headers={"Referer": url_tela, "Origin": f"https://{urllib.parse.urlparse(destino).netloc}"},
    )
    corpo, url_final, status = buscar(abridor, req, rotulo="POST do CAS")
    print(f"   cadeia de redirecionamento:")
    mostrar_saltos()
    print(f"   terminou em: {limpar(url_final)} (status {status})")
    salvar("01-pos-login", corpo)

    tem_password = bool(re.search(r'type="password"', corpo, re.I))
    tem_bloqueio = "BLOQUEADO" in corpo.upper() or "NÃO AUTORIZADO" in corpo.upper()
    tem_ticket = any("ticket" in u for _, u in TracaRedirect.saltos)

    if tem_password:
        print("\n   O CAS remostrou o login: credenciais recusadas na autenticacao.")
        return
    print(f"   ticket emitido pelo CAS? {'sim' if tem_ticket else 'NAO'}")
    if tem_bloqueio:
        print("   O SIGAA autenticou mas mostrou ACESSO BLOQUEADO.")
        print("   (Se o mesmo login entra no navegador, e diferenca do fluxo automatizado.)")

    print("3. abrindo o portal do discente")
    portal, url_portal, status = buscar(abridor, PORTAL, rotulo="portal")
    print(f"   cadeia:")
    mostrar_saltos()
    print(f"   terminou em: {limpar(url_portal)} (status {status})")
    salvar("02-portal", portal)

    print("\ncookies da sessao (so os nomes):", ", ".join(sorted({c.name for c in jar})))
    print("\nPronto. Os arquivos estao em capturas/")
    print("Compartilhe SOMENTE os .esqueleto.txt e .formularios.txt")
    print("De uma olhada neles antes de enviar; o esqueleto troca texto por marcador, mas confira.")


if __name__ == "__main__":
    main()