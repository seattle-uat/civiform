FROM adoptopenjdk/openjdk11:jdk-11.0.10_9-alpine-slim AS stage1

# sbt

ENV SBT_VERSION 1.6.1
ENV INSTALL_DIR /usr/local
ENV SBT_HOME /usr/local/sbt
ENV PATH ${PATH}:${SBT_HOME}/bin

RUN apk update

# Install sbt
RUN apk add --no-cache --update bash wget && mkdir -p "$SBT_HOME" && \
    wget -qO - --no-check-certificate "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" |  tar xz -C $INSTALL_DIR && \
    echo -ne "- with sbt $SBT_VERSION\n" >> /root/.built

# Install git
RUN apk add --no-cache git openssh

# Install node.js
RUN apk add --update npm

# Copy play project and compile it.
# This will download all the ivy2 and sbt dependencies and install them
# in the container /root directory, which saves time on future runs,
# even if the dependencies change slightly.

ENV PROJECT_HOME /usr/src
ENV PROJECT_NAME universal-application-tool-0.0.1

COPY ${PROJECT_NAME}/build.sbt ${PROJECT_HOME}/${PROJECT_NAME}/
COPY ${PROJECT_NAME}/project ${PROJECT_HOME}/${PROJECT_NAME}/project
RUN cd $PROJECT_HOME/$PROJECT_NAME && sbt update

COPY ${PROJECT_NAME} ${PROJECT_HOME}/${PROJECT_NAME}
RUN cd $PROJECT_HOME/$PROJECT_NAME && npm install && \
    sbt dist

# This is a common trick to shrink container sizes.  we just throw away all that build stuff and use only the jars
# we built with sbt dist.
FROM adoptopenjdk/openjdk11:jdk-11.0.10_9-alpine-slim AS stage2
COPY --from=stage1 /usr/src/universal-application-tool-0.0.1/target/universal/universal-application-tool-0.0.1.zip /civiform.zip
RUN apk add bash nodejs npm
RUN unzip /civiform.zip; chmod +x /universal-application-tool-0.0.1/bin/universal-application-tool

CMD ["/universal-application-tool-0.0.1/bin/universal-application-tool", "-Dconfig.file=/universal-application-tool-0.0.1/conf/application.conf"]
