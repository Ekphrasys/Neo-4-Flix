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
                    dir('services') {
                        for (service in services) {
                            sh "mvn clean install -pl ${service} -am"
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
    }
}