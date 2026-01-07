#!/bin/bash

# Quick Start Script for Spring Batch Backup POC

set -e

echo "=========================================="
echo "Spring Batch Database Backup - Quick Start"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored messages
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi
print_success "Docker is installed"

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    print_info "kubectl is not installed. Kubernetes deployment will not be available."
    KUBECTL_AVAILABLE=false
else
    KUBECTL_AVAILABLE=true
    print_success "kubectl is installed"
fi

echo ""
echo "Choose deployment option:"
echo "1. Local with Docker MySQL"
echo "2. Kubernetes deployment (requires kubectl and cluster)"
read -p "Enter choice (1 or 2): " CHOICE

if [ "$CHOICE" == "1" ]; then
    echo ""
    print_info "Starting local deployment..."
    
    # Check if MySQL container already exists
    if docker ps -a --format '{{.Names}}' | grep -q "^mysql-batch$"; then
        print_info "MySQL container already exists. Starting it..."
        docker start mysql-batch || docker run --name mysql-batch \
          -e MYSQL_ROOT_PASSWORD=rootpassword \
          -e MYSQL_DATABASE=batch_backup_db \
          -e MYSQL_USER=batchuser \
          -e MYSQL_PASSWORD=password \
          -p 3306:3306 \
          -d mysql:8.0
    else
        print_info "Creating MySQL container..."
        docker run --name mysql-batch \
          -e MYSQL_ROOT_PASSWORD=rootpassword \
          -e MYSQL_DATABASE=batch_backup_db \
          -e MYSQL_USER=batchuser \
          -e MYSQL_PASSWORD=password \
          -p 3306:3306 \
          -d mysql:8.0
    fi
    
    print_success "MySQL container started"
    
    # Wait for MySQL to be ready
    print_info "Waiting for MySQL to be ready..."
    sleep 20
    
    # Initialize database
    print_info "Initializing database schema..."
    docker exec -i mysql-batch mysql -u root -prootpassword batch_backup_db < db-scripts/schema.sql
    print_success "Database schema created"
    
    print_info "Loading sample data..."
    docker exec -i mysql-batch mysql -u root -prootpassword batch_backup_db < db-scripts/sample-data.sql
    print_success "Sample data loaded"
    
    # Build the application
    print_info "Building application..."
    mvn clean package -DskipTests
    print_success "Application built successfully"
    
    echo ""
    print_success "Setup complete!"
    echo ""
    echo "To run the application:"
    echo "  mvn spring-boot:run"
    echo ""
    echo "Or run the JAR file:"
    echo "  java -jar target/spring-batch-backup-1.0.0.jar"
    echo ""
    echo "To check MySQL:"
    echo "  docker exec -it mysql-batch mysql -u root -prootpassword batch_backup_db"
    echo ""
    echo "To stop MySQL:"
    echo "  docker stop mysql-batch"
    echo ""

elif [ "$CHOICE" == "2" ]; then
    if [ "$KUBECTL_AVAILABLE" == false ]; then
        print_error "kubectl is not installed. Cannot proceed with Kubernetes deployment."
        exit 1
    fi
    
    echo ""
    print_info "Starting Kubernetes deployment..."
    
    # Build Docker image
    print_info "Building Docker image..."
    docker build -t spring-batch-backup:latest .
    print_success "Docker image built"
    
    # Check if using Minikube
    if command -v minikube &> /dev/null && minikube status &> /dev/null; then
        print_info "Detected Minikube, loading image..."
        minikube image load spring-batch-backup:latest
        print_success "Image loaded to Minikube"
    fi
    
    # Deploy to Kubernetes
    print_info "Deploying MySQL to Kubernetes..."
    kubectl apply -f k8s/mysql-deployment.yaml
    
    print_info "Waiting for MySQL to be ready..."
    kubectl wait --for=condition=ready pod -l app=mysql -n batch-backup --timeout=300s || true
    
    print_info "Deploying application to Kubernetes..."
    kubectl apply -f k8s/app-deployment.yaml
    
    print_info "Waiting for application to be ready..."
    kubectl wait --for=condition=ready pod -l app=spring-batch-backup -n batch-backup --timeout=300s || true
    
    # Get MySQL pod name
    MYSQL_POD=$(kubectl get pod -n batch-backup -l app=mysql -o jsonpath='{.items[0].metadata.name}')
    
    # Initialize database
    print_info "Initializing database..."
    kubectl cp db-scripts/schema.sql batch-backup/$MYSQL_POD:/tmp/schema.sql
    kubectl cp db-scripts/sample-data.sql batch-backup/$MYSQL_POD:/tmp/sample-data.sql
    kubectl exec -n batch-backup $MYSQL_POD -- sh -c "mysql -u root -prootpassword batch_backup_db < /tmp/schema.sql"
    kubectl exec -n batch-backup $MYSQL_POD -- sh -c "mysql -u root -prootpassword batch_backup_db < /tmp/sample-data.sql"
    print_success "Database initialized"
    
    echo ""
    print_success "Kubernetes deployment complete!"
    echo ""
    echo "Useful commands:"
    echo "  kubectl get all -n batch-backup"
    echo "  kubectl logs -f -n batch-backup -l app=spring-batch-backup"
    echo "  kubectl port-forward -n batch-backup svc/spring-batch-backup 8080:8080"
    echo ""
    echo "Access health check (after port-forward):"
    echo "  curl http://localhost:8080/actuator/health"
    echo ""
    echo "To cleanup:"
    echo "  kubectl delete namespace batch-backup"
    echo ""
else
    print_error "Invalid choice. Please run the script again."
    exit 1
fi
