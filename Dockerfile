FROM dockerfile/java
MAINTAINER Alexander Petrov <alexetrov.rb@gmail.com>

RUN sudo apt-get update

ADD target/kmg-0.1.0-SNAPSHOT-standalone.jar /srv/kmg.jar

EXPOSE 3000

CMD ["java", "-jar", "/srv/kmg.jar"]
