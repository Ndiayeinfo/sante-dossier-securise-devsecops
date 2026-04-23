# Rapport — Vulnérabilités détectées et corrections (Projet 2 DevSecOps)

**Projet :** dossier-sante-api  
**Auteur(s) :**  
**Date :**

---

## 1. Contexte et objectif

Décrire en quelques phrases l’objectif du pipeline sécurisé (build Maven, Gitleaks, OWASP Dependency-Check, SonarQube) et le périmètre du rapport (branche analysée, version du commit ou tag).

---

## 2. Outils et méthodologie

| Outil | Rôle | Fichiers / artefacts produits |
|--------|------|-------------------------------|
| Maven (`mvn verify`) | Build et tests | Console Jenkins |
| Gitleaks | Détection de secrets | `gitleaks.sarif` |
| OWASP Dependency-Check | Vulnérabilités des dépendances (SCA) | `dependency-check/*.html` |
| SonarQube | Analyse statique / qualité (SAST) | Tableau de bord Sonar, issues |

Indiquer la **date** des analyses et si les étapes étaient **bloquantes** ou **informatives** (`|| true` dans le `Jenkinsfile` pour Gitleaks et OWASP).

---

## 3. Synthèse des résultats

### 3.1 OWASP Dependency-Check (SCA)

- Nombre de dépendances analysées (indicatif) :  
- Nombre de vulnérabilités signalées (par sévérité si disponible : Critical / High / Medium / Low) :  

**Exemple de tableau (à compléter avec vos résultats réels) :**

| Dépendance (groupId:artifactId) | Version | CVE / identifiant | Sévérité | Statut |
|----------------------------------|---------|-------------------|----------|--------|
| … | … | … | … | Corrigée / Acceptée / Faux positif |

### 3.2 Gitleaks (secrets)

- Fuite détectée : oui / non  
- Si oui : type (clé API, mot de passe, token) et fichier concerné (sans recopier le secret en clair dans le rapport).  
- Action prise : suppression, `.gitignore`, rotation du secret, règle Gitleaks, etc.

### 3.3 SonarQube (SAST / qualité)

- Lien ou capture du tableau de bord projet (`sante-dossier-api` ou clé équivalente).  
- Bugs / vulnérabilités / code smells relevés (chiffres globaux).  

**Exemple de tableau :**

| Type (Bug / Vulnerability / Code Smell) | Fichier | Description courte | Correction appliquée |
|----------------------------------------|---------|--------------------|-----------------------|
| … | … | … | … |

---

## 4. Vulnérabilités ou problèmes retenus pour correction

Pour **au moins une ou deux** entrées significatives, détailler :

1. **Constat** : extrait du rapport (CVE, règle Sonar, alerte OWASP).  
2. **Analyse** : pourquoi c’est problématique dans un contexte données de santé / sécurité.  
3. **Correction** :  
   - mise à jour de version dans `pom.xml` ;  
   - ou modification de code (extrait avant/après ou référence au commit) ;  
   - ou décision documentée (risque résiduel, faux positif, exclusion justifiée).  
4. **Vérification** : nouveau build / nouveau scan montrant la disparition ou la réduction du finding.

---

## 5. Synthèse et limites

- Ce que le pipeline **automatise** utilement pour la sécurité.  
- Limites : faux positifs Dependency-Check, temps d’analyse, nécessité de **revue humaine** et de politique de mise à jour des dépendances.  
- Pistes d’amélioration (quality gate Sonar, seuils bloquants, intégration IDE, etc.).

---

## Annexes (optionnel)

- Captures d’écran : job Jenkins avec **Poll SCM** ou **webhook**, build déclenché après push.  
- Hash du commit analysé : `…………`  
- Lien vers artefacts archivés dans Jenkins (si dépôt interne).
