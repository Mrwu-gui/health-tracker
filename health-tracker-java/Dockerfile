FROM docker.m.daocloud.io/library/maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY maven-settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src
RUN mvn -q -s /root/.m2/settings.xml -DskipTests package

FROM docker.m.daocloud.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
