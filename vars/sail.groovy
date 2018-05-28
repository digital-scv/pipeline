import static groovy.io.FileType.FILES

def call(){
  log """\
  |SYSTEM :: run @shared-lib/vars/sail.groovy
  |SYSTEM :: ${new File('.').absolutePath}
  |SYSTEM :: ${GroovySystem.version}"""
  
  // Errors
  //def _default = load("${WORKSPACE}/Jenkinsfile.default")
  //def _default = libraryResource 'Jenkinsfile.default'
  //println _default
  
  node {
    println "SYSTEM :: ${pwd()}"
    
    //Load a pipline config file(.yaml)
    loadConfig()
    
    //Load prepared Jenkinsfiles (hard coded path)
    //- https://jenkins.io/doc/pipeline/examples/#load-from-file
    //load("${pwd()}@libs/sail-lib/Jenkinsfile.default")
    
    //Load prepared Jenkinsfiles (load and run string)
    //- https://github.com/jenkinsci/workflow-cps-global-lib-plugin/blob/master/src/main/java/org/jenkinsci/plugins/workflow/libs/LibraryAdder.java#L194
    //- https://stackoverflow.com/a/43327284
    def type = 'default'  //TODO: load standard pipline type from .yaml
    def pipeline = libraryResource "Jenkinsfile.${type}"
    evaluate pipeline
  }
}
