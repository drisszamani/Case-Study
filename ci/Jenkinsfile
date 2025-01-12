pipeline {
    agent any

    tools {
        jdk 'JDK 17'
        maven 'Maven_3.9.9'
    }

    environment {
        DOCKER_REGISTRY = 'docker.io/drisszamanii'
        VERSION = '1.0.0'
        MAVEN_OPTS = '-Dmaven.repo.local=.m2'
    }

    options {
        timeout(time: 1, unit: 'HOURS')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Verify Tools') {
            steps {
                sh '''
                    java -version
                    mvn -version
                    docker --version
                    kubectl version --client
                '''
            }
        }

        stage('Clean Workspace') {
            steps {
                sh '''
                    mvn clean
                    rm -rf ~/.m2/repository/com/example/
                '''
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    def services = ['car', 'client', 'gateway', 'server_eureka']

                    for (service in services) {
                        dir(service) {
                            sh """
                                echo "Building ${service}..."
                                mvn package -DskipTests
                                echo "Testing ${service}..."
                                mvn test
                            """
                        }
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = [
                        [name: 'car', dir: 'car'],
                        [name: 'client', dir: 'client'],
                        [name: 'gateway', dir: 'gateway'],
                        [name: 'eureka', dir: 'server_eureka']
                    ]

                    services.each { service ->
                        dir(service.dir) {
                            sh """
                                echo "Building Docker image for ${service.name}..."
                                docker build \
                                    -t ${DOCKER_REGISTRY}/${service.name}-service:${VERSION} \
                                    -f ../docker/${service.name}/Dockerfile .
                            """
                        }
                    }
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker-credentials-id', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'

                        ['car', 'client', 'gateway', 'eureka'].each { service ->
                            sh """
                                echo "Pushing ${service} service image..."
                                docker push ${DOCKER_REGISTRY}/${service}-service:${VERSION}
                            """
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh '''
                        echo "Applying Kubernetes configurations..."
                        kubectl apply -f k8s/services/namespace.yaml
                        kubectl apply -f k8s/services/configmap.yaml
                        kubectl apply -f k8s/services/secrets.yaml
                        kubectl apply -f k8s/deployments/
                        kubectl apply -f k8s/services/

                        echo "Waiting for deployments to be ready..."
                        kubectl wait --for=condition=ready pod -l app=eureka-service --timeout=300s
                        kubectl wait --for=condition=ready pod -l app=car-service --timeout=300s
                        kubectl wait --for=condition=ready pod -l app=client-service --timeout=300s
                        kubectl wait --for=condition=ready pod -l app=gateway-service --timeout=300s
                    '''
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            sh 'docker system prune -f'
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed! Check the logs for details.'
        }
    }
}