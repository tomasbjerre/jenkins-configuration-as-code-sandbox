FROM jenkins/jenkins:lts

RUN mkdir ${JENKINS_HOME}/casc_configs
COPY ./jenkins.yaml ${JENKINS_HOME}/casc_configs/jenkins.yaml
ENV CASC_JENKINS_CONFIG=${JENKINS_HOME}/casc_configs

ENV JAVA_OPTS=-Djenkins.install.runSetupWizard=false

RUN install-plugins.sh \
 configuration-as-code:experimental

