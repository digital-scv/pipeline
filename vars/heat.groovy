
def call(String... files){
  //TODO: print as debug
  echo """\
  |SYSTEM :: run @shared-lib/vars/sail.groovy
  |SYSTEM :: ${new File('.').absolutePath}
  |SYSTEM :: ${GroovySystem.version}""".stripMargin()
  
  //Load a pipline config file(.yaml)
  env._config = scmResource files ?: ['pipeline.yaml', 'pipeline.yml'] as String[]
        
  //Load prepared Jenkinsfiles (load and run string)
  //- https://github.com/jenkinsci/workflow-cps-global-lib-plugin/blob/master/src/main/java/org/jenkinsci/plugins/workflow/libs/LibraryAdder.java#L194
  //- https://stackoverflow.com/a/43327284

  //TODO: Throw error about invalid yaml
  def conf = yaml.load(env._config)
  def file = 'Jenkinsfile.' + (conf['type'] ?: 'default')
  def pipeline = libraryResource file

  //TODO: yaml to dynamic pipeline

  echo "'${file}' is loaded."

  /*
   * Load env variables in yaml.
   * Evaluation of GString with Custom Context.
   * - http://mrhaki.blogspot.com/2009/11/groovy-goodness-simple-evaluation-of.html
   * - http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.groovy/groovy-all/2.4.4/groovy/util/Eval.java#98
   */
  if(conf.env in Map){
    Binding b = new Binding(conf.env)
    GroovyShell sh = new GroovyShell(b)

    conf.env.each {
      env[it.key] = sh.evaluate("\"$it.value\"")
    }
  }

  //TODO: print as debug
  echo env.getEnvironment().collect({e -> "${e.key}=${e.value}"}).join("\n") 

  onKube.run(conf, pipeline)
}
