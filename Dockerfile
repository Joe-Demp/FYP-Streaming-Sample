FROM openjdk:11.0.8-jre
COPY target/streaming-sample-0.0.1-SNAPSHOT.jar /app.jar
CMD java -jar /app.jar
