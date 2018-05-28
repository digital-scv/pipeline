def withGit(String repoUrl, String repoBranch, 
        String credentialsId = null, labelExpression = '', Closure body){
  Map<String, Object> loaded = new TreeMap<String, Object>()
    node(labelExpression) {
      //withTimestamper {
        dir('libLoader') {
          // Flush the directory
          deleteDir()

          // Checkout
          echo "Checking out ${repoUrl}, branch=${repoBranch}"
          checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: repoBranch]], 
                          userRemoteConfigs: [[credentialsId: credentialsId, url: repoUrl]]]
          
          // Invoke body in the folder
          body();

          // Flush the directory again
          deleteDir()
        }
      //}
    }
}

/*
 * - Ref. Shared Library Type := https://jenkins.io/doc/book/pipeline/shared-libraries/#defining-global-variables
 * - Original Source := https://github.com/jenkinsci/workflow-remote-loader-plugin/blob/master/src/main/resources/org/jenkinsci/plugins/workflow/remoteloader/FileLoaderDSL/FileLoaderDSLImpl.groovy#L51
 */
