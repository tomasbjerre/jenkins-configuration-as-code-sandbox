package se.bjurr.jenkinssandbox

public class RestClient {
  private def baseUrl
  private def steps
  
  public RestClient(def steps, def baseUrl='http://localhost') {
    this.steps = steps
    this.baseUrl = baseUrl
  }

  def get(def path) {
    def responseText = ""
    steps.withCredentials([steps.string(credentialsId: 'gitlab-token', variable: 'gitlabToken')]) {
      steps.println "Using token: "+steps.env.gitlabToken +" to get \"" + baseUrl + path + "\""
      def conn = null
      try {
        conn = new URL(baseUrl+path).openConnection();
        conn.setRequestMethod("GET")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Private-Token", steps.env.gitlabToken)
        conn.setDoOutput(false)
        def postRC = conn.getResponseCode();
        responseText = conn.getInputStream().getText()
        steps.println "Got: " + postRC + "\n"+responseText
      } finally {
        conn.disconnect()
      }
      //Because classic is serilizable
      return new groovy.json.JsonSlurperClassic().parseText(responseText)
    }
  }

  def delete(def path) {
    def responseText = ""
    steps.withCredentials([steps.string(credentialsId: 'gitlab-token', variable: 'gitlabToken')]) {
      steps.println "Using token: "+steps.env.gitlabToken +" to delete \"" + baseUrl + path + "\""
      def conn = null
      try {
        conn = new URL(baseUrl+path).openConnection();
        conn.setRequestMethod("DELETE")
        conn.setRequestProperty("Private-Token", steps.env.gitlabToken)
        conn.setDoOutput(false)
        def postRC = conn.getResponseCode();
        responseText = conn.getInputStream().getText()
        steps.println "Got: " + postRC + "\n"+responseText
      } finally {
        conn.disconnect()
      }
    }
  }

  def post(def path) {
    def responseText = ""
    steps.withCredentials([steps.string(credentialsId: 'gitlab-token', variable: 'gitlabToken')]) {
      steps.println "Using token: "+steps.env.gitlabToken +" to post \"" + baseUrl + path + "\""
      def conn = null
      try {
        conn = new URL(baseUrl+path).openConnection();
        conn.setRequestMethod("POST")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Private-Token", steps.env.gitlabToken)
        conn.setDoOutput(false)
        def postRC = conn.getResponseCode();
        responseText = conn.getInputStream().getText()
        steps.println "Got: " + postRC + "\n"+responseText
      } finally {
        conn.disconnect()
      }
      //Because classic is serilizable
      return new groovy.json.JsonSlurperClassic().parseText(responseText)
    }
  }

  def post(def path, def payload) {
    def responseText = ""
    steps.withCredentials([steps.string(credentialsId: 'gitlab-token', variable: 'gitlabToken')]) {
      String jsonString = new groovy.json.JsonBuilder(payload).toPrettyString()
      steps.println "Using token: "+steps.env.gitlabToken +" to post \"" + baseUrl + path + "\" with:\n"+jsonString
      def conn = null
      try {
        conn = new URL(baseUrl+path).openConnection();
        conn.setRequestMethod("POST")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Private-Token", steps.env.gitlabToken)
        conn.setDoOutput(true)
        conn.getOutputStream().write(jsonString.getBytes("UTF-8"));
        def postRC = conn.getResponseCode();
        responseText = conn.getInputStream().getText()
        steps.println "Got: " + postRC + "\n"+responseText
      } finally {
        conn.disconnect()
      }
      //Because classic is serilizable
      return new groovy.json.JsonSlurperClassic().parseText(responseText)
    }
  }
}
