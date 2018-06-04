def call(){
  //TODO: print as debug
  echo """\
  |SYSTEM :: run @shared-lib/vars/sail.groovy
  |SYSTEM :: ${new File('.').absolutePath}
  |SYSTEM :: ${GroovySystem.version}""".stripMargin()
  
  //TODO: print as debug
  echo env.getEnvironment().collect({e -> "${e.key}=${e.value}"}).join("\n")
    
  //Load a pipline config file(.yaml)
  loadConfig()
        
  //Load prepared Jenkinsfiles (load and run string)
  //- https://github.com/jenkinsci/workflow-cps-global-lib-plugin/blob/master/src/main/java/org/jenkinsci/plugins/workflow/libs/LibraryAdder.java#L194
  //- https://stackoverflow.com/a/43327284

  //TODO: Throw error about invalid yaml
  def conf = yaml.load(env._config)
  def file = 'Jenkinsfile.' + (conf['type'] ?: 'default')
  def pipeline = libraryResource file

  //TODO: yaml to dynamic pipeline

  echo "'${file}' is loaded."

  kubeSlave(conf, pipeline)
}
