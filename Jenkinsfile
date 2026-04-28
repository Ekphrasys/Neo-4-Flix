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
                dir('services') {
                    sh 'mvn clean install'
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