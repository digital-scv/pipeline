import retort.utils.logging.Logger
import static retort.utils.Utils.delegateParameters as getParam

/**
 * kubectl apply
 *
 * @param
 */
def apply(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret)
}

/**
 * kubectl describe
 *
 * @param type resource type. ex) deploy, service etc.
 * @param name resource name or name prefix
 * @param file resource yaml file
 * @param namespace namespace
 * @param throwException : false throw Exception 
 */
def describe(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret)
  
  def command = new StringBuffer('kubectl describe')
  
  if (config.type && config.name) {
    command.append(" ${config.type}")
    command.append(" ${config.name}")
  } else if (config.file) {
    command.append(" -f ${config.file}")
  } else {
    logger.debug('type and name values are required. or specify file value.')
    if (config.throwException == true) {
      throw new IllegalArgumentException('type and name values are required. or specify file value.');
    }
    return
  }
  
  if (config.namespace) {
    command.append(" -n ${config.namespace}")
  }
  
  try {
    sh command.toString()
  } catch (Exception e) {
    if (config.throwException == true) {
      throw e
    }
  }

}
