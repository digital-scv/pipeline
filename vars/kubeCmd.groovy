import retort.utils.logging.Logger
import static retort.utils.Utils.delegateParameters as getParam

/**
 * kubectl apply
 *
 * @param file resource file. YAML or json
 * @param namespace namespace
 * @param record Record current kubectl command in the resource annotation.
 * @param option apply option
 * @param wait Wait n seconds while this resource rolled out
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
  
  if (config.record) {
    logger.debug("RECORD : ${config.record}")
    command.append(" --record=true")
  }
  
  if (config.option) {
    logger.debug("OPTION : ${config.option}")
    command.append(" ${config.option}")
  }
  
  if (config.namespace) {
    logger.debug("NAMESPACE : ${config.namespace}")
    command.append(" -n ${config.namespace}")
  }

  executeApply(command, config, logger)
}

/**
 * kubectl describe
 *
 * @param type resource type. ex) deploy, service etc.
 * @param name resource name or name prefix
 * @param label label selector array
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

/**
 * kubectl get with json path
 *
 * @param file
 * @param type
 * @param name
 * @param jsonpath 
 * @param namespace
 * @param throwException : false throw Exception 
 * @return String value
 */
def getValue(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret)
  
  def value = ''
  def command = new StringBuffer('kubectl get')
  
  if (config.type && config.name) {
    logger.debug("RESOURCE TYPE : ${config.type}")
    command.append(" ${config.type}")
    
    // kubectl get type name
    logger.debug("RESOURCE NAME : ${config.name}")
    command.append(" ${config.name}")
  } else if (config.file) {
    logger.debug("RESOURCE FILE : ${config.file}")
    command.append(" -f ${config.file}")
  } else {
    logger.error('type and name values are required. or specify file value.')
    if (config.throwException == true) {
      throw new IllegalArgumentException('type and name values are required. or specify file value.')
    }
    return value
  }
  
  if (config.namespace) {
    logger.debug("NAMESPACE : ${config.namespace}")
    command.append(" -n ${config.namespace}")
  }
  
  if (config.jsonpath) {
    logger.debug("JSONPATH : ${config.jsonpath}")
    command.append(" -o jsonpath=${config.jsonpath}")
  } else {
    logger.error('jsonpath value is required.')
    if (config.throwException == true) {
      throw new IllegalArgumentException('jsonpath value is required.')
    }
    return value
  }
  
  try {
    value = sh script: command.toString(), returnStdout: true
  } catch (Exception e) {
    if (config.throwException == true) {
      throw e
    }
  }
  
  return value
}


/**
 * excute apply command.
 */
private def executeApply(command, config, logger) {
  
  if (config.wait instanceof Integer) {
    
    sh command.toString()
    def resource = getValue file: config.file, namespace: config.namespace, jsonpath: '{.kind}/{.metadata.name}'
    
    logger.debug("Waiting for ${config.wait} seconds, during ${resource} being applied.")
    
    try {
      timeout (time: config.wait, unit: 'SECONDS') {
        sh "kubectl rollout status ${resource} -n ${config.namespace}"
      }

    } catch (Exception e) {
      logger.debug("Timeout occured, during ${resource} being applied.")
      throw new IllegalStateException("Timeout occured, during ${resource} being applied.")
    }

  } else {
    sh command.toString()
  }

}

