# Jenkins Configuration as Code Sandbox

This is an example of Jenkins Configuration as Code with GitLab.

This is a fully working CI/CD setup with Jenkins and GitLab where everything is put under version control:

 * Global Jenkins settings - With [Jenkins Configuration as Code](https://jenkins.io/projects/jcasc/).
 * Jenkins job configurations - With [Job DSL](https://github.com/jenkinsci/job-dsl-plugin/wiki).
 * Jenkins build process - With [Pipelines](https://jenkins.io/doc/book/pipeline/) and [Shared Library](https://jenkins.io/doc/book/pipeline/shared-libraries/).
 * Application source code - With [GitLab](https://docs.gitlab.com/ce/).

This allows for you to:

 * Do local development of the CI/CD setup. Giving self confidence to do refactorings and keep all the scripts clean.
 * Allow code review of changes to the installation. Enable anyone to contribute to an innovative build CI/CD process.
 * Manage different installations in different branches. Push to the branch of installation **X** and **X** will automatically be re-configured.

It also demonstrates a pattern where a "*contract*" is established between application repositories and the infrastructure.  A `jenkins-settings.json`-file is created in the application repositories to tweak the build process. This means:

 * The pipeline code can be developed generically. Pipelines are not created to take care of a specific repo. Generic features are created, and documented, and the application developers chooses what features that should apply to thir repo. By editing `/jenkins-settings.json` in their repo. A default set of settings are derived and applied if no properties found in the repo. 
 * The solution becomes scalable and can handle a vast amount of application repositories. With low effort needed for support.

## Usage

**Start Environment**
```shell
docker-compose up -d 
```

GitLab will be available in a few moments at http://localhost/ and you can login with credentials found [here](gitlab-setup/config.txt).

Jenkins now available on: http://localhost:8080/

JobDSL docs available at: http://localhost:8080/plugin/job-dsl/api-viewer/index.html

**Destroy Environment**

```shell
docker-compose down -v --rmi local
```
all services are down and data lost

Enjoy!
