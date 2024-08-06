FROM mcr.microsoft.com/openjdk/jdk:17-mariner

WORKDIR /app

COPY build/libs/translator-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
