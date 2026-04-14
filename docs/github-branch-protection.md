# Protéger `main` et `develop` sur GitHub

À faire par un **administrateur** du dépôt  
`Ndiayeinfo/sante-dossier-securise-devsecops` (compte avec droits *Settings*).

## 1. Règle pour `main`

1. GitHub → **Settings** → **Branches** → **Add branch protection rule**.
2. **Branch name pattern** : `main`
3. Activez au minimum :
   - **Require a pull request before merging**  
     → empêche tout push direct sur `main` : les changements passent par une **pull request** depuis une autre branche (ex. `develop`, `equipe/...`).
   - (Recommandé) **Require approvals** : **1** approbation avant merge.
   - (Recommandé) **Dismiss stale pull request approvals when new commits are pushed**
4. Optionnel : **Require status checks to pass before merging** (si vous branchez Jenkins / CI plus tard).
5. Pour que **personne** ne contourne la règle sans PR : cochez **Do not allow bypassing the above settings** (souvent sous une section *Merge restrictions* / administrateurs selon le plan GitHub).
6. **Create** ou **Save changes**.

## 2. Règle pour `develop`

Répétez avec une **deuxième** règle :

- **Branch name pattern** : `develop`
- Mêmes options : **Require a pull request before merging**, approbations si vous le souhaitez, etc.

Ainsi, les membres travaillent sur `equipe/<prénom>` (ou autre branche), ouvrent une **PR vers `develop`**, puis une PR **`develop` → `main`** quand c’est prêt pour une release.

## 3. Droits des collaborateurs

Dans **Settings** → **Collaborators** : les membres en **Write** peuvent pousser sur les branches **non protégées** ; avec les règles ci-dessus, ils **ne peuvent pas** pousser directement sur `main` ni `develop` s’ils doivent passer par une PR (comportement standard de la protection « Require a pull request »).

## 4. Hook local (optionnel)

Voir `githooks/README.md` : bloque un `git push` **depuis** votre machine si la branche courante est `main` ou `develop` (sécurité supplémentaire, pas un substitut aux règles GitHub).
