FROM openjdk:8u181-alpine3.8

WORKDIR /

COPY target/french-tarot.jar french-tarot.jar
EXPOSE 3000

CMD java -cp french-tarot.jar clojure.main -m user
