# Spring Batch Database Backup POC

A complete Spring Batch application for backing up old records from MySQL database with Kubernetes deployment support.

## Overview

This application demonstrates how to use Spring Batch to automatically backup old records from multiple microservices' database tables. It simulates three microservices:
- **Order Service** - Manages order records
- **Customer Service** - Manages customer records  
- **Product Service** - Manages product records

The application automatically backs up:
- Orders older than 90 days
- Inactive customers (no activity for 90+ days)
- Discontinued products

## Architecture

```
┌─────────────────────────────────────────┐
│     Spring Batch Application            │
│  ┌─────────────────────────────────┐   │
│  │   Backup Scheduler               │   │
│  │   (Daily at 2 AM)                │   │
│  └────────────┬────────────────────┘   │
│               │                          │
│  ┌────────────▼────────────────────┐   │
│  │   Order Backup Job               │   │
│  │   - Reader → Processor → Writer  │   │
│  └──────────────────────────────────┘   │
│  ┌──────────────────────────────────┐   │
│  │   Customer Backup Job            │   │
│  │   - Reader → Processor → Writer  │   │
│  └──────────────────────────────────┘   │
│  ┌──────────────────────────────────┐   │
│  │   Product Backup Job             │   │
│  │   - Reader → Processor → Writer  │   │
│  └──────────────────────────────────┘   │
└─────────────┬────────────────────────────┘
              │
     ┌────────▼────────┐
     │  MySQL Database │
     │  ┌────────────┐ │
     │  │Source Tables│ │
     │  │Backup Tables│ │
     │  └────────────┘ │
     └─────────────────┘
```

## Project Structure

```
spring-batch-demo/
├── src/
│   ├── main/
│   │   ├── java/com/example/batchbackup/
│   │   │   ├── BatchBackupApplication.java      # Main application
│   │   │   ├── config/
│   │   │   │   └── BatchConfiguration.java      # Batch job configuration
│   │   │   ├── model/
│   │   │   │   ├── order/                       # Order entities
│   │   │   │   ├── customer/                    # Customer entities
│   │   │   │   └── product/                     # Product entities
│   │   │   ├── repository/
│   │   │   │   ├── order/                       # Order repositories
│   │   │   │   ├── customer/                    # Customer repositories
│   │   │   │   └── product/                     # Product repositories
│   │   │   └── scheduler/
│   │   │       └── BackupScheduler.java         # Job scheduler
│   │   └── resources/
│   │       └── application.yml                  # Application configuration
│   └── test/
├── db-scripts/
│   ├── schema.sql                               # Database schema
│   └── sample-data.sql                          # Sample data for testing
├── k8s/
│   ├── mysql-deployment.yaml                    # MySQL K8s resources
│   └── app-deployment.yaml                      # Application K8s resources
├── Dockerfile                                    # Docker image build
├── pom.xml                                       # Maven dependencies
└── README.md
```

## Technologies Used

- **Spring Boot 3.1.5** - Application framework
- **Spring Batch** - Batch processing framework
- **Spring Data JPA** - Data access layer
- **MySQL 8.0** - Database
- **Kubernetes** - Container orchestration
- **Docker** - Containerization
- **Maven** - Build tool
- **Java 17** - Programming language

## Prerequisites

### For Local Development
- Java 17 or higher
- Maven 3.8+
- MySQL 8.0
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### For Kubernetes Deployment
- Docker
- Kubernetes cluster (Minikube, Docker Desktop, or cloud provider)
- kubectl CLI

## Local Setup and Testing

### 1. Database Setup

Start MySQL and create the database:

```bash
# Start MySQL (if using Docker)
docker run --name mysql-batch \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=batch_backup_db \
  -e MYSQL_USER=batchuser \
  -e MYSQL_PASSWORD=password \
  -p 3306:3306 \
  -d mysql:8.0
```

### 2. Initialize Database Schema

```bash
# Connect to MySQL
mysql -h localhost -u root -prootpassword batch_backup_db < db-scripts/schema.sql

# Insert sample data
mysql -h localhost -u root -prootpassword batch_backup_db < db-scripts/sample-data.sql
```

### 3. Configure Application

Update `src/main/resources/application.yml` if needed (default values work for local Docker MySQL):

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/batch_backup_db
    username: batchuser
    password: password
```

### 4. Build the Application

```bash
mvn clean package
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR:

```bash
java -jar target/spring-batch-backup-1.0.0.jar
```

### 6. Verify the Application

Check application health:
```bash
curl http://localhost:8080/actuator/health
```

The backup jobs will run automatically at 2 AM daily. For testing, you can modify the cron expression in `application.yml` to run more frequently:

