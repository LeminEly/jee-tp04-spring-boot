#!/usr/bin/env python3
"""Contrôle du message de commit (crochet commit-msg).

Deux exigences de nature différente (registre G.58) :

- le FOND protège la trace du dépôt et s'applique à TOUT message, fusions et
  reverts compris : aucune mention d'outil ou d'agent d'assistance, aucun emoji ;
- la FORME décrit un message rédigé par une personne (Conventional Commits,
  sujet en minuscules, 72 caractères au plus) et ne s'applique pas aux messages
  que git engendre lui-même. Une fusion se reconnaît à MERGE_HEAD, un revert à
  REVERT_HEAD — jamais à leur texte, qu'on falsifie.

Limite (registre G.52) : ce crochet ne lit que le MESSAGE. L'identité de
l'auteur est lue par scripts/check-identity.py, à l'étape pre-commit.

Usage : check-commit-msg.py [--fond-seulement] <fichier-du-message>

--fond-seulement : utilisé en intégration pour un commit de fusion, reconnu à
ses parents dans l'historique (plus d'un parent).
"""
import os
import re
import subprocess
import sys

TYPES = "feat|fix|docs|refactor|test|chore|perf|style|ci|build|revert"
SUJET = re.compile(rf"^(?:{TYPES})(?:\([a-z0-9._/-]+\))?!?: [a-z0-9`].*[^.]$")
LONGUEUR_MAX = 72

# Mentions d'outils d'assistance, y compris dans une ligne Co-Authored-By.
OUTILS = re.compile(
    r"\b(claude|anthropic|chatgpt|openai|copilot|gpt-?\d|gemini|cursor|codeium|"
    r"tabnine|llm|generated with|co-authored-by:\s*(?:claude|copilot|.*\[bot\]))",
    re.IGNORECASE,
)
EMOJI = re.compile(
    "[\U0001F000-\U0001FAFF\U00002600-\U000027BF\U0001F1E6-\U0001F1FF⭐⭕️]"
)


def dossier_git():
    return subprocess.run(
        ["git", "rev-parse", "--git-dir"], capture_output=True, text=True, check=True
    ).stdout.strip()


def lignes_utiles(texte):
    """Retire les commentaires que git ajoute dans l'éditeur (lignes en #)."""
    utiles = []
    for ligne in texte.splitlines():
        if ligne.startswith("# ------------------------ >8"):
            break
        if not ligne.startswith("#"):
            utiles.append(ligne.rstrip())
    return utiles


def main(argv):
    fond_seulement = "--fond-seulement" in argv
    argv = [a for a in argv if a != "--fond-seulement"]
    if len(argv) != 2:
        print("usage : check-commit-msg.py [--fond-seulement] <fichier-du-message>",
              file=sys.stderr)
        return 2

    with open(argv[1], encoding="utf-8") as f:
        lignes = lignes_utiles(f.read())
    while lignes and not lignes[-1]:
        lignes.pop()
    erreurs = []

    if not lignes or not lignes[0]:
        print("Message de commit vide.", file=sys.stderr)
        return 1

    # Le fond, partout.
    for numero, ligne in enumerate(lignes, start=1):
        if OUTILS.search(ligne):
            erreurs.append(f"ligne {numero} : mention d'outil interdite — « {ligne} »")
        if EMOJI.search(ligne):
            erreurs.append(f"ligne {numero} : emoji interdit — « {ligne} »")

    # La forme, seulement pour un message rédigé.
    git = dossier_git()
    engendre = fond_seulement or any(
        os.path.exists(os.path.join(git, f)) for f in ("MERGE_HEAD", "REVERT_HEAD")
    )
    if not engendre:
        sujet = lignes[0]
        if not SUJET.match(sujet):
            erreurs.append(
                f"sujet non conforme — « {sujet} »\n"
                f"    attendu : <type>(<portée>)?: <description en minuscules, sans point final>\n"
                f"    types   : {TYPES.replace('|', ', ')}"
            )
        if len(sujet) > LONGUEUR_MAX:
            erreurs.append(f"sujet trop long : {len(sujet)} caractères (max {LONGUEUR_MAX})")
        if len(lignes) > 1 and lignes[1]:
            erreurs.append("la deuxième ligne doit être vide (séparation sujet / corps)")

    if erreurs:
        print("Message de commit refusé :", file=sys.stderr)
        for e in erreurs:
            print(f"  - {e}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
