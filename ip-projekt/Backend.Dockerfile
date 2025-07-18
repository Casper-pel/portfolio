FROM gradle:8.11-jdk-21-and-23 AS backend

COPY backend/ /backend
WORKDIR /backend
RUN ./gradlew build --no-daemon --parallel -x test

FROM openjdk:21 AS server

# Kopiere das gebaute Jar
COPY --from=backend /backend/build/libs/ /app
# Kopiere wait-for-it.sh in das Image
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

WORKDIR /app

# Warten auf Mongo (Standard: Port 27017)
CMD ["sh", "-c", "sleep 10 && /wait-for-it.sh mongo:27017 -- java -jar backend-0.0.1-SNAPSHOT.jar"]
