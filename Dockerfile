FROM openjdk:23-jdk-oracle AS builder

ARG COMPILE_DIR=/compiledir

WORKDIR ${COMPILE_DIR}

COPY mvnw .
COPY pom.xml .
COPY .mvn .mvn 
COPY src src

# set executable permission for mvnw
RUN chmod a+x ./mvnw

# package the application
RUN ./mvnw package -Dmaven.test.skip=true
# /compiledir/target/demo-0.0.1-SNAPSHOT.jar

# set env
ENV PORT=8080
ENV SPRING_REDIS_USERNAME=""
ENV SPRING_REDIS_PASSWORD=""
ENV SPRING_REDIS_HOST=localhost 
ENV SPRING_REDIS_PORT=6379 
ENV SPRING_REDIS_DATABASE=0 

EXPOSE ${PORT}

# not needed here as entrypoint should belong in second stage
# ENTRYPOINT java -jar target/demo-0.0.1-SNAPSHOT.jar

# day 18 - slide 13
# second stage
FROM openjdk:23-jdk-oracle

ARG WORK_DIR=/app

WORKDIR ${WORK_DIR}

# copy file /compiledir/target/demo-0.0.1-SNAPSHOT.jar and rename as demo.jar
COPY --from=builder /compiledir/target/bookreviews-0.0.1-SNAPSHOT.jar bookreviews.jar
# include any reference files you need to include but are not needed in the build here
# eg. a csv file to read data from: COPY customers-100.csv

ENV PORT=8080
ENV SPRING_REDIS_USERNAME=""
ENV SPRING_REDIS_PASSWORD=""
ENV SPRING_REDIS_HOST=localhost 
ENV SPRING_REDIS_PORT=6379 
ENV SPRING_REDIS_DATABASE=0

EXPOSE ${PORT}

# run the jar file
ENTRYPOINT SERVER_PORT=${PORT} java -jar bookreviews.jar

# still requires defining of 'healthy' by your standards (eg. in the form of a GET /health request)
# CMD line is to run the GET /health handler
# backslash \ at end of line is to break a line
HEALTHCHECK --interval=60s --timeout=10s --start-period=5s --retries=3 \
CMD curl -s -f http://localhost:${PORT}/health || exit 1
