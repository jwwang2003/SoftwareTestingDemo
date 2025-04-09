# Use the official MySQL image from Docker Hub
FROM mysql:latest

# Set environment variables for MySQL root user and password
ENV MYSQL_ROOT_PASSWORD=password
ENV MYSQL_DATABASE=demo_db

# Copy the provided SQL file into the container
COPY demo_db.sql /docker-entrypoint-initdb.d/init.sql

# Expose MySQL default port
EXPOSE 3306

# Default command to run MySQL server
CMD ["mysqld"]