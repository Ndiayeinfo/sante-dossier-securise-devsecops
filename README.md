# Projet 1 — Application santé (données sensibles)

Ce dépôt correspond au **Projet 1** décrit dans le document *Intégration de la sécurité dans le cycle de développement (DevSecOps)* :

- **Objectif** : identifier, classifier et sécuriser les données sensibles d’une application **santé** ; formaliser des exigences de sécurité et une architecture cible (security by design, moindre privilège, traçabilité).
- **Projet 2 (DevSecOps)** : le pipeline Jenkins est dans ce même dépôt (`Jenkinsfile` à la racine, dossier `jenkins/` pour lancer Jenkins en Docker).

Le référentiel *Livrables Projet Sec* (exigences fonctionnelles, specs techniques performance / maintenance / sécurité, architecture, plan de conformité, MVP) sert de grille : cette API en est une **implémentation MVP** centrée sur la confidentialité et la traçabilité des données de santé.

## Fonctionnalités (MVP)

- **Patients** : identité, date de naissance, identifiant de couverture (NIR / mutuelle) **chiffré au repos** en base (AES-256-GCM).
- **Dossier médical** : résumé, notes infirmières, **notes cliniques** chiffrées au repos et **visibles uniquement pour le rôle médecin** (RBAC).
- **Journal d’accès** (`data_access_audit`) : qui a lu / listé / créé quelle ressource, horodatage, IP (base pour conformité CDP / preuves de traçabilité).
- **Authentification** : HTTP Basic avec comptes de **démonstration** (à remplacer par IAM / MFA en production).

## Rôles (RBAC)

| Utilisateur Basic | Rôle | Capacités principales |
|-------------------|------|------------------------|
| `medecin` / `changeit` | MEDECIN | CRUD patient (création avec secrétaire), dossier complet y compris notes cliniques |
| `infirmier` / `changeit` | INFIRMIER | Dossier (sans notes cliniques) ; saisie notes infirmières uniquement |
| `secretaire` / `changeit` | SECRETAIRE | Création patient, consultation identité avec **identifiant de couverture masqué** |
| `admin_secu` / `changeit` | ADMIN_SECURITE | Lecture des **200 derniers** événements d’audit (`GET /api/security/audit`) |

## Prérequis

- Java 17, Maven 3.9+
- Docker (pour MySQL local)

## Démarrage

1. Base de données :

   ```bash
   docker compose up -d
   ```

   MySQL écoute sur le port **3307** (pour éviter un conflit avec d’autres projets sur 3306).

2. Application :

   ```bash
   mvn spring-boot-run
   ```

   API : `http://localhost:8090`  
   Swagger UI : `http://localhost:8090/swagger-ui.html` (redirige vers l’UI ; **springdoc-openapi** en **2.8.x** pour compatibilité Spring Boot 3.5).  
   La spécification OpenAPI (`/v3/api-docs`) et l’interface Swagger sont **accessibles sans login** pour éviter l’erreur « Failed to load API definition ». Les routes **`/api/**`** restent protégées : cliquez sur **Authorize**, choisissez **basicAuth**, saisissez par ex. `medecin` / `changeit`, puis testez les endpoints.

3. Tests :

   ```bash
   mvn verify
   ```

   Le profil `test` utilise une base **H2** en mémoire (aucun conteneur requis).

## Projet 2 — Jenkins & pipeline

### Contenu du `Jenkinsfile`

1. **Build & tests** : `mvn verify` (module unique à la racine).
2. **Secrets** : Gitleaks → artefact `gitleaks.sarif` (SARIF).
3. **SCA** : OWASP Dependency-Check (HTML sous `dependency-check/`, analyseur .NET désactivé pour ce projet Java).
4. **SAST (optionnel)** : SonarQube si la variable d’environnement du job **`SONARQUBE_ENABLED=true`** (serveur Jenkins nommé **`SonarQube`**, identifiant de secret **`sonar-token`**).

Clé d’analyse Sonar : **`sante-dossier-api`** (`SONAR_PROJECT_KEY` dans le `Jenkinsfile`).

### Lancer Jenkins localement (Docker)

Depuis le dossier `jenkins/` :

```bash
docker compose up -d
docker compose logs -f jenkins
```

- UI : **http://localhost:8098** (le port **8098** évite le conflit avec l’API Spring Boot sur **8090**).
- Mot de passe initial du wizard :

```bash
docker exec -it sante-dossier-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Prérequis Jenkins : plugin **Pipeline**, plugin **Docker Pipeline** (agents Docker), Docker Desktop avec socket monté (voir `jenkins/docker-compose.yml`).

Créer un job **Pipeline** pointant sur ce dépôt (SCM) avec chemin du script : `Jenkinsfile` à la racine du repo.

## Fichiers de configuration sensibles

- `app.security.encryption-key-base64` : en production, fournir la clé via **secret manager** (Vault, KMS, variables chiffrées CI), jamais en clair dans le dépôt.
