# DevOps Assignment Submission: DevSecOps & Blue-Green Deployments

This project fulfills three core deliverables for the assignment:

## 1. CI/CD Pipeline to Deploy Dockerized Application on Kubernetes using Jenkins (5 Marks)
- **Implementation**: The `Jenkinsfile` fully automates the software delivery process. 
- **Details**: It includes stages for checking out the source code, building a Docker image (`docker.build`), pushing it to a container registry (`docker.withRegistry`), and deploying it directly to a Kubernetes cluster via `kubectl`. 

## 2. Automated Blue-Green Deployment Strategy (Jenkins, Kubernetes, Docker) (5 Marks)
- **Implementation**: The `Jenkinsfile` contains a dynamic "Deploy Blue-Green" stage, backed by parameterized `k8s-deployment.yaml` and `k8s-service-active.yaml` templates.
- **Details**: 
  - Jenkins queries Kubernetes to discover if the "blue" or "green" environment is currently active.
  - It deploys the newly built Docker image to the *inactive* color.
  - It waits for the new pods to become healthy (`kubectl rollout status`).
  - It seamlessly patches the LoadBalancer Service to point live traffic to the new color.
  - It scales down the old deployment to 0 to save resources, achieving true zero-downtime Blue-Green deployments.

## 3. Secure DevOps (DevSecOps) Implementation in a Healthcare System
- **Implementation**: Security is baked into every layer of the application (Code, Container, Cluster, and Pipeline), focusing on protecting sensitive Healthcare API endpoints.
- **Details**:
  - **Code Level**: The healthcare API (`server.js`) utilizes `helmet` for secure HTTP headers, `cors` to prevent cross-origin attacks, and regex-based input validation on patient IDs to prevent injection.
  - **Container Level**: The `Dockerfile` implements the principle of least privilege by creating and running as an unprivileged `appuser`.
  - **Pipeline Level (SAST & SCA)**: The Jenkinsfile executes `npm audit` to detect known vulnerabilities in open-source dependencies.
  - **Pipeline Level (Container Scanning)**: The Jenkinsfile uses **Trivy** (`trivy image --severity HIGH,CRITICAL`) to scan the built Docker image for OS-level vulnerabilities before allowing it to be pushed or deployed.
  - **Cluster Level**: The `k8s-deployment.yaml` uses a `securityContext` to explicitly forbid root execution (`runAsNonRoot: true`) and privilege escalation (`allowPrivilegeEscalation: false`).
