pipeline {
  agent any

  options {
    timestamps()
  }

  parameters {
    booleanParam(
      name: 'RUN_SONAR',
      defaultValue: false,
      description: 'Exécuter SonarQube (SAST). Peut aussi être forcé via variable de job SONARQUBE_ENABLED=true.'
    )
  }

  environment {
    MAVEN_CLI_OPTS = '-B -Dmaven.test.failure.ignore=false'
    MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
    GITLEAKS_VERSION = '8.21.2'
    MAVEN_IMAGE = 'maven:3.9.9-eclipse-temurin-17'
    GITLEAKS_IMAGE = 'zricethezav/gitleaks:v8.21.2'
    SONAR_PROJECT_KEY = 'sante-dossier-api'
    // Permet aux conteneurs lancés par Jenkins (docker.image().inside)
    // de résoudre host.docker.internal sur Linux (Docker Desktop / WSL2).
    DOCKER_INSIDE_OPTS = '--add-host=host.docker.internal:host-gateway'

    // Docker Hub (ne pas mettre de secrets ici : utiliser un credential Jenkins)
    DOCKERHUB_REPOSITORY = 'ndiayeinf/dossier-sante-api'
    DOCKERHUB_CREDENTIALS_ID = 'dockerhub'

    // Jenkins tourne en conteneur : les conteneurs Maven lancés par Docker Pipeline
    // ne résolvent pas forcément le hostname docker-compose "sonarqube".
    // Sur Docker Desktop, host.docker.internal permet d’atteindre le port publié (9000).
    SONAR_HOST_URL_OVERRIDE = 'http://host.docker.internal:9000'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Test (Maven)') {
      steps {
        script {
          docker.image(env.MAVEN_IMAGE).inside(env.DOCKER_INSIDE_OPTS) {
            sh "mvn -q ${env.MAVEN_CLI_OPTS} verify"
          }
        }
      }
    }

    stage('Secrets scan (Gitleaks)') {
      steps {
        script {
          docker.image(env.GITLEAKS_IMAGE).inside("${env.DOCKER_INSIDE_OPTS} --entrypoint=''") {
            sh "gitleaks version"
            sh "gitleaks detect --source . --no-git --report-format sarif --report-path gitleaks.sarif --redact || true"
          }
        }
      }
      post {
        always {
          archiveArtifacts artifacts: 'gitleaks.sarif', allowEmptyArchive: true
        }
      }
    }

    stage('SCA (OWASP Dependency-Check)') {
      steps {
        script {
          docker.image(env.MAVEN_IMAGE).inside(env.DOCKER_INSIDE_OPTS) {
            sh """
              mvn -q org.owasp:dependency-check-maven:check \
                -Dformat=HTML \
                -DassemblyAnalyzerEnabled=false \
                -DoutputDirectory=dependency-check \
                || true
            """.stripIndent().trim()
          }
        }
      }
      post {
        always {
          archiveArtifacts artifacts: 'dependency-check/**', allowEmptyArchive: true
        }
      }
    }

    stage('SAST / Qualité (SonarQube)') {
      when {
        expression {
          def fromEnv = (env.SONARQUBE_ENABLED ?: '').trim().equalsIgnoreCase('true')
          def fromParam = params.RUN_SONAR == true
          return fromEnv || fromParam
        }
      }
      steps {
        script {
          withSonarQubeEnv('SonarQube') {
            withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
              docker.image(env.MAVEN_IMAGE).inside(env.DOCKER_INSIDE_OPTS) {
                // Éviter l’interpolation Groovy du secret (warning Jenkins) :
                // on passe le token via variable d’environnement shell ($SONAR_TOKEN).
                sh """
                  mvn ${env.MAVEN_CLI_OPTS} \
                    -Dsonar.host.url=${env.SONAR_HOST_URL_OVERRIDE} \
                    -Dsonar.login=\\$SONAR_TOKEN \
                    -Dsonar.projectKey=${env.SONAR_PROJECT_KEY} \
                    verify sonar:sonar
                """.stripIndent().trim()
              }
            }
          }
        }
      }
    }

    stage('Build & Push (Docker Hub)') {
      when {
        expression {
          // Ne pas pousser depuis une PR (Multibranch)
          if ((env.CHANGE_ID ?: '').trim()) {
            return false
          }

          // Si job non-multibranch, BRANCH_NAME peut être vide : on autorise dans ce cas.
          def branch = (env.BRANCH_NAME ?: env.GIT_BRANCH ?: '').trim()
          if (!branch) {
            return true
          }

          // Push sur branches autorisées (adapter à votre stratégie)
          def allowedBranches = [
            'main',
            'master',
            'equipe/youssou'
          ]

          // Jenkins peut exposer "main", "origin/main", "refs/remotes/origin/main", etc.
          def normalized = branch
            .replaceFirst('^refs/remotes/', '')
            .replaceFirst('^remotes/', '')
            .replaceFirst('^origin/', '')

          return allowedBranches.any { b ->
            normalized == b || normalized.endsWith("/${b}")
          }
        }
      }
      steps {
        script {
          def imageTag = "${env.BUILD_NUMBER}"
          def imageNameWithTag = "${env.DOCKERHUB_REPOSITORY}:${imageTag}"
          def imageNameLatest = "${env.DOCKERHUB_REPOSITORY}:latest"

          // Build OCI image via Buildpacks (Spring Boot) dans le daemon Docker de l’agent Jenkins.
          docker.image(env.MAVEN_IMAGE).inside("${env.DOCKER_INSIDE_OPTS} -v /var/run/docker.sock:/var/run/docker.sock") {
            sh """
              mvn -q ${env.MAVEN_CLI_OPTS} -DskipTests \
                spring-boot:build-image \
                -Dspring-boot.build-image.imageName=${imageNameWithTag}
            """.stripIndent().trim()
          }

          withCredentials([usernamePassword(credentialsId: env.DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_TOKEN')]) {
            sh """
              echo "\$DOCKERHUB_TOKEN" | docker login -u "\$DOCKERHUB_USER" --password-stdin
              docker tag ${imageNameWithTag} ${imageNameLatest}
              docker push ${imageNameWithTag}
              docker push ${imageNameLatest}
              docker logout
            """.stripIndent().trim()
          }
        }
      }
    }
  }
}
