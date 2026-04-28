pipeline {
    agent any

    triggers {
        pollSCM('* * * * *')
    }

    tools {
        maven 'maven-3.9.6'
        jdk 'jdk-17'
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
    }
}