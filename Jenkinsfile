pipeline {
    agent any

    triggers {
        pollSCM('* * * * *')
    }

    tools {
        maven 'MAVEN'
        jdk 'JDK17'
        nodejs 'node18'
    }

    environment {
        MVN_OPTS = '-B'
        HOME = '/tmp/jenkins-home'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('SERVICES : Build & Test') {
            steps {
                script {
                    def services = ['movie-service']
                    for (service in services) {
                        dir("services/${service}") {
                            sh "mvn clean install dependency:copy-dependencies"
                        }
                    }
                }
            }
        }

        stage('FRONTEND : Build & Test') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('SONARQUBE ANALYSIS') {
            steps {
                script {
                    def scannerHome = tool 'SonarQubeScanner'
                    withSonarQubeEnv('SonarQube') { 
                        sh "${scannerHome}/bin/sonar-scanner"
                    }
                }
            }
        }
    }
}