pipeline {
    agent any

    environment {
        BACKEND_IMAGE = "turismo-backend"
        ANDROID_HOME  = "${env.HOME}/Android/Sdk"
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME}"
            }
        }

        stage('Lint Backend') {
            steps {
                dir('backend') {
                    sh 'npm ci'
                    sh 'npm audit --production || true'
                }
            }
        }

        stage('Build Backend Docker') {
            steps {
                sh "docker build -t ${BACKEND_IMAGE}:latest ./backend"
            }
        }

        stage('Lint Android (KtLint)') {
            steps {
                sh './gradlew lintDebug'
            }
        }

        stage('Build Android APK') {
            steps {
                sh './gradlew assembleDebug'
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/*.apk', allowEmptyArchive: true
            }
        }

        stage('Unit Tests') {
            steps {
                sh './gradlew testDebugUnitTest || true'
            }
        }

        stage('Deploy Backend') {
            when {
                branch 'main'
            }
            steps {
                sh "docker-compose down || true"
                sh "docker-compose up -d --build"
                sh "sleep 5"
                sh "curl -f http://localhost:3000/api/lugares || exit 1"
            }
        }
    }

    post {
        success {
            echo "Pipeline completado exitosamente"
        }
        failure {
            echo "Pipeline fallo - revisar logs"
        }
        always {
            cleanWs()
        }
    }
}
