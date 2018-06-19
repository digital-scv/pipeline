import retort.config.Profile
import retort.utils.logging.Logger

def call(String... names){
  timestamps{
    Logger log = Logger.getLogger(this)

    //TODO: print as debug
    log.info """
    |[Environment]
    |++ Workspace      :: $env.WORKSPACE
    |++ Groovy version :: $GroovySystem.version""".stripMargin()

    //Load a pipline config file(.yaml)
    def profile = new Profile(this)
    def config = scmResource profile.candidate(names, 'pipeline.yaml', 'pipeline.yml')
    def conf = yaml.load(config)
    env._config = config
          
    /*
     * Load env variables in yaml.
     * Evaluation of GString with Custom Context.
     * - http://mrhaki.blogspot.com/2009/11/groovy-goodness-simple-evaluation-of.html
     * - http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.groovy/groovy-all/2.4.4/groovy/util/Eval.java#98
     */
    if(conf.env in Map){
      Binding b = new Binding(conf.env)
      GroovyShell sh = new GroovyShell(b)

      /*
       * Serialize Collection as String
       * - http://groovy.329449.n5.nabble.com/How-to-convert-String-1-2-3-to-List-tp4500137p4500155.html
       * - http://mrhaki.blogspot.com/2015/09/groovy-goodness-inspect-method-returns.html
       */ 
      conf.env.each {
        def val = it.value
        if(it.value in Map || it.value in List)
          val = val.inspect()

        env[it.key] = sh.evaluate("\"$val\"")
      }
    }

    //TODO: print as debug
    log.info "\n[Variables in 'env']\n" + env.getEnvironment().collect({e -> "++ ${e.key}=${e.value}"}).sort().join("\n") 

    //TODO: Throw error about invalid yaml
    //TODO: yaml to dynamic pipeline
    def file = 'Jenkinsfile.' + (conf['type'] ?: 'default')
    def pipeline = libraryResource file
    log.info "'${file}' is loaded."

    onKube.run(conf, pipeline)
  }
}
