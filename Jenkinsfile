@Library('webplatform-jenkins-library')
import fleetcomplete.Common

def common = new Common()


pipeline {
  agent {
    kubernetes {
      yaml common.loadKubernetesYaml('openjdk11')
    }
  }
  options {
        ansiColor('xterm')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
  }
  stages {
    stage('Build Jenkins plugin') {
      steps {
        container('build') {
          sh './mvnw install'
          sh './mvnw verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar'
        }
      }
    }
  }
  post {
    always {
        archiveArtifacts artifacts: 'target/entigo-pipeline.hpi', onlyIfSuccessful: true
        cleanWs()
    }
  }
}