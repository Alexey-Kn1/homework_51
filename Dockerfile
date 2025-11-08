FROM eclipse-temurin:24.0.2_12-jre

EXPOSE 5500

COPY build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
