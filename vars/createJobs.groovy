package se.bjurr.jenkinssandbox

import se.bjurr.jenkinssandbox.*

import static se.bjurr.jenkinssandbox.JenkinsSandboxUtils.*

def call() {
  node {
    //Need to replace localhost with hostIp when working locally.
    def hostIp = getHostIp(this)

    def jenkinsUrl = env.jenkinsUrl.replace('localhost',hostIp.trim())
    def gitlabUrl = env.gitlabUrl.replace('localhost',hostIp.trim())

    def restClient = new RestClient(this, gitlabUrl)

    stage('Checkout') {
      deleteDir()
      sh """
      git clone ${jenkinsGitRepo} work --depth 1
      """
    }

    def repos = []
    stage("Gather repos") {
      def json = restClient
        .get('/api/v4/projects')
      repos = json.collect {
        [
          name: it.name,
          path: it.path,
          namespacePath: it.namespace.path,
          namespaceName: it.namespace.name,
          description: it.description,
          cloneUrl: it.http_url_to_repo.replace('localhost',hostIp.trim()),
          url: it.web_url,
          id: it.id,
          default_branch: it.default_branch
        ]
      }
    }

    stage("Create jobs") {
      jobDsl targets: 'work/jobs/*.groovy',
        removedJobAction: 'DELETE',
        removedViewAction: 'DELETE',
        lookupStrategy: 'SEED_JOB',
        additionalClasspath: [].join('\n'),
        additionalParameters: [repos: repos]
    }

    stage('Configure webhooks') {
      repos.each { repo->
        println ""
        println " Configuring ${repo.name}"
        println ""

        restClient
          .get('/api/v4/projects/' + repo.id + '/hooks')
          .each { hook ->
            if (hook.url.endsWith(repo.path)) {
              restClient
                .delete('/api/v4/projects/' + repo.id + '/hooks/' + hook.id)
            }
          }

        restClient
          .post(
            '/api/v4/projects/' + repo.id + '/hooks'
            + '?url='+java.net.URLEncoder.encode(jenkinsUrl+'/generic-webhook-trigger/invoke?token='+ repo.path, "UTF-8")
            + '&push_events=true'
            + '&merge_requests_events=true'
            + '&tag_push_events=true'
            + '&note_events=true')
      }
    }

    stage('Cleanup') {
      deleteDir()
    }
  }
}