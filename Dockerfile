FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Сначала только pom.xml: слой с зависимостями кэшируется и не скачивается заново при каждой правке кода
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q package -DskipTests


FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/target/*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
