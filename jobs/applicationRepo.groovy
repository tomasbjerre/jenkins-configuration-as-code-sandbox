import static groovy.json.JsonOutput.*

def createFolder(generatedFolder, repo) {
  def namespaceFolder = generatedFolder+'/'+repo.namespacePath
  folder(namespaceFolder) {
    displayName(repo.namespaceName)
    description('')
  }

  def repoFolder = namespaceFolder+'/'+repo.path
  folder(repoFolder) {
    displayName(repo.name)
    description(
      repo.description + "\n\n"
      + repo.url + "\n\n"
      + repo.cloneUrl
    )
  }
  return repoFolder
}


def createSnapshotJob(repoFolder, repo) {
  pipelineJob(repoFolder+'/snapshot') {
    description('Triggered when pusing to default branch in GitLab ('+repo.default_branch+').')
    quietPeriod(0)
    concurrentBuild(false)
    logRotator {
      numToKeep(10)
    }
    triggers {
      genericTrigger {
        genericVariables {
          genericVariable {
            key('user_name')
            value('$.user_name')
          }
          genericVariable {
            key('commit')
            value('$.after')
          }
          genericVariable {
            key('ref')
            value('$.ref')
          }
          genericVariable {
            key('object_kind')
            value('$.object_kind')
          }
        }
        token(repo.path)
        printContributedVariables(true)
        printPostContent(true)
        silentResponse(false)
        regexpFilterText('$object_kind $ref')
        regexpFilterExpression('^push refs/heads/' + repo.default_branch + '$')
        causeString('Triggered by $user_name who pushed $commit to $ref')
      }
    }
    definition {
      cps {
        script('buildRepo(\'\'\'\n'+ prettyPrint(toJson(repo)) + '\n\'\'\')')
        sandbox()
      }
    }
  }
}


def createReleaseJob(repoFolder, repo) {
  pipelineJob(repoFolder+'/release') {
    description('Triggered when pusing tag to repo in GitLab ('+repo.cloneUrl+'). '
              + 'Or when run manually.')
    quietPeriod(0)
    concurrentBuild(false)
    logRotator {
      numToKeep(10)
    }
    parameters {
      stringParam('ref', '', 'The version to release, same as tag name.')
    }
    triggers {
      genericTrigger {
        genericVariables {
          genericVariable {
            key('user_name')
            value('$.user_name')
          }
          genericVariable {
            key('after')
            value('$.after')
          }
          genericVariable {
            key('before')
            value('$.before')
          }
          genericVariable {
            key('ref')
            value('$.ref')
            regexpFilter('refs/tags/')
          }
          genericVariable {
            key('object_kind')
            value('$.object_kind')
          }
        }
        token(repo.path)
        printContributedVariables(true)
        printPostContent(true)
        silentResponse(false)
        regexpFilterText('$object_kind $before $after')
        regexpFilterExpression('^tag_push\\s0{40}\\s.{40}$')
        causeString('Triggered by $user_name who pushed $ref referencing $after')
      }
    }
    definition {
      cps {
        script('buildRepo(\'\'\'\n'+ prettyPrint(toJson(repo)) + '\n\'\'\')')
        sandbox()
      }
    }
  }
}


def createMergeRequestJob(repoFolder, repo) {
  pipelineJob(repoFolder+'/merge-request') {
    quietPeriod(0)
    concurrentBuild(true)
    logRotator {
      numToKeep(10)
    }
    triggers {
      genericTrigger {
        genericVariables {
          genericVariable {
            key('user_name')
            value('$.user.name')
          }
          genericVariable {
            key('MR_URL')
            value('$.object_attributes.url')
          }
          genericVariable {
            key('MR_FROM_URL')
            value('$.object_attributes.source.git_http_url')
          }
          genericVariable {
            key('MR_FROM_BRANCH')
            value('$.object_attributes.source_branch')
          }
          genericVariable {
            key('MR_TO_URL')
            value('$.object_attributes.target.git_http_url')
          }
          genericVariable {
            key('MR_TO_BRANCH')
            value('$.object_attributes.target_branch')
            regexpFilter('refs/tags/')
          }
          genericVariable {
            key('MR_PROJECT_ID')
            value('$.object_attributes.target_project_id')
          }
          genericVariable {
            key('MR_IID')
            value('$.object_attributes.iid')
          }
          genericVariable {
            key('MR_OLD_REV')
            value('$.object_attributes.oldrev')
          }
          genericVariable {
            key('MR_ACTION')
            value('$.object_attributes.action')
          }
          genericVariable {
            key('MR_TITLE')
            value('$.object_attributes.title')
          }
          genericVariable {
            key('MR_STATE')
            value('$.object_attributes.state')
          }
          genericVariable {
            key('MR_OBJECT_KIND')
            value('$.object_kind')
          }
          genericVariable {
            key('MR_TITLE')
            value('\\$.object_attributes.title')
          }
        }
        token(repo.path)
        printContributedVariables(true)
        printPostContent(true)
        silentResponse(false)
        regexpFilterText('$MR_OBJECT_KIND $MR_ACTION $MR_OLD_REV')
        regexpFilterExpression('^merge_request\\s(update\\s.{40}$|open.*)')
        causeString('Triggered by $user_name who pushed to $MR_FROM_BRANCH')
      }
    }
    definition {
      cps {
        script('buildRepo(\'\'\'\n'+ prettyPrint(toJson(repo)) + '\n\'\'\')')
        sandbox()
      }
    }
  }
}


def generatedFolder = 'generated'
folder(generatedFolder) {
  displayName('Generated Jobs')
  description('Jobs that are automatically generated.')
}

repos.each { repo ->
  println "Creating application repo for: " + repo.name

  def repoFolder = createFolder(generatedFolder, repo)
  createSnapshotJob(repoFolder,repo)
  createReleaseJob(repoFolder,repo)
  createMergeRequestJob(repoFolder,repo)
}
