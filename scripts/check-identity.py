#!/usr/bin/env python3
"""Contrôle de l'identité de l'auteur avant chaque commit (registre G.52).

Le crochet commit-msg ne voit que le message : un commit dont l'AUTEUR porte le
nom ou l'adresse d'un outil d'assistance le traverserait. Ce contrôle lit
l'identité qui sera inscrite.

Acceptés délibérément (G.52, corollaire) : les adresses masquées
`…@users.noreply.github.com`. Seuls les noms et domaines d'outils sont refusés.
"""
import os
import re
import subprocess
import sys

OUTILS = re.compile(
    r"(claude|anthropic|chatgpt|openai|copilot|gemini|cursor|codeium|tabnine|\[bot\])",
    re.IGNORECASE,
)


def identite(variable):
    resultat = subprocess.run(["git", "var", variable], capture_output=True, text=True)
    if resultat.returncode != 0:
        print(f"Identité git absente ({variable}) : configurer user.name et user.email.",
              file=sys.stderr)
        sys.exit(1)
    # Format : « Nom <adresse> horodatage fuseau »
    return resultat.stdout.rsplit(" ", 2)[0]


def identite_configuree():
    """Sans user.name/user.email, git fabrique une identité depuis le nom du
    poste (utilisateur@machine.local) et l'inscrit dans l'historique public.
    Les variables GIT_AUTHOR_* explicites comptent comme une configuration."""
    for cle, variable in (("user.name", "GIT_AUTHOR_NAME"), ("user.email", "GIT_AUTHOR_EMAIL")):
        if os.environ.get(variable):
            continue
        r = subprocess.run(["git", "config", cle], capture_output=True, text=True)
        if r.returncode != 0 or not r.stdout.strip():
            return f"{cle} n'est pas configuré : git utiliserait le nom du poste"
    return None


def main():
    erreurs = []
    manque = identite_configuree()
    if manque:
        erreurs.append(manque + " — git config user.name / user.email")
    for variable, role in (("GIT_AUTHOR_IDENT", "auteur"), ("GIT_COMMITTER_IDENT", "validateur")):
        valeur = identite(variable)
        if OUTILS.search(valeur):
            erreurs.append(f"{role} refusé — « {valeur} » : identité d'outil")
    if erreurs:
        print("Commit refusé :", file=sys.stderr)
        for e in erreurs:
            print(f"  - {e}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
