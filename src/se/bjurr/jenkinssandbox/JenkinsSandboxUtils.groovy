package se.bjurr.jenkinssandbox

public class JenkinsSandboxUtils {
  /**
  * Get ip of host machine.
  */
  public static String getHostIp(steps) {
    steps.sh(
      returnStdout: true, 
      script: '''ip route|awk '/default/ { print $3 }' '''
    )
    .trim()
  }
}