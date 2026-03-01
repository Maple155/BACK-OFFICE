# ==============================
# Stage 1: Build WAR with Maven
# ==============================
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml ./
COPY lib ./lib
COPY src ./src

RUN mvn -B clean package -DskipTests

# =============================================
# Stage 2: Run application with Tomcat 10 (JDK 17)
# =============================================
FROM tomcat:10.1-jdk17-temurin

WORKDIR /usr/local/tomcat

RUN rm -rf webapps/*
COPY --from=build /app/target/location.war webapps/ROOT.war

EXPOSE 8080

CMD ["sh", "-c", "if [ -n \"$PORT\" ]; then sed -i \"s/port=\\\"8080\\\"/port=\\\"$PORT\\\"/\" conf/server.xml; fi; catalina.sh run"]
