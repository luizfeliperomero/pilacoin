FROM maven:3.8.3-openjdk-17 AS build
COPY src /home/app/src
COPY pom.xml /home/app
COPY public-key /home/app
COPY private-key /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests

FROM openjdk:17-oracle
COPY --from=build /home/app/target/pilacoin-server.jar /usr/local/lib/pilacoin-server.jar
COPY --from=build /home/app/public-key /usr/local/lib/public-key
COPY --from=build /home/app/private-key /usr/local/lib/private-key
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/pilacoin-server.jar"]
