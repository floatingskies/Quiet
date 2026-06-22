#!/usr/bin/env python3
"""
Gerador de códigos de ativação para o Quiet (org.floatingskies.Quiet).

Use este script para gerar um código de 9 linhas para cada cliente pagante.
Cada código é único e assinado com SHA-256 + segredo embarcado no app.

Uso:
    python gerar_codigo.py
    python gerar_codigo.py --quantidade 5
    python gerar_codigo.py --quantidade 10 --saida codigos.txt
"""

import hashlib
import random
import string
import argparse
import sys
from datetime import datetime

SEGREDO = "BloqueadorBR-2024-Lifetime-Protect-Key"
NUM_LINHAS = 9

CHARSET = string.ascii_uppercase + string.digits  # A-Z + 0-9


def gerar_bloco() -> str:
    """Gera um bloco no formato XXXX-XXXX-XXXX."""
    s = "".join(random.choices(CHARSET, k=12))
    return f"{s[:4]}-{s[4:8]}-{s[8:12]}"


def gerar_assinatura(corpo: str) -> str:
    """Gera a 9ª linha (assinatura) do código."""
    digest = hashlib.sha256((corpo + SEGREDO).encode()).hexdigest().upper()
    filtrado = "".join(c for c in digest if c.isalnum())[:12].ljust(12, "0")
    return f"{filtrado[:4]}-{filtrado[4:8]}-{filtrado[8:12]}"


def gerar_codigo() -> str:
    """Gera um código completo de 9 linhas."""
    corpo = "\n".join(gerar_bloco() for _ in range(8))
    assinatura = gerar_assinatura(corpo)
    return f"{corpo}\n{assinatura}"


def validar_codigo(codigo: str) -> bool:
    """Valida um código (usado para testar antes de enviar ao cliente)."""
    linhas = [l.strip().upper() for l in codigo.strip().split("\n") if l.strip()]
    if len(linhas) != NUM_LINHAS:
        return False
    for l in linhas:
        # Formato XXXX-XXXX-XXXX = 14 caracteres, com "-" nas posições 4 e 9
        if len(l) != 14 or l[4] != "-" or l[9] != "-":
            return False
        if not all(c.isalnum() for c in l.replace("-", "")):
            return False
    corpo = "\n".join(linhas[:8])
    return linhas[8] == gerar_assinatura(corpo)


def main():
    parser = argparse.ArgumentParser(description="Gerador de códigos de ativação Quiet")
    parser.add_argument("--quantidade", "-n", type=int, default=1, help="Quantos códigos gerar (padrão: 1)")
    parser.add_argument("--saida", "-o", type=str, help="Arquivo para salvar (opcional)")
    parser.add_argument("--compacto", action="store_true", help="Gerar no formato compacto (com pipes |)")
    args = parser.parse_args()

    codigos = []
    for i in range(args.quantidade):
        codigo = gerar_codigo()
        if not validar_codigo(codigo):
            print(f"ERRO: código gerado falhou na validação!", file=sys.stderr)
            sys.exit(1)
        codigos.append(codigo)

    if args.saida:
        with open(args.saida, "w", encoding="utf-8") as f:
            for i, c in enumerate(codigos, 1):
                f.write(f"===== Código #{i} ({datetime.now().isoformat()}) =====\n")
                if args.compacto:
                    f.write("|".join(c.split("\n")) + "\n\n")
                else:
                    f.write(c + "\n\n")
        print(f"{args.quantidade} código(s) salvo(s) em: {args.saida}")
    else:
        for i, c in enumerate(codigos, 1):
            print(f"\n===== Código #{i} =====")
            if args.compacto:
                print("|".join(c.split("\n")))
            else:
                print(c)
            print()


if __name__ == "__main__":
    main()
