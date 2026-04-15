pipeline {
  agent any

  options {
    timestamps()
  }

  environment {
    MAVEN_CLI_OPTS = '-B -Dmaven.test.failure.ignore=false'
    MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
    MAVEN_IMAGE = 'maven:3.9.9-eclipse-temurin-17'
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

    stage('SAST / Qualité (SonarQube)') {
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
