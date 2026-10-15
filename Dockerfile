# Etapa 1: compila o projeto com Maven e Java 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode -DskipTests package

# Etapa 2: imagem final só com o Java (menor e mais rápida)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/classes ./classes
ENV PORT=8080
EXPOSE 8080
CMD ["java", "-cp", "classes", "barbearia.ServidorWeb"]
