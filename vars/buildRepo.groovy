package se.bjurr.jenkinssandbox

import groovy.json.*
import se.bjurr.jenkinssandbox.*

import static groovy.json.JsonOutput.*
import static se.bjurr.jenkinssandbox.JenkinsSandboxUtils.*


def getChangelogTemplateString(params) {
    return """
<h1> ${params.repo.name} </h1>

<p>
Changelog of ${params.repo.description}.
</p>

{{#tags}}
<h2> {{name}} </h2>
 {{#issues}}
  {{#hasIssue}}
   {{#hasLink}}
<h2> {{name}} <a href="{{link}}">{{issue}}</a> {{title}} </h2>
   {{/hasLink}}
   {{^hasLink}}
<h2> {{name}} {{issue}} {{title}} </h2>
   {{/hasLink}}
  {{/hasIssue}}
  {{^hasIssue}}
<h2> {{name}} </h2>
  {{/hasIssue}}


   {{#commits}}
<a href="${params.repo.url}/commit/{{hash}}">{{hash}}</a> {{authorName}} <i>{{commitTime}}</i>
<p>
<h3>{{{messageTitle}}}</h3>

{{#messageBodyItems}}
 <li> {{.}}</li> 
{{/messageBodyItems}}
</p>


  {{/commits}}

 {{/issues}}
{{/tags}}
    """
}

def getParams(repo) {
    def paramsFromRepo = []
    def settingsFilename = 'jenkins-settings.json'

    node {
        stage("Determine params") {
            sh """
            git clone ${repo.cloneUrl} . --depth 1
            """
            try {
                paramsFromRepo = readJSON file: settingsFilename
                println "Found repo settings:\n\n" + prettyPrint(toJson(paramsFromRepo))+"\n\n"
            } catch (Throwable t) {
                println "Was unable to read " + settingsFilename + " in root of repository. Add it to the repo (${repo.cloneUrl}) if you want to adjust build process.\n" + t.getMessage()
            }
            deleteDir()
        }
    }

    def params = [
        skipTests: false,
        skipDoc: false,
        buildLabel: 'BUILD',
        snapshotBranch: 'master'
    ] + paramsFromRepo
    params.repo = repo //Doing this last to not allow override from settingsFilename

    println "Building " + env.JOB_NAME + " with: \n\n" + prettyPrint(toJson(params)) + "\n\n"

    return params
}

def commentMr(projectId, mergeRequestIid, comment) {
    def hostIp = getHostIp(this)
    new RestClient(this,env.gitlabUrl.replace('localhost',hostIp.trim()))
        .post(
            '/api/v4/projects/'+projectId+'/merge_requests/'+mergeRequestIid+'/notes',
            [ "body": comment ])
}

