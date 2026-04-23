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
          docker.image(env.MAVEN_IMAGE).inside {
            sh "mvn -q ${env.MAVEN_CLI_OPTS} verify"
          }
        }
      }
    }

    stage('Secrets scan (Gitleaks)') {
      steps {
        script {
          docker.image(env.GITLEAKS_IMAGE).inside("--entrypoint=''") {
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
          docker.image(env.MAVEN_IMAGE).inside {
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
              docker.image(env.MAVEN_IMAGE).inside {
                sh "mvn ${env.MAVEN_CLI_OPTS} -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.projectKey=${env.SONAR_PROJECT_KEY} verify sonar:sonar"
              }
            }
          }
        }
      }
    }
  }
}
