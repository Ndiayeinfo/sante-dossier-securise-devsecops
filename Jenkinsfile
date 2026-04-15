pipeline {
  agent any

  options {
    timestamps()
  }

  environment {
    MAVEN_IMAGE = 'maven:3.9.9-eclipse-temurin-17'
    SONAR_PROJECT_KEY = 'sante-dossier-api'
    SONAR_HOST_URL = 'http://sonarqube:9000'
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build + Dependencies') {
      steps {
        script {
          docker.image(env.MAVEN_IMAGE).inside {
            sh "mvn clean compile"
          }
        }
      }
    }

    stage('Tests Unitaires') {
      steps {
        script {
          docker.image(env.MAVEN_IMAGE).inside {
            sh "mvn test"
          }
        }
      }
    }

    stage('Build Package') {
      steps {
        script {
          docker.image(env.MAVEN_IMAGE).inside {
            sh "mvn package -DskipTests"
          }
        }
      }
    }

    stage('OWASP Dependency Check (Vulnérabilités)') {
      steps {
        script {
          docker.image(env.MAVEN_IMAGE).inside {
            sh "mvn org.owasp:dependency-check-maven:check"
          }
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        script {
          withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
            docker.image(env.MAVEN_IMAGE).inside {
              sh """
                mvn sonar:sonar \
                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                -Dsonar.host.url=${SONAR_HOST_URL} \
                -Dsonar.login=${SONAR_TOKEN}
              """
            }
          }
        }
      }
    }
  }

  post {
    success {
      echo "✅ Pipeline réussi : code build + tests + scans OK"
    }

    failure {
      echo "❌ Pipeline échoué : vérifier logs"
    }
  }
}