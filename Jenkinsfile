pipeline {
    agent any

    triggers {
        pollSCM('* * * * *')
    }

    tools {
        maven 'MAVEN'
        jdk 'JDK17'
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
                nodejs(nodeJSInstallationName: 'node22', configId: '') {
                    dir('frontend') {
                        sh 'node -v'
                        sh 'npm -v'
                        sh 'npm ci'
                        sh 'npm run build'
                    }
                }
            }
        }

        stage('SONARQUBE ANALYSIS') {
            steps {
                script {
                    def scannerHome = tool 'SonarQubeScanner'
                    withSonarQubeEnv('SonarQube') {
                        nodejs(nodeJSInstallationName: 'node18', configId: '') {
                            sh 'node -v'
                            sh "${scannerHome}/bin/sonar-scanner"
                        }
                    }
                }
            }
        }
    }
}