FROM eclipse-temurin:8u372-b07-jre-focal

ARG ENV

ARG DEBIAN_FRONTEND=noninteractive

# Install initial dependencies
RUN apt-get -y update && \
        apt-get install -y -q software-properties-common

ARG APT_KEY_DONT_WARN_ON_DANGEROUS_USAGE=DontWarn

# Add the apt.gbif.es repo and gpg key
RUN echo "deb [arch=amd64] https://apt.gbif.es/ bionic main" > /etc/apt/sources.list.d/ala.list && \
        apt-key adv --keyserver keyserver.ubuntu.com --recv-keys F697D8D2ADB9E24A

RUN wget https://github.com/mikefarah/yq/releases/download/v4.16.1/yq_linux_arm64.tar.gz -O - | tar xz
RUN mv yq_linux_arm64 /usr/bin/yq
RUN curl -o /usr/bin/docopts -LJO https://github.com/docopt/docopts/releases/download/v0.6.3-rc2/docopts_linux_arm
RUN chmod +x /usr/bin/docopts

# Install main dependencies
RUN apt-get install -y -q rsync tar curl
RUN apt-get -y update && apt-get install -y -q la-pipelines

RUN mkdir -p /data/dwca-tmp/
RUN mkdir -p /data/spark-tmp

COPY ./configs/la-pipelines-docker.yaml /data/la-pipelines/config/la-pipelines-local.yaml

CMD ["tail", "-f", "/dev/null"]
