FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/campuslink-career-1.0.0.jar /app/app.jar
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
ENTRYPOINT ["java","-jar","/app/app.jar"]
