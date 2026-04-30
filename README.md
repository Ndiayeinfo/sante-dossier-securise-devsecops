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
4. **SAST (optionnel)** : SonarQube si **`RUN_SONAR`** est coché au lancement du build **ou** si la variable du job **`SONARQUBE_ENABLED=true`** (serveur Jenkins nommé **`SonarQube`**, identifiant de secret **`sonar-token`**).
5. **Build & push image (Docker Hub)** : build d’une image OCI via `mvn spring-boot:build-image` puis push sur Docker Hub (credentials Jenkins, aucun secret dans Git).

Clé d’analyse Sonar : **`sante-dossier-api`** (`SONAR_PROJECT_KEY` dans le `Jenkinsfile`).

#### Publication Docker Hub (image applicative)

- Image : **`ndiayeinf/dossier-sante-api`** (variable `DOCKERHUB_REPOSITORY` dans le `Jenkinsfile`)
- Tags poussés :
  - **`${BUILD_NUMBER}`**
  - **`latest`**
- Exécution du push :
  - **jamais** depuis une Pull Request (si job multibranch, `CHANGE_ID` détecté)
  - autorisé sur `main`, `master` et **`equipe/youssou`** (adapter si besoin)

**Credential Jenkins requis (Docker Hub)** :
- **Manage Jenkins → Credentials → System → Global**
- Ajouter un credential **“Username with password”**
  - **ID** : `dockerhub` (par défaut dans `DOCKERHUB_CREDENTIALS_ID`)
  - **Username** : votre user Docker Hub (ex. `ndiayeinf`)
  - **Password** : un **Access Token** Docker Hub

### SonarQube en local (Docker)

Le fichier `jenkins/docker-compose.yml` lance aussi **SonarQube Community** sur le port **9000**.

1. Mémoire : SonarQube demande souvent **≥ 4 Go** de RAM pour le conteneur ; augmentez la mémoire allouée à Docker Desktop si le conteneur redémarre en boucle.

2. Démarrage (réseau commun avec Jenkins pour l’URL interne `http://sonarqube:9000`) :

   ```bash
   cd jenkins && docker compose up -d
   ```

3. Interface : **http://localhost:9000** — connexion initiale **`admin` / `admin`**, puis **changement de mot de passe** imposé.

4. Créer un **token utilisateur** : **My Account → Security → Generate Tokens** (ex. nom : `jenkins`), copier le token.

5. **Jenkins** (une fois les plugins installés : **SonarQube Scanner**) :
   - **Manage Jenkins → Credentials → System → Global** : ajouter un secret de type **Secret text**, identifiant **`sonar-token`**, contenu = le token Sonar.
   - **Manage Jenkins → System → SonarQube servers** : ajouter une installation nommée exactement **`SonarQube`**, URL **`http://sonarqube:9000`** (nom du service Docker), cocher **Server authentication token** → credential **`sonar-token`**.

6. Lancer le pipeline avec Sonar :
   - cocher **`RUN_SONAR`** dans **Build with Parameters**, **ou**
   - définir une variable d’environnement sur le job **`SONARQUBE_ENABLED=true`** (si vous utilisez le plugin *Environment Injector* ou équivalent).

### Lancer Jenkins localement (Docker)

Depuis le dossier `jenkins/` :

```bash
docker compose up -d
docker compose logs -f jenkins
```

- Jenkins : **http://localhost:8098** (le port **8098** évite le conflit avec l’API Spring Boot sur **8090**).
- SonarQube : **http://localhost:9000**
- Mot de passe initial du wizard :

```bash
docker exec -it sante-dossier-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

> Note : si le volume Docker `jenkins_home` existe déjà (Jenkins déjà initialisé), ce fichier peut ne pas exister.  
> Pour réinitialiser Jenkins et forcer la régénération du wizard : `docker compose down -v` puis `docker compose up -d`.

Prérequis Jenkins : plugins **Pipeline**, **Docker Pipeline** (agents Docker), **SonarQube Scanner** (pour l’étape Sonar et `withSonarQubeEnv`), Docker Desktop avec socket monté (voir `jenkins/docker-compose.yml`).

Créer un job **Pipeline** pointant sur ce dépôt (SCM) avec chemin du script : `Jenkinsfile` à la racine du repo.

### Déclencher le pipeline à chaque modification (Poll ou webhook)

Deux approches courantes pour satisfaire l’exigence « build après chaque changement » :

#### A) Poll SCM (simple, Jenkins interroge Git)

Adapté au **PC local** : pas besoin d’exposer Jenkins sur Internet.

1. Ouvrir la configuration du job Pipeline → section **Build Triggers** (Déclencheurs de build).
2. Cocher **Poll SCM**.
3. Saisir une planification **cron**, par exemple :
   - `H/5 * * * *` — toutes les **5 minutes** (le `H` répartit la charge ; variante `*/5 * * * *`).
   - `H * * * *` — toutes les heures (démo peu chargée).

À chaque intervalle, Jenkins interroge le dépôt ; si un **nouveau commit** est détecté sur la branche suivie, le build démarre.

#### B) Webhook GitHub / GitLab (réaction immédiate au push)

GitHub ou GitLab envoie une requête HTTP à Jenkins **au moment du push**. Jenkins doit être joignable depuis Internet (port ouvert, HTTPS conseillé). **`http://localhost:8098`** ne fonctionne pas depuis les serveurs GitHub/GitLab : utilisez une **VM cloud**, ou un **tunnel** vers votre machine (**ngrok**, **Cloudflare Tunnel**, **localhost.run**, etc.) pour obtenir une URL du type `https://abc123.ngrok-free.app` pointant vers `localhost:8098`.

