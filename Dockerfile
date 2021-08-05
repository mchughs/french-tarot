FROM openjdk:8u181-alpine3.8

WORKDIR /

RUN mkdir app
COPY target/french-tarot.jar app/french-tarot.jar
# COPY resources/public app/resources/public
EXPOSE 3000

WORKDIR /app

CMD java -cp french-tarot.jar clojure.main -m user