```yaml
backup:
  schedule:
    cron: "0 */5 * * * ?"  # Run every 5 minutes for testing
```

### 7. Monitor Backup Execution

Check the application logs to see batch jobs executing:

```bash
# Check logs for job execution
tail -f logs/application.log
```

Sample output:
```
2026-01-05 02:00:00 - Starting scheduled backup jobs...
2026-01-05 02:00:05 - Order backup job completed successfully
2026-01-05 02:00:10 - Customer backup job completed successfully
2026-01-05 02:00:15 - Product backup job completed successfully
```

### 8. Verify Backup Data

```sql
-- Check backed up orders
SELECT COUNT(*) FROM orders_backup;

-- Check backed up customers
SELECT COUNT(*) FROM customers_backup;

-- Check backed up products
SELECT COUNT(*) FROM products_backup;

-- View sample backup data
SELECT * FROM orders_backup LIMIT 5;
```

## Kubernetes Deployment

### 1. Build Docker Image

```bash
docker build -t spring-batch-backup:latest .
```

### 2. Load Image to Kubernetes (for Minikube)

```bash
# If using Minikube
minikube image load spring-batch-backup:latest

# If using Docker Desktop, no need to load
```

### 3. Deploy to Kubernetes

```bash
# Create namespace and deploy MySQL
kubectl apply -f k8s/mysql-deployment.yaml

# Wait for MySQL to be ready
kubectl wait --for=condition=ready pod -l app=mysql -n batch-backup --timeout=300s

# Deploy the application
kubectl apply -f k8s/app-deployment.yaml

# Wait for application to be ready
kubectl wait --for=condition=ready pod -l app=spring-batch-backup -n batch-backup --timeout=300s
```

### 4. Verify Deployment

```bash
# Check all resources
kubectl get all -n batch-backup

# Check pod status
kubectl get pods -n batch-backup

# Check logs
kubectl logs -f -n batch-backup -l app=spring-batch-backup

# Check MySQL logs
kubectl logs -f -n batch-backup -l app=mysql
```

### 5. Initialize Database in Kubernetes

```bash
# Copy SQL scripts to MySQL pod
kubectl cp db-scripts/schema.sql batch-backup/$(kubectl get pod -n batch-backup -l app=mysql -o jsonpath='{.items[0].metadata.name}'):/tmp/

kubectl cp db-scripts/sample-data.sql batch-backup/$(kubectl get pod -n batch-backup -l app=mysql -o jsonpath='{.items[0].metadata.name}'):/tmp/

# Execute SQL scripts
kubectl exec -n batch-backup $(kubectl get pod -n batch-backup -l app=mysql -o jsonpath='{.items[0].metadata.name}') -- mysql -u root -prootpassword batch_backup_db < /tmp/schema.sql

kubectl exec -n batch-backup $(kubectl get pod -n batch-backup -l app=mysql -o jsonpath='{.items[0].metadata.name}') -- mysql -u root -prootpassword batch_backup_db < /tmp/sample-data.sql
```

Or connect directly:

```bash
# Get MySQL pod name
MYSQL_POD=$(kubectl get pod -n batch-backup -l app=mysql -o jsonpath='{.items[0].metadata.name}')

# Port forward MySQL
kubectl port-forward -n batch-backup $MYSQL_POD 3306:3306 &

# Run scripts from local
mysql -h 127.0.0.1 -u root -prootpassword batch_backup_db < db-scripts/schema.sql
mysql -h 127.0.0.1 -u root -prootpassword batch_backup_db < db-scripts/sample-data.sql
```

### 6. Access Application Health Check

```bash
# Port forward the application
kubectl port-forward -n batch-backup svc/spring-batch-backup 8080:8080 &

# Check health
curl http://localhost:8080/actuator/health

# Check metrics
curl http://localhost:8080/actuator/metrics
```

### 7. Monitor Jobs in Kubernetes

```bash
# Stream application logs
kubectl logs -f -n batch-backup -l app=spring-batch-backup

# Check batch job execution in database
kubectl exec -n batch-backup $(kubectl get pod -n batch-backup -l app=mysql -o jsonpath='{.items[0].metadata.name}') -- \
  mysql -u root -prootpassword batch_backup_db -e "SELECT * FROM BATCH_JOB_EXECUTION ORDER BY CREATE_TIME DESC LIMIT 5;"
```

## Configuration Options

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| DB_HOST | MySQL host | localhost |
| DB_PORT | MySQL port | 3306 |
| DB_NAME | Database name | batch_backup_db |
| DB_USERNAME | Database username | root |
| DB_PASSWORD | Database password | password |
| BACKUP_RETENTION_DAYS | Days before records are backed up | 90 |
| BACKUP_SCHEDULE_CRON | Cron expression for schedule | 0 0 2 * * ? |

