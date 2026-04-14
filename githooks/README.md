# Hooks Git (optionnel)

## pre-push

Bloque les `git push` lorsque la branche courante est `main` ou `develop`.

**Linux / macOS / Git Bash (Windows)** :

```bash
cd /chemin/vers/projet1-application-sante
git config core.hooksPath githooks
chmod +x githooks/pre-push   # si besoin
```

Ensuite, un push depuis `main` ou `develop` sera refusé côté client.  
La protection effective pour tout le monde reste la **branch protection** sur GitHub (`docs/github-branch-protection.md`).
