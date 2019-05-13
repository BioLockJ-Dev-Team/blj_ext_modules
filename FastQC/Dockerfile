# Deployment path:  $BLJ/resources/docker/FastQC/Dockerfile

## Create and test image
# docker build -t local/fastqc $BLJ/resources/docker/FastQC
# docker run -it -v $BLJ_SUP/resources/test/data/wgs_fastq/:/input local/fastqc /bin/bash
# fastqc /input/3A_1.fq.gz
## see output in $BLJ_SUP/resources/test/data/wgs_fastq/

FROM biolockj/blj_basic_java
ARG DEBIAN_FRONTEND=noninteractive

#1.) Install FastQC
ENV FASTQC_VER=0.11.8
ENV BASE_URL=https://www.bioinformatics.babraham.ac.uk/projects/fastqc/fastqc_v
ENV FASTQC_URL=${BASE_URL}${FASTQC_VER}.zip
ENV BUILD_DIR=/usr/local/bin
RUN cd $BUILD_DIR &&\
	wget -qO- --no-check-certificate $FASTQC_URL | bsdtar -xzpf- && \
	chmod -R a+x FastQC
ENV PATH $PATH:$BUILD_DIR/FastQC

#2.) Avoid Assistive Technology error (see https://github.com/Microsoft/vscode-arduino/issues/644)
RUN rm /etc/java-8-openjdk/accessibility.properties

#3.) Cleanup
RUN	apt-get clean && \
	find / -name *python* | xargs rm -rf && \
	rm -rf /tmp/* && \
	rm -rf /var/cache/* && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /var/log/*
