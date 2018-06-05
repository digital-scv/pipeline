import retort.utils.logging.Logger
import static retort.utils.Utils.delegateParameters as getParam

/**
 * kubectl apply
 *
 * @param file
 */
def apply(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret)
  
  def command = new StringBuffer('kubectl apply')
  if (config.file) {
    logger.debug("FILE : ${config.file}")
    command.append(" -f ${config.file}")
  } else {
    throw new IllegalArgumentException('file is required')      
  }
  
  if (config.option) {
    logger.debug("OPTION : ${config.option}")
    command.append(" ${config.option}")
  }

  

  logger.debug('apply executed')
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
  
  if (config.type) {
    logger.debug("RESOURCE TYPE : ${config.type}")
    command.append(" ${config.type}")
    
    if (config.name) {
      // kubectl describe type name
      logger.debug("RESOURCE NAME : ${config.name}")
      command.append(" ${config.name}")
    } else if ((config.label instanceof List) || config.label.getClass().isArray()) {
      // kubectl describe type -l [key=value]+
      command.append(" -l ")
      command.append config.label.collect{ l ->
        logger.debug("LABEL-SELECTOR : ${l}")
        return "${l}"
      }.join(',')
    } else {
      if (config.throwException == true) {
        logger.error('type value should be used with name or label.')
        throw new IllegalArgumentException('type value should be used with name or label.')
      }
    }
  } else if (config.file) {
    logger.debug("RESOURCE FILE : ${config.file}")
    command.append(" -f ${config.file}")
  } else {
    logger.error('type and name values are required. or specify file value.')
    if (config.throwException == true) {
      throw new IllegalArgumentException('type and name values are required. or specify file value.')
    }
    return
  }
  
  if (config.namespace) {
    logger.debug("NAMESPACE : ${config.namespace}")
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


