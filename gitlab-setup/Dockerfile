FROM ubuntu:20.04

RUN apt-get update && \
 apt-get install -y software-properties-common && \
 add-apt-repository -y ppa:deadsnakes/ppa && \
 apt-mark hold python2 python2-minimal python2.7 python2.7-minimal libpython2-stdlib libpython2.7-minimal libpython2.7-stdlib && \
 apt-get install -y \
 python3.9 \
 python3-pip \
 python-is-python3 \
 curl \
 gettext-base \
 git \
 && rm -rf /var/lib/apt/lists/*

RUN pip3 install requests beautifulsoup4 lxml retrying

RUN git config --global user.email "noone@nowhere.com" && \
    git config --global user.name "Provisioning Script"

COPY . /bootstrap

CMD ["/bootstrap/main.sh"]

