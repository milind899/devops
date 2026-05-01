pipeline {
    agent any
    environment {
        DOCKER_IMAGE = 'healthcare-app'
        DOCKER_TAG = "v${env.BUILD_NUMBER}"
        KUBECONFIG_CREDENTIAL_ID = 'k8s-kubeconfig'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('DevSecOps: SAST & SCA') {
            steps {
                sh 'npm install'
                sh 'npm audit --audit-level=high || true'
            }
        }
        stage('Build Image') {
            steps {
                script {
                    dockerImage = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                }
            }
        }
        stage('DevSecOps: Container Scan') {
            steps {
                sh "trivy image --severity HIGH,CRITICAL ${DOCKER_IMAGE}:${DOCKER_TAG}"
            }
        }
        stage('Deploy Blue-Green') {
            steps {
                withCredentials([file(credentialsId: KUBECONFIG_CREDENTIAL_ID, variable: 'KUBECONFIG')]) {
                    script {
                        def activeColor = sh(script: "kubectl get svc my-app-service-active -o jsonpath='{.spec.selector.color}' --kubeconfig=$KUBECONFIG 2>/dev/null || echo 'none'", returnStdout: true).trim()
                        
                        def newColor = activeColor == 'blue' ? 'green' : 'blue'
                        def oldColor = activeColor == 'blue' ? 'blue' : 'green'
                        
                        sh "cp k8s-deployment.yaml deploy-${newColor}.yaml"
                        sh "sed -i 's|DEPLOYMENT_COLOR|${newColor}|g; s|DOCKER_IMAGE:DOCKER_TAG|${DOCKER_IMAGE}:${DOCKER_TAG}|g' deploy-${newColor}.yaml"
                        sh "kubectl apply -f deploy-${newColor}.yaml --kubeconfig=$KUBECONFIG"
                        
                        sh "kubectl rollout status deployment/my-app-${newColor} --kubeconfig=$KUBECONFIG"
                        
                        sh "cp k8s-service-active.yaml active-svc.yaml"
                        sh "sed -i 's|ACTIVE_COLOR|${newColor}|g' active-svc.yaml"
                        sh "kubectl apply -f active-svc.yaml --kubeconfig=$KUBECONFIG"
                        
                        if (activeColor != 'none') {
                            sh "kubectl scale deployment my-app-${oldColor} --replicas=0 --kubeconfig=$KUBECONFIG"
                        }
                    }
                }
            }
        }
    }
}