---

##### GitHub

**Plugins Jenkins** : **GitHub Integration** (id `github-integration`) ou **GitHub** (`github`) selon votre version ; les deux gèrent le point d’entrée webhook standard.

**1. Job Pipeline (script from SCM)**

- Configurer le dépôt Git sous **Pipeline** (URL HTTPS du repo, credentials si besoin).
- Dans **Build Triggers**, cocher **« GitHub hook trigger for GITScm polling »**.  
  (Le libellé peut varier légèrement ; l’idée est : le webhook GitHub déclenche une **analyse SCM immédiate**, puis le build si le commit a changé.)

**2. Webhook côté dépôt GitHub**

- Repo → **Settings** → **Webhooks** → **Add webhook**.
- **Payload URL** : `https://<VOTRE_JENKINS_PUBLIC>/github-webhook/`  
  Exemple avec tunnel : `https://xxxx.ngrok-free.app/github-webhook/` (sans oublier le `/` final selon les versions).
- **Content type** : `application/json`.
- **Secret** (optionnel) : si vous configurez un secret dans Jenkins (**Manage Jenkins → GitHub** / réglages du plugin), mettre la même valeur ici.
- **Which events** : au minimum **Just the push event** (ou « Let me select » → **Pushes**).
- Enregistrer : GitHub envoie un **ping** ; la pastille du webhook doit passer au vert.

**3. Credentials utiles**

- **Manage Jenkins → Configure System → GitHub** : serveur GitHub + éventuellement PAT (**Personal Access Token**) avec droits **`repo`** pour que Jenkins clone les dépôts privés.
- Pour créer le webhook **depuis** Jenkins (si le plugin le propose) : PAT avec **`admin:repo_hook`** sur le dépôt concerné.

**4. Test**

- `git commit --allow-empty -m "ci: test webhook"` puis `git push`.
- Dans Jenkins : le build doit apparaître dans les **seconds** suivant le push (vérifier **Build History** et les **logs système** en cas d’échec).

---

##### GitLab (gitlab.com ou auto-hébergé)

**Plugin Jenkins** : installer **GitLab** (GitLab Plugin) depuis le gestionnaire d’extensions.

**1. Job Pipeline**

- Même principe : Pipeline from SCM avec l’URL du projet GitLab.
- **Build Triggers** : cocher **Build when a change is pushed to GitLab** (libellé exact selon version du plugin). Indiquer l’**URL du projet GitLab** et un **secret token** (chaîne que vous choisissez ; il servira aussi côté GitLab).

**2. Webhook côté GitLab**

- Projet → **Settings** → **Webhooks**.
- **URL** : selon la doc du **GitLab Plugin**, en général :  
  `https://<VOTRE_JENKINS_PUBLIC>/project/<CHEMIN_DU_JOB>`  
  où `<CHEMIN_DU_JOB>` est le **nom complet du job** avec `/` encodés en **`%2F`**.  
  Exemple : job nommé `dossier-sante-api` à la racine →  
  `https://jenkins.exemple.org/project/dossier-sante-api`  
  Exemple : job dans dossier `projet/api` →  
  `https://jenkins.exemple.org/project/projet%2Fapi`  
- Cocher **Push events** ; optionnel **Merge request events** si vous voulez déclencher sur les MR.
- Coller le **même secret** que dans la config du job (champ **Secret token** du trigger GitLab).

**3. Test**

- **Test** → **Push events** depuis l’interface GitLab, ou push réel sur une branche suivie par le job.

---

##### Dépôt / livrable

Conservez une **capture** de l’écran GitHub (**Webhooks**) ou GitLab (**Webhooks**) montrant l’URL et l’état vert, plus une **capture** du build Jenkins déclenché au bon moment.

Si vous restez en **local sans tunnel**, préférez **Poll SCM** (section A) pour le même exigence « après chaque modification », avec un intervalle court (`H/5 * * * *`).

### Rapport vulnérabilités (livrable Projet 2)

Un **canevas** de rapport (structure, tableaux à remplir) est proposé dans **`docs/rapport-vulnerabilites-projet2.md`**. Complétez-le avec les extraits de **OWASP Dependency-Check**, **Gitleaks**, **SonarQube** et les actions réellement menées sur le code ou les dépendances.

## Workflow Git — branches protégées

Les pushes **directs** sur **`main`** et **`develop`** doivent être **interdits** sur GitHub : travail sur `equipe/<prénom>` (ou autre branche), puis **pull requests**.  
Procédure pour l’admin du dépôt : voir **`docs/github-branch-protection.md`**.  
Hook local optionnel : dossier **`githooks/`** (voir `githooks/README.md`).

## Fichiers de configuration sensibles

- `app.security.encryption-key-base64` : en production, fournir la clé via **secret manager** (Vault, KMS, variables chiffrées CI), jamais en clair dans le dépôt.
