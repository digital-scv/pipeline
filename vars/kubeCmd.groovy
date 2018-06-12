import retort.utils.logging.Logger
import retort.exception.RetortException
import static retort.utils.Utils.delegateParameters as getParam

/**
 * kubectl apply
 *
 * @param file required. resource file. YAML or json
 * @param namespace namespace
 * @param recoverOnFail Delete resource when fail applying.
 * @param option apply option
 * @param wait : 300 . Wait n seconds while this resource rolled out
 */
def apply(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret, [wait: 300, recoverOnFail: false])
  
  def command = new StringBuffer('kubectl apply')
  if (config.file) {
    logger.debug("FILE : ${config.file}")
    command.append(" -f ${config.file}")
  } else {
    throw createException('RC301')   
  }
  
  if (config.option) {
    logger.debug("OPTION : ${config.option}")
    command.append(" ${config.option}")
  }
  
  if (config.namespace) {
    logger.debug("NAMESPACE : ${config.namespace}")
    command.append(" -n ${config.namespace}")
  }
  
  command.append(" --record=true")

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
  def config = getParam(ret, [throwException: false])
  
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
        logger.error('describe : type value should be used with name or label.')
        throw createException('RC302')
      }
    }
  } else if (config.file) {
    logger.debug("RESOURCE FILE : ${config.file}")
    command.append(" -f ${config.file}")
  } else {
    logger.error('describe : type and name values are required. or specify file value.')
    if (config.throwException == true) {
      throw createException('RC303')
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
    logger.error('Exception occured while running describe command : ${command.toString()}')
    if (config.throwException == true) {
      throw createException('RC306', e, command.toString())
    }
  }

}

/**
 * Check resource exists.
 *
 * @param type resource type. ex) deploy, service etc.
 * @param name resource name or name prefix
 * @param label label selector array
 * @param file resource yaml file
 * @param namespace namespace
 * @param throwException : false throw Exception
 * @return boolean resource exists or not. if error occured and throwException : false return false.
 */
def resourceExists(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret, [throwException: false])
  
  def command = new StringBuffer('kubectl get')
  def result = false
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
        logger.error('resourceExists : type value should be used with name or label.')
        throw createException('RC302')
      }
      return result
    }
  } else if (config.file) {
    logger.debug("RESOURCE FILE : ${config.file}")
    command.append(" -f ${config.file}")
  } else {
    logger.error('resourceExists : type and name values are required. or specify file value.')
    if (config.throwException == true) {
      throw createException('RC303')
    }
    return result
  }
  
  if (config.namespace) {
    logger.debug("NAMESPACE : ${config.namespace}")
    command.append(" -n ${config.namespace}")
  }
  
  try {
    def status = sh script: "${command.toString()}", returnStatus: true
    result = status == 0 ? true : false
  } catch (Exception e) {
    if (config.throwException == true) {
      logger.error('Exception occured while checking resource is exist : ${command.toString()}')
      throw createException('RC309', e, command.toString())
    }
  }
  
  return result
}

/**
 * kubectl get with json path
 *
 * @param type
 * @param name
 * @param file
 * @param jsonpath 
 * @param namespace
 * @param throwException : false throw Exception 
 * @return String value
 */
def getValue(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret, [throwException: false])
  
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
    logger.error('getValue : type and name values are required. or specify file value.')
    if (config.throwException == true) {
      throw createException('RC303')
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
      throw createException('RC304')
    }
    return value
  }
  
  try {
    value = sh script: command.toString(), returnStdout: true
  } catch (Exception e) {
    logger.error("Exception occured while getting jsonpath : ${config.jsonpath}")
    if (config.throwException == true) {
      throw createException('RC305', e, config.jsonpath)
    }
  }
  
  return value
}

/**
 * kubectl rollout status
 *
 * @param type
 * @param name
 * @param file
 * @param namespace
 * @param wait : 300
 * @param throwException : false throw Exception 
 */
def rolloutStatus(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret, [wait: 300])
  
  def value = ''
  def command = new StringBuffer('kubectl rollout status')
  
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
    logger.error('rolloutStatus : type and name values are required. or specify file value.')
    if (config.throwException == true) {
      throw createException('RC303')
    }
  }
  
  if (config.namespace) {
    logger.debug("NAMESPACE : ${config.namespace}")
    command.append(" -n ${config.namespace}")
  }
  
  def config2 = config.clone()
  config2.put('jsonpath', '{.kind}/{.metadata.name}')
  def resource
  try {
    resource = getValue config2
  } catch (Exception e2) {
    if (config.type && config.name) {
      resource = "${config.type}/${config.name}"
    } else {
      resource = config.file
    }
  }
  
  try {
    logger.debug("Waiting for ${config.wait} seconds, during ${resource} being applied.")
    timeout (time: config.wait, unit: 'SECONDS') {
      try {
        sh command.toString()
      } catch (Exception e) {
        logger.error("Exception occured while running rollout status : ${config.jsonpath}")
        if (config.throwException == true) {
          throw createException('RC307', e, config.jsonpath)
        }
      }
    }
  } catch (Exception e) {
    if (e instanceof RetortException && e.getErrorCode == 'RC307') {
      throw e
    }

    logger.debug("Timeout occured while ${resource} being applied. Check events.")
    
    config2.put('throwException', false)
    describe config2
    
    throw createException('RC308', e, resource)
  }
  
}



/**
 * excute apply command.
 */
private def executeApply(command, config, logger) {
  
  def exists = false
  try {
    exists = resourceExists config
    
    // no need to recover
    sh command.toString()

    // recover
    if (config.wait instanceof Integer && config.wait > 0) {
      rolloutStatus config
    }
    
  } catch (Exception e) {
    logger.error()
    if (config.recoverOnFail && e instanceof RetortException) {
      recoverApply(exists, config, logger)
    }
    throw createException('RC310', e, config.file)
  }

}

/**
 * 
 */
private def recoverApply(exists, config, logger) {
  if (exists) {
     // rollback
  } else {
    // delete        
  }


}


