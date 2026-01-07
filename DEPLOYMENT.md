# Quick Deployment Guide

This guide provides step-by-step instructions to deploy and test the Spring Batch Database Backup POC on your local machine.

## Prerequisites Check

Before starting, ensure you have:
- ✅ Docker installed and running
- ✅ kubectl installed (for Kubernetes deployment)
- ✅ Java 17+ installed (for local development)
- ✅ Maven 3.8+ installed (for building)
- ✅ A Kubernetes cluster running (Minikube, Docker Desktop, or cloud provider)

## Option 1: Quick Start with Script

We provide a helper script that automates most of the setup:

```bash
# Make the script executable (if not already)
chmod +x quick-start.sh

# Run the script
./quick-start.sh
```

Follow the prompts to choose between:
1. Local deployment with Docker MySQL
2. Kubernetes deployment

## Option 2: Manual Local Setup

### Step 1: Start MySQL Database

```bash
docker run --name mysql-batch \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=batch_backup_db \
  -e MYSQL_USER=batchuser \
  -e MYSQL_PASSWORD=password \
  -p 3306:3306 \
  -d mysql:8.0
```

Wait ~20 seconds for MySQL to be ready.

### Step 2: Initialize Database

```bash
# Create schema
docker exec -i mysql-batch mysql -u root -prootpassword batch_backup_db < db-scripts/schema.sql

# Load sample data
docker exec -i mysql-batch mysql -u root -prootpassword batch_backup_db < db-scripts/sample-data.sql
```

### Step 3: Build Application

```bash
mvn clean package -DskipTests
```

### Step 4: Run Application

```bash
java -jar target/spring-batch-backup-1.0.0.jar
```

Or using Maven:

```bash
mvn spring-boot:run
```

### Step 5: Verify Application

```bash
# Check health
curl http://localhost:8080/actuator/health

# Expected output:
# {"status":"UP"}
```

### Step 6: Test Backup Job

The backup jobs run automatically at 2 AM daily. For immediate testing, modify `src/main/resources/application.yml`:

```yaml
backup:
  schedule:
    cron: "0 */1 * * * ?"  # Run every minute
```

Then restart the application and watch the logs:

```bash
# You should see logs like:
# Starting scheduled backup jobs...
# Order backup job completed successfully
# Customer backup job completed successfully
# Product backup job completed successfully
```

### Step 7: Verify Backups

```bash
# Connect to MySQL
docker exec -it mysql-batch mysql -u root -prootpassword batch_backup_db

# Run queries
SELECT COUNT(*) FROM orders;
SELECT COUNT(*) FROM orders_backup;
SELECT COUNT(*) FROM customers;
SELECT COUNT(*) FROM customers_backup;
SELECT COUNT(*) FROM products;
SELECT COUNT(*) FROM products_backup;
```

## Option 3: Kubernetes Deployment

### Step 1: Build Docker Image

```bash
docker build -t spring-batch-backup:latest .
```

### Step 2: Load Image to Cluster

For Minikube:
```bash
minikube image load spring-batch-backup:latest
```

For Docker Desktop: No action needed

### Step 3: Deploy MySQL

```bash
kubectl apply -f k8s/mysql-deployment.yaml

# Wait for MySQL to be ready
kubectl wait --for=condition=ready pod -l app=mysql -n batch-backup --timeout=300s
```

### Step 4: Initialize Database in Kubernetes

```bash
# Get MySQL pod name
MYSQL_POD=$(kubectl get pod -n batch-backup -l app=mysql -o jsonpath='{.items[0].metadata.name}')

# Copy SQL files
kubectl cp db-scripts/schema.sql batch-backup/$MYSQL_POD:/tmp/
kubectl cp db-scripts/sample-data.sql batch-backup/$MYSQL_POD:/tmp/

# Execute SQL
kubectl exec -n batch-backup $MYSQL_POD -- sh -c "mysql -u root -prootpassword batch_backup_db < /tmp/schema.sql"
kubectl exec -n batch-backup $MYSQL_POD -- sh -c "mysql -u root -prootpassword batch_backup_db < /tmp/sample-data.sql"
```

### Step 5: Deploy Application

```bash
kubectl apply -f k8s/app-deployment.yaml

# Wait for application to be ready
kubectl wait --for=condition=ready pod -l app=spring-batch-backup -n batch-backup --timeout=300s
```

### Step 6: Verify Deployment

```bash
# Check all resources
kubectl get all -n batch-backup

# Check logs
kubectl logs -f -n batch-backup -l app=spring-batch-backup
```

### Step 7: Access Application

```bash
# Port forward
kubectl port-forward -n batch-backup svc/spring-batch-backup 8080:8080 &

# Check health
curl http://localhost:8080/actuator/health
```

### Step 8: Monitor Backup Jobs

```bash
# Stream logs
kubectl logs -f -n batch-backup -l app=spring-batch-backup

# Or check batch job execution table
kubectl exec -n batch-backup $MYSQL_POD -- \
  mysql -u root -prootpassword batch_backup_db \
  -e "SELECT * FROM BATCH_JOB_EXECUTION ORDER BY CREATE_TIME DESC LIMIT 5;"
```

## Testing Scenarios

### Scenario 1: Verify Old Orders Backup

```sql
-- Check orders older than 90 days
SELECT * FROM orders WHERE created_date < DATE_SUB(NOW(), INTERVAL 90 DAY);

-- After backup job runs, check backup table
SELECT * FROM orders_backup;

-- Verify data integrity
SELECT o.order_number, o.created_date, ob.backup_date
FROM orders o
JOIN orders_backup ob ON o.id = ob.original_id
LIMIT 5;
```

### Scenario 2: Verify Inactive Customers Backup

```sql
-- Check inactive customers (no activity for 90+ days)
SELECT * FROM customers WHERE last_activity_date < DATE_SUB(NOW(), INTERVAL 90 DAY);

-- Check backup
SELECT * FROM customers_backup;
```

### Scenario 3: Verify Discontinued Products Backup

```sql
-- Check discontinued products
SELECT * FROM products WHERE status = 'DISCONTINUED' 
  AND updated_date < DATE_SUB(NOW(), INTERVAL 90 DAY);

-- Check backup
SELECT * FROM products_backup;
```

## Configuration Options

### Change Retention Period

Edit `src/main/resources/application.yml`:

```yaml
backup:
  retention:
    days: 60  # Change from 90 to 60 days
```

Or set environment variable:
```bash
export BACKUP_RETENTION_DAYS=60
```

### Change Schedule

Edit `src/main/resources/application.yml`:

```yaml
backup:
  schedule:
    cron: "0 0 3 * * ?"  # Run at 3 AM instead of 2 AM
```

Or set environment variable:
```bash
export BACKUP_SCHEDULE_CRON="0 0 3 * * ?"
```

## Troubleshooting

### Issue: MySQL connection refused

**Solution:**
```bash
# Check MySQL is running
docker ps | grep mysql-batch
# Or for Kubernetes
kubectl get pods -n batch-backup

# Check MySQL logs
docker logs mysql-batch
# Or for Kubernetes
kubectl logs -n batch-backup -l app=mysql
```

### Issue: Application won't start

**Solution:**
```bash
# Check application logs
# For local
tail -f logs/spring.log

# For Kubernetes
kubectl logs -n batch-backup -l app=spring-batch-backup
```

### Issue: No data being backed up

**Solution:**
1. Verify data is older than retention period (default 90 days)
2. Check that sample data was inserted correctly
3. Adjust retention days in configuration for testing:
   ```yaml
   backup:
     retention:
       days: 1  # Backup data older than 1 day
   ```

### Issue: Jobs not running on schedule

**Solution:**
1. Verify `@EnableScheduling` is present in main application class
2. Check cron expression is valid
3. Ensure `spring.batch.job.enabled=false` to prevent auto-start conflicts
4. Check application logs for scheduler execution

## Cleanup

### Local Deployment
```bash
# Stop application (Ctrl+C)

# Stop and remove MySQL container
docker stop mysql-batch
docker rm mysql-batch
```

### Kubernetes Deployment
```bash
# Delete all resources
kubectl delete namespace batch-backup

# This removes:
# - MySQL deployment and service
# - Application deployment and service
# - ConfigMaps and Secrets
# - PersistentVolumeClaim
```

## Next Steps

After successful deployment and testing:

1. **Monitor Performance**: Check batch job execution times in logs
2. **Adjust Chunk Size**: Modify chunk size in `BatchConfiguration.java` if needed
3. **Add More Services**: Extend the POC to include more microservices
4. **Setup Alerts**: Configure alerts for backup job failures
5. **Backup Cleanup**: Add a job to clean up very old backup records
6. **Production Setup**: 
   - Use proper secrets management (e.g., Sealed Secrets)
   - Set up proper resource limits
   - Configure persistent storage for MySQL
   - Set up monitoring and logging

## Support

For issues or questions:
- Check the main README.md for detailed documentation
- Review application logs for error messages
- Verify all prerequisites are met
- Check database connectivity and credentials
