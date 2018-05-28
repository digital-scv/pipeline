/* Will executed in node */
def call() {
  fileLoader.withGit scm.userRemoteConfigs[0].url, scm.branches[0].name, scm.userRemoteConfigs.credentialsId[0], '', {
    // Find list of file name
    // http://mrhaki.blogspot.kr/2009/11/groovy-goodness-finding-files-with.html
    def _files = new FileNameFinder().getFileNames("${pwd()}", 'pipeline.yaml pipeline.yml')
    
    _files.each { log "Load '${it}'" }
      
    env._config = _files[0] ? new File(_files[0]).text : null
  }
}
