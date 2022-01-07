FROM jenkins/jenkins:lts

RUN mkdir $JENKINS_HOME/casc_configs
COPY ./jenkins.yaml $JENKINS_HOME/casc_configs/jenkins.yaml
ENV CASC_JENKINS_CONFIG=$JENKINS_HOME/casc_configs

ENV JAVA_OPTS=-Djenkins.install.runSetupWizard=false

# install the "ip" executable called by se.bjurr.jenkinssandbox.JenkinsSandboxUtils#getHostIp()
USER root
RUN apt-get update \
      && apt-get install -y iproute2 \
      && rm -rf /var/lib/apt/lists/*
USER jenkins

RUN install-plugins.sh \
  configuration-as-code \
  blueocean \
  job-dsl \
  cloudbees-folder \
  workflow-aggregator \
  pipeline-utility-steps \
  generic-webhook-trigger \
  git-changelog \
  violation-comments-to-gitlab

COPY ./jenkins-wrapper.sh /usr/local/bin/jenkins-wrapper.sh

ENTRYPOINT ["/sbin/tini", "--", "/usr/local/bin/jenkins-wrapper.sh"]