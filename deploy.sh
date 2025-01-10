#!/bin/bash

# Create namespace
kubectl apply -f k8s/services/namespace.yaml

# Create RBAC resources
kubectl apply -f k8s/rbac/role.yaml
kubectl apply -f k8s/rbac/role-binding.yaml
kubectl apply -f k8s/rbac/service-account.yaml

# Create ConfigMaps and Secrets
kubectl apply -f k8s/services/configmap.yaml
kubectl apply -f k8s/services/mysql-configmap.yaml
kubectl apply -f k8s/services/service-discovery-config.yaml
kubectl apply -f k8s/services/secrets.yaml

# Deploy MySQL first
kubectl apply -f k8s/deployments/mysql-deployment.yaml
kubectl apply -f k8s/services/mysql-service.yaml

# Wait for MySQL to be ready
echo "Waiting for MySQL to be ready..."
kubectl wait --namespace=microservices \
  --for=condition=ready pod \
  --selector=app=mysql \
  --timeout=300s

# Deploy Eureka Server
kubectl apply -f k8s/deployments/eureka-deployment.yaml
kubectl apply -f k8s/services/eureka-service.yaml

# Wait for Eureka to be ready
echo "Waiting for Eureka to be ready..."
kubectl wait --namespace=microservices \
  --for=condition=ready pod \
  --selector=app=eureka-service \
  --timeout=300s

# Deploy microservices
kubectl apply -f k8s/deployments/car-service-deployment.yaml
kubectl apply -f k8s/deployments/client-service-deployment.yaml
kubectl apply -f k8s/deployments/gateway-deployment.yaml

# Deploy services
kubectl apply -f k8s/services/car-service.yaml
kubectl apply -f k8s/services/client-service.yaml
kubectl apply -f k8s/services/gateway-service.yaml

echo "Deployment completed. Checking pod status..."
kubectl get pods -n microservices