FROM openjdk:21-slim
WORKDIR /app
COPY build/libs/user-management-0.1.0.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