### Kubernetes ConfigMap

Edit `k8s/app-deployment.yaml` to modify:
- Backup retention days
- Schedule cron expression
- Database connection settings

### Application Properties

Edit `src/main/resources/application.yml` to configure:
- JPA/Hibernate settings
- Logging levels
- Actuator endpoints
- Batch job settings

## Backup Strategy

### What Gets Backed Up

1. **Orders**: All orders with `created_date` older than retention period (default 90 days)
2. **Customers**: Inactive customers with `last_activity_date` older than retention period
3. **Products**: Discontinued products with `updated_date` older than retention period

### Backup Process

1. **Reader**: Queries source tables for records matching backup criteria
2. **Processor**: Transforms source records to backup records, adding `backup_date` and `original_id`
3. **Writer**: Batch writes backup records to backup tables

### Scheduling

- Default: Daily at 2:00 AM
- Configurable via cron expression
- Each microservice backup runs as a separate job
- Jobs run sequentially with logging

## Troubleshooting

### Common Issues

1. **MySQL Connection Refused**
   ```bash
   # Check MySQL is running
   kubectl get pods -n batch-backup
   
   # Check MySQL logs
   kubectl logs -n batch-backup -l app=mysql
   ```

2. **Application Won't Start**
   ```bash
   # Check application logs
   kubectl logs -n batch-backup -l app=spring-batch-backup
   
   # Check config
   kubectl get configmap -n batch-backup app-config -o yaml
   ```

3. **Jobs Not Running**
   - Check scheduler is enabled: `@EnableScheduling` in main class
   - Verify cron expression in application.yml
   - Check logs for scheduler execution

4. **No Data Being Backed Up**
   - Verify sample data is older than retention period
   - Check query filters in repository classes
   - Adjust retention days in configuration

### Cleanup

```bash
# Delete all Kubernetes resources
kubectl delete namespace batch-backup

# Stop local MySQL Docker container
docker stop mysql-batch
docker rm mysql-batch
```

## Testing the POC

### Test Scenario

1. **Deploy everything**: Follow Kubernetes deployment steps
2. **Verify initial state**: Check source tables have data
3. **Trigger backup manually** (optional):
   - Modify cron to run every minute: `"0 * * * * ?"`
   - Redeploy application
4. **Wait for scheduled execution**: Check logs at 2 AM or after cron trigger
5. **Verify backup**: Query backup tables to see copied records
6. **Check metadata**: Verify `backup_date` and `original_id` are populated

### Sample Verification Queries

```sql
-- Compare source and backup counts
SELECT 
  (SELECT COUNT(*) FROM orders WHERE created_date < DATE_SUB(NOW(), INTERVAL 90 DAY)) as old_orders,
  (SELECT COUNT(*) FROM orders_backup) as backed_up_orders;

-- Verify backup integrity
SELECT o.order_number, o.created_date, ob.backup_date
FROM orders o
JOIN orders_backup ob ON o.id = ob.original_id
LIMIT 5;
```

## Extending the POC

### Adding More Microservices

1. Create new entity classes in `model` package
2. Create corresponding backup entity classes
3. Add repositories with custom queries
4. Add reader, processor, and writer beans in `BatchConfiguration`
5. Create new job and step beans
6. Add job execution in `BackupScheduler`

### Adding Manual Triggers

Create a REST controller:

```java
@RestController
@RequestMapping("/api/backup")
public class BackupController {
    
    @Autowired
    private BackupScheduler scheduler;
    
    @PostMapping("/orders")
    public void triggerOrderBackup() throws Exception {
        scheduler.runOrderBackup();
    }
}
```

### Adding Backup Cleanup

Add a cleanup job to delete very old backup records:

```java
@Scheduled(cron = "0 0 3 * * ?")
public void cleanupOldBackups() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(365);
    orderBackupRepository.deleteByBackupDateBefore(cutoff);
}
```

## Performance Considerations

- **Chunk Size**: Currently set to 100 records per chunk. Adjust based on memory and performance.
- **Database Indexes**: Added on date columns for query performance.
- **Resource Limits**: Kubernetes deployment has memory/CPU limits configured.
- **Connection Pooling**: Spring Boot auto-configures HikariCP for connection pooling.

## Security Best Practices

- Database credentials stored in Kubernetes Secrets
- Passwords not hardcoded in application
- Use environment variables for sensitive data
- Network policies can be added for pod-to-pod communication
- Consider using sealed secrets for production

## License

This is a POC (Proof of Concept) project for demonstration purposes.

## Support

For issues or questions, please create an issue in the repository.