def buildMergeRequest(Map params) {
    //Need to replace localhost with hostIp when working locally.
    def hostIp = getHostIp(this)
    def MR_FROM_URL = env.MR_FROM_URL.replace('localhost',hostIp.trim())
    def MR_TO_URL = env.MR_TO_URL.replace('localhost',hostIp.trim())

    currentBuild.displayName = 'MR: ' + MR_TITLE

    commentMr(MR_PROJECT_ID, MR_IID, "Verifying ${env.BUILD_URL}")
    stage("Clone") {
        sh """
        git clone $MR_TO_URL --depth 100 .
        git reset --hard $MR_TO_BRANCH
        git status
        git remote add from $MR_FROM_URL
        git fetch from
        """
    }

    stage("Changelog") {
        def changelogString = gitChangelog returnType: 'STRING',
        from: [type: 'REF', value: MR_TO_BRANCH],
        to: [type: 'REF', value: MR_FROM_BRANCH],
        template: getChangelogTemplateString(params)
        currentBuild.description = changelogString
    }

    currentBuild.description = 'MR: ' + MR_URL + "\n\n" + currentBuild.description

    stage("Merge") {
        sh """
        git merge from/$MR_FROM_BRANCH
        git --no-pager log --max-count=10 --graph --abbrev-commit
        """
        commentMr(MR_PROJECT_ID, MR_IID, "Merge OK =) ${env.BUILD_URL}")
    }

    stage("Compile") {
        sh "./gradlew assemble"
        commentMr(MR_PROJECT_ID, MR_IID, "Compile OK =) ${env.BUILD_URL}")
    }

    if (!params.skipTests) {
        stage("Test") {
            sh "./gradlew test"
            commentMr(MR_PROJECT_ID, MR_IID, "Test OK =) ${env.BUILD_URL}")
        }
    }

    stage("Static code analysis") {
        sh "./gradlew build"
        commentMr(MR_PROJECT_ID, MR_IID, "Static Code Analysis OK =) ${env.BUILD_URL}")
    }

    stage("Report") {
        ViolationsToGitLab([
            apiTokenCredentialsId: 'gitlab-token', 
            apiTokenPrivate: true, 
            authMethodHeader: true, 
            commentOnlyChangedContent: true, 
            commentTemplate: '''**Reporter**: {{violation.reporter}}{{#violation.rule}} **Rule**: {{violation.rule}}{{/violation.rule}} **Severity**: {{violation.severity}}
{{#violation.source}}

**Source**: {{violation.source}}{{/violation.source}}

{{violation.message}}''', 
            createSingleFileComments: true, 
            gitLabUrl: env.gitlabUrl.replace('localhost',hostIp.trim()), 
            ignoreCertificateErrors: true, 
            keepOldComments: false, 
            mergeRequestIid: MR_IID, 
            minSeverity: 'INFO', 
            projectId: params.repo.namespacePath+'/'+params.repo.path, 
            violationConfigs: [
                [parser: 'FINDBUGS', pattern: '.*/spotbugs/.*\\.xml$', reporter: 'Spotbugs'], 
                [parser: 'CHECKSTYLE', pattern: '.*/checkstyle/.*\\.xml$', reporter: 'Checkstyle'], 
                [parser: 'PMD', pattern: '.*/pmd/.*\\.xml$', reporter: 'PMD']
            ]
        ])
    }

    commentMr(MR_PROJECT_ID, MR_IID, "Everything OK =) ${env.BUILD_URL}")
}

def buildRelease(Map params) {
    if (ref == null || ref?.trim().isEmpty()) {
        currentBuild.displayName = 'No ref given'
        return
    }
    currentBuild.displayName = 'Release ' + params.repo.name + " " + ref
    def notTriggeredByTag = env.object_kind == null
    if (notTriggeredByTag) {
        stage("Tagging ${params.repo.default_branch} with ${ref}") {
            sh """
             git clone ${params.repo.cloneUrl} .
             git checkout ${params.repo.default_branch}
             git tag ${ref}
             git push --tags
            """
        }
        // The tagging will trigger the same job again.
        return
    }
    stage("Checkout") {
        sh """
        git clone ${params.repo.cloneUrl} .
        git checkout ${after}
        """
        // Perhaps set the version in the build tool to value of $ref ?
        // In Maven that would be mvn versions:set -DnewVersion=${ref}
    }

    stage("Changelog") {
        def changelogString = gitChangelog returnType: 'STRING',
        to: [type: 'COMMIT', value: after],
        template: getChangelogTemplateString(params)
        currentBuild.description = changelogString
    }

    stage("Compile") {
        sh "sleep 5"
    }
    stage("Deploy") {
        sh "sleep 5"
    }
}

def buildSnapshot(Map params) {
    def ref = params.snapshotBranch
    if (env.commit != null) {
        ref = commit.substring(0,5)
    }
    currentBuild.displayName = 'Snapshot ' + params.repo.name + ' ' + ref

    stage("Checkout") {
        sh """
        git clone ${params.repo.cloneUrl} .
        git checkout ${ref}
        """
    }

    stage("Changelog") {
        def changelogString = gitChangelog returnType: 'STRING',
        to: [type: 'REF', value: params.snapshotBranch],
        template: getChangelogTemplateString(params)
        currentBuild.description = changelogString
    }

    stage("Compile") {
        sh "sleep 5"
    }
    if (!params.skipTests) {
        stage("Test") {
            sh "sleep 5"
        }
    }
    stage("Deploy") {
        sh "sleep 5"
    }
}

def call(repoJson) {
    def params = getParams(new JsonSlurperClassic().parseText(repoJson))

    node {
        try {
            if (env.JOB_NAME.endsWith('/merge-request')) {
                buildMergeRequest(params)
            } else if (env.JOB_NAME.endsWith('/snapshot')) {
                buildSnapshot(params)
            } else if (env.JOB_NAME.endsWith('/release')) {
                buildRelease(params)
            } else {
                throw new RuntimeException("Job not mapped "+env.JOB_NAME)
            }
        } finally {
            stage("Cleanup") {
                deleteDir()
            }
        }
    }
}
