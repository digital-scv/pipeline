@Grab('org.yaml:snakeyaml:1.17')
import org.yaml.snakeyaml.Yaml as Parser

import retort.utils.logging.Logger
import static retort.utils.Utils.delegateParameters as getParam

//Expose static function like 'yaml.load()'
//- https://stackoverflow.com/a/25603288
static def load(String yaml){
  //Parse yaml file to collection
  //- https://stackoverflow.com/a/41731617
  new Parser().load(yaml)
}

static def dump(def yaml){
  new Parser().dump(yaml)
}

/**
 * file
 * update map key: yaml path, value: value
 */
def update(def ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret)
  
  if (!config.file) {
    logger.error('file is required.')
    createException('RC401')
  }
  
  logger.debug("FILE : ${config.file}")
 
  if (!config.update) {
    logger.error('file is required.')
    createException('RC402')
  } else if (!(config.update in Map)){
    logger.error('update only support Map type parameter.')
    createException('RC403')
  }
  
  logger.debug("UPDATE : ${config.update}")

  def yamlText = readFile file:'k8s/deploy.yaml'
  logger.debug("""
Original yaml contents
${yamlText}
""")

  def yaml = load(yamlText)
  
  config.update.each { k, v ->
      def binding = new Binding(yaml:yaml)
      def shell = new GroovyShell(binding)
      def expression = "yaml${k} = '${v}'"
      logger.debug 
      
      shell.evaluate(expression)
  }
  
  def updatedYamlText = dump(yaml)
  
  logger.debug("""
Updated yaml contents
${updatedYamlText}
""")

  writeFile file: config.file, text: updatedYamlText
}
