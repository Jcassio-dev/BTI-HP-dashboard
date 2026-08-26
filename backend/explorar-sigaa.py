#!/usr/bin/env python3
"""
Explora o SIGAA com a SUA credencial, na SUA maquina.

A senha vem de variavel de ambiente e nunca aparece na linha de comando.
O HTML cru fica em capturas/ e NAO deve ser compartilhado.
O que da para compartilhar e o esqueleto em capturas/*.esqueleto.txt, que tem a
estrutura das paginas com todo texto trocado por marcador de tipo.

    export SIGAA_USER='seu.login'
    read -rs SIGAA_PASS && export SIGAA_PASS
    python3 explorar-sigaa.py
"""

import html.parser
import http.cookiejar
import os
import re
import ssl
import sys
import urllib.parse
import urllib.request
from pathlib import Path

BASE = os.environ.get("SIGAA_BASE", "https://sigaa.ufrn.br")
LOGIN = f"{BASE}/sigaa/logar.do?dispatch=logOn"
PORTAL = f"{BASE}/sigaa/verPortalDiscente.do"
NAVEGADOR = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36"
SAIDA = Path("capturas")

SENSIVEL = re.compile(
    r"\b(\d{3}\.?\d{3}\.?\d{3}-?\d{2}|\d{4}\d{6,}|\d{2}/\d{2}/\d{4})\b"
)


def contexto_tls():
    """O host da UFRN pode nao mandar o intermediario da cadeia; usa o bundle local se houver."""
    ctx = ssl.create_default_context()
    extra = Path("certs/rnp-icpedu-gr46-ov-tls-ca-2025.pem")
    if extra.exists():
        ctx.load_verify_locations(cafile=str(extra))
    return ctx


class Esqueleto(html.parser.HTMLParser):
    """Guarda a arvore de tags com ids e classes, e troca todo texto por marcador de tipo."""

    INTERESSA = {"id", "class", "name", "value", "type", "action", "method", "href"}
    IGNORA = {"script", "style"}

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
        if tag not in ("br", "img", "input", "hr", "meta", "link"):
            self.nivel += 1

    def handle_endtag(self, tag):
        if tag in self.IGNORA:
            self.pulando = max(0, self.pulando - 1)
            return
        if self.pulando:
            return
        if tag not in ("br", "img", "input", "hr", "meta", "link"):
            self.nivel = max(0, self.nivel - 1)

    def handle_data(self, data):
        if self.pulando:
            return
        t = data.strip()
        if not t:
            return
        self.linhas.append("  " * self.nivel + tipo(t))


def tipo(texto):
    if SENSIVEL.search(texto):
        return "«DADO PESSOAL»"
    if re.fullmatch(r"[\d.,]+", texto):
        return "«NUM»"
    if len(texto) > 40:
        return "«TEXTO LONGO»"
    # rotulo curto: provavelmente cabecalho de coluna, util e nao e dado pessoal
    return f"«{texto}»"


def resumo_formularios(corpo):
    """Lista os formularios e os campos, que e o que preciso para remontar a navegacao."""
    saida = []
    for m in re.finditer(r"<form[^>]*>", corpo, re.I):
        tag = m.group(0)
        acao = re.search(r'action="([^"]*)"', tag, re.I)
        nome = re.search(r'(?:id|name)="([^"]*)"', tag, re.I)
        saida.append(f"FORM nome={nome.group(1) if nome else '?'} action={acao.group(1) if acao else '?'}")
    campos = set()
    for m in re.finditer(r'<(?:input|select|textarea)[^>]*name="([^"]+)"', corpo, re.I):
        campos.add(m.group(1))
    for c in sorted(campos):
        saida.append(f"  campo: {c}")
    return "\n".join(saida)


def salvar(nome, corpo):
    SAIDA.mkdir(exist_ok=True)
    (SAIDA / f"{nome}.html").write_text(corpo, encoding="utf-8", errors="replace")

    e = Esqueleto()
    e.feed(corpo)
    (SAIDA / f"{nome}.esqueleto.txt").write_text("\n".join(e.linhas), encoding="utf-8")

    (SAIDA / f"{nome}.formularios.txt").write_text(resumo_formularios(corpo), encoding="utf-8")
    print(f"  {nome}.html            (cru, NAO compartilhar)")
    print(f"  {nome}.esqueleto.txt   (estrutura, pode compartilhar)")
    print(f"  {nome}.formularios.txt (campos, pode compartilhar)")


def main():
    usuario = os.environ.get("SIGAA_USER")
    senha = os.environ.get("SIGAA_PASS")
    if not usuario or not senha:
        sys.exit("Defina SIGAA_USER e SIGAA_PASS no ambiente. Veja o cabecalho deste arquivo.")

    jar = http.cookiejar.CookieJar()
    abridor = urllib.request.build_opener(
        urllib.request.HTTPCookieProcessor(jar),
        urllib.request.HTTPSHandler(context=contexto_tls()),
    )
    abridor.addheaders = [("User-Agent", NAVEGADOR)]

    print("1. abrindo a pagina de login para pegar cookie inicial")
    with abridor.open(BASE + "/sigaa/verTelaLogin.do") as r:
        inicial = r.read().decode("utf-8", "replace")
    salvar("00-login", inicial)

    print("2. enviando credenciais")
    dados = urllib.parse.urlencode({
        "user.login": usuario,
        "user.senha": senha,
        "dispatch": "logOn",
        "urlRedirect": "",
        "subsistemaRedirect": "",
        "acao": "",
        "acessibilidade": "",
    }).encode()
    with abridor.open(urllib.request.Request(LOGIN, data=dados)) as r:
        pos_login = r.read().decode("utf-8", "replace")
        url_final = r.geturl()
    print(f"   terminou em: {url_final}")
    salvar("01-pos-login", pos_login)

    if "verTelaLogin" in url_final or "senha" in pos_login.lower()[:4000]:
        print("\n   Parece que o login NAO passou. Confira usuario e senha.")
        print("   Se o SIGAA pediu captcha, esse caminho nao vai funcionar sem intervencao.")
        return

    print("3. abrindo o portal do discente")
    try:
        with abridor.open(PORTAL) as r:
            portal = r.read().decode("utf-8", "replace")
        salvar("02-portal", portal)
    except Exception as e:
        print(f"   nao consegui: {e}")

    print("\ncookies da sessao (so os nomes):", ", ".join(sorted({c.name for c in jar})))
    print("\nPronto. Os arquivos estao em capturas/")
    print("Compartilhe SOMENTE os .esqueleto.txt e .formularios.txt")
    print("Confira antes de enviar; o esqueleto troca texto por marcador, mas dê uma olhada.")


if __name__ == "__main__":
    main()
