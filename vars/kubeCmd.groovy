import retort.utils.logging.Logger
import retort.exception.RetortException
import static retort.utils.Utils.delegateParameters as getParam

/**
 * kubectl apply
 *
 * @param file resource file. YAML or json
 * @param folder resource folder.
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
  } else if (config.folder) {
    logger.debug("FOLDER : ${config.folder}")
    command.append(" -f ${config.folder}")
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

  if (config.file) {
    executeApplyFile(command, config, logger)
  } else if (config.folder) {
    executeApplyFolder(command, config, logger)
  }

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
  
  try {
    if (config.type) {
      logger.debug("RESOURCE TYPE : ${config.type}")
      command.append(" ${config.type}")
      
      if (config.name) {
        // kubectl describe type name
        logger.debug("RESOURCE NAME : ${config.name}")
        command.append(" ${config.name}")
      } else if (config.label) {
        if ((config.label instanceof List) || config.label.getClass().isArray()) {
          // kubectl describe type [-l key=value ]+
          command.append config.label.collect{ l ->
              logger.debug("LABEL-SELECTOR : ${l}")
              return " -l ${l}"
            }.join()
        } else if (config.label instanceof Map) {
          command.append config.label.collect { k, v ->
              logger.debug("LABEL-SELECTOR : ${k}=${v}")
              return " -l ${k}=${v}"
            }.join()
        } else {
          logger.error('describe : label only support Map, List, Array type parameter.')
          throw createException('RC311')
        }
      } else {
        logger.error('describe : type value should be used with name or label.')
        throw createException('RC302')
      }
    } else if (config.file) {
      logger.debug("RESOURCE FILE : ${config.file}")
      command.append(" -f ${config.file}")
    } else {
      logger.error('describe : type and name values are required. or specify file value.')
      throw createException('RC303')
    }
    
    if (config.namespace) {
      logger.debug("NAMESPACE : ${config.namespace}")
      command.append(" -n ${config.namespace}")
    }
    
    try {
      sh command.toString()
    } catch (Exception e) {
      logger.error('Exception occured while running describe command : ${command.toString()}')
      throw createException('RC306', e, 'describe', command.toString())
    }
  } catch (Exception e) {
    if (config.throwException == true) {
      throw e
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
  
  try {
    if (config.type) {
      logger.debug("RESOURCE TYPE : ${config.type}")
      command.append(" ${config.type}")
      
      if (config.name) {
        // kubectl describe type name
        logger.debug("RESOURCE NAME : ${config.name}")
        command.append(" ${config.name}")
      } else if (config.label) {
        if ((config.label instanceof List) || config.label.getClass().isArray()) {
          // kubectl describe type [-l key=value ]+
          command.append config.label.collect{ l ->
              logger.debug("LABEL-SELECTOR : ${l}")
              return " -l ${l}"
            }.join()
        } else if (config.label instanceof Map) {
          command.append config.label.collect { k, v ->
              logger.debug("LABEL-SELECTOR : ${k}=${v}")
              return " -l ${k}=${v}"
            }.join()
        } else {
          logger.error('resourceExists : label only support Map, List, Array type parameter.')
          throw createException('RC311')
        }
      } else {
        logger.error('resourceExists : type value should be used with name or label.')
        throw createException('RC302')
      }
    } else if (config.file) {
      logger.debug("RESOURCE FILE : ${config.file}")
      command.append(" -f ${config.file}")
    } else {
      logger.error('resourceExists : type and name values are required. or specify file value.')
      throw createException('RC303')
    }
    
    if (config.namespace) {
      logger.debug("NAMESPACE : ${config.namespace}")
      command.append(" -n ${config.namespace}")
    }
    
    try {
      def status = sh script: "${command.toString()}", returnStatus: true
      result = status == 0 ? true : false
    } catch (Exception e) {
      logger.error('Exception occured while checking resource is exist : ${command.toString()}')
      throw createException('RC309', e, command.toString())
    }
  } catch (Exception e) {
    if (config.throwException == true) {
      throw e
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
  
  try {
    if (config.type && config.name) {
      logger.debug("RESOURCE TYPE : ${config.type}")
      command.append(" ${config.type}")
      
      logger.debug("RESOURCE NAME : ${config.name}")
      command.append(" ${config.name}")
    } else if (config.file) {
      logger.debug("RESOURCE FILE : ${config.file}")
      command.append(" -f ${config.file}")
    } else {
      logger.error('getValue : type and name values are required. or specify file value.')
      throw createException('RC303')
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
      throw createException('RC304')
    }
    
    try {
      value = sh script: command.toString(), returnStdout: true
    } catch (Exception e) {
      logger.error("Exception occured while getting jsonpath : ${config.jsonpath}")
      throw createException('RC305', e, config.jsonpath)
    }
  } catch (Exception e) {
    if (config.throwException == true) {
      throw e
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
  
  try {
      
    if (!(config.wait instanceof Integer)) {
      logger.error("wait value must be Integer but received ${config.wait.getClass().toString()}")
      throw createException('RC313', config.wait.getClass().toString())
    }
    
    def command = new StringBuffer('kubectl rollout status')
    
    if (config.type && config.name) {
      logger.debug("RESOURCE TYPE : ${config.type}")
      command.append(" ${config.type}")
      
      logger.debug("RESOURCE NAME : ${config.name}")
      command.append(" ${config.name}")
    } else if (config.file) {
      logger.debug("RESOURCE FILE : ${config.file}")
      command.append(" -f ${config.file}")
    } else {
      logger.error('rolloutStatus : type and name values are required. or specify file value.')
      throw createException('RC303')
    }
    
    if (config.namespace) {
      logger.debug("NAMESPACE : ${config.namespace}")
      command.append(" -n ${config.namespace}")
    }
    
    def config2 = config.clone()
    def resourceKind
    def resourceName
    try {
      config2.put('jsonpath', '{.kind}/{.metadata.name}')
      def resource = getValue config2
      
      resourceKind = resource.tokenize('/')[0]
      resourceName = resource.tokenize('/')[1]
    } catch (Exception e2) {
      logger.error("Resource does not exists. Can not execute rollout status.")
      throw createException('RC316', "rollout status")
    }
    
    def rolloutPossibleResources = ['deployment', 'deploy', 'daemonset', 'ds', 'statefullset', 'sts']
    if (!rolloutPossibleResources.contains(resourceKind.toLowerCase())) {
      logger.error("roleoutStatus : Rollout resource type must be ${rolloutPossibleResources}. But received ${resourceKind}")
      throw createException('RC317', rolloutPossibleResources, resourceKind)
    }

    
    try {
      logger.debug("Waiting for ${config.wait} seconds, during ${resourceKind}/${resourceName} being applied.")
      timeout (time: config.wait, unit: 'SECONDS') {
        try {
          sh command.toString()
        } catch (Exception e) {
          // https://wiki.jenkins.io/display/JENKINS/Job+Exit+Status
          // You sent it a signal with the UNIX kill command, or SGE command qdel. 
          // If you don't specify which signal to send, kill defaults to SIGTERM (exit code 15+128=143) 
          // and qdel sends SIGINT (exit code 2+128=130), then SIGTERM, then SIGKILL until your job dies.
          // TIMEOUT 
          if (e.getMessage().contains('143')) {
            logger.error('Timeout occured')
            throw e
          } else {
            logger.error("Exception occured while running rollout status : ${resourceKind}/${resourceName}")
            throw createException('RC306', e, 'rollout status', command.toString())
          }
  
        }
      }
    } catch (Exception e) {
      // sh fail
      if (e instanceof RetortException && e.getErrorCode() == 'RC306') {
        throw e
      }
  
      // timeout
      logger.error("Timeout occured while ${resourceKind}/${resourceName} being applied. Check events.")
      
      config2 = config.clone()
      config2.put('type', 'pod')
      config2.put('name', resourceName)
      config2.put('throwException', false)
      
      describe config2
      throw createException('RC308', e, "${resourceKind}/${resourceName}")
    }
  } catch (Exception e) {
    if (config.throwException == true) {
      throw e
    }

  }

}


/**
 * kubectl rollout undo
 *
 * @param type
 * @param name
 * @param file
 * @param revision
 * @param namespace
 * @param wait : 300
 */
def rolloutUndo(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret, [wait: 300])
  
  if (!(config.wait instanceof Integer)) {
    logger.error("wait value must be Integer but received ${config.wait.getClass().toString()}")
    throw createException('RC313', config.wait.getClass().toString())
  }
  
  def command = new StringBuffer('kubectl rollout undo')
  
  if (config.type && config.name) {
    logger.debug("RESOURCE TYPE : ${config.type}")
    command.append(" ${config.type}")
    
    logger.debug("RESOURCE NAME : ${config.name}")
    command.append(" ${config.name}")
  } else if (config.file) {
    logger.debug("RESOURCE FILE : ${config.file}")
    command.append(" -f ${config.file}")
  } else {
    logger.error('rolloutUndo : type and name values are required. or specify file value.')
    throw createException('RC303')
  }
  
  if (config.namespace) {
    logger.debug("NAMESPACE : ${config.namespace}")
    command.append(" -n ${config.namespace}")
  }
  
  if (config.revision) {
    logger.debug("TO REVISION : ${config.revision}")
    command.append(" --to-revision=${config.revision}")
  }
  
  def config2 = config.clone()
  def resourceKind
  def resourceName
  try {
    config2.put('jsonpath', '{.kind}/{.metadata.name}')
    def resource = getValue config2
    
    resourceKind = resource.tokenize('/')[0]
    resourceName = resource.tokenize('/')[1]
  } catch (Exception e2) {
    logger.error("Resource does not exists. Can not execute rollout undo.")
    throw createException('RC316', "rollout undo")
  }
  
  def rolloutPossibleResources = ['deployment', 'deploy', 'daemonset', 'ds', 'statefullset', 'sts']
  if (!rolloutPossibleResources.contains(resourceKind.toLowerCase())) {
    logger.error("roleoutUndo : Rollout resource type must be ${rolloutPossibleResources}. But received ${resourceKind}")
    throw createException('RC317', rolloutPossibleResources, resourceKind)
  }
  
  try {
    sh command.toString()
  } catch (Exception e) {
    logger.error("Exception occured while running rollout undo command : ${command.toString()}")
    throw createException('RC306', e, 'rollout undo', command.toString())
  }
  
  if (config.wait > 0) {
    rolloutStatus config
  }
  
}

/**
 * kubectl delete
 *
 * @param type
 * @param name
 * @param file
 * @param namespace
 * @param force
 */
def delete(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret)
  
  def command = new StringBuffer('kubectl delete')
  
  if (config.type && config.name) {
    logger.debug("RESOURCE TYPE : ${config.type}")
    command.append(" ${config.type}")
    
    logger.debug("RESOURCE NAME : ${config.name}")
    command.append(" ${config.name}")
  } else if (config.file) {
    logger.debug("RESOURCE FILE : ${config.file}")
    command.append(" -f ${config.file}")
  } else {
    logger.error('delete : type and name values are required. or specify file value.')
    throw createException('RC303')
  }
  
  if (config.namespace) {
    logger.debug("NAMESPACE : ${config.namespace}")
    command.append(" -n ${config.namespace}")
  }
  
  if (config.force == true) {
    logger.debug("FORCE : ${config.force}")
    command.append(" -force ${config.force}")
  }
  
  def config2 = config.clone()
  def resourceKind
  def resourceName
  try {
    config2.put('jsonpath', '{.kind}/{.metadata.name}')
    def resource = getValue config2
    
    resourceKind = resource.tokenize('/')[0]
    resourceName = resource.tokenize('/')[1]
  } catch (Exception e2) {
    logger.error("Resource does not exists. Can not execute delete.")
    throw createException('RC316', 'delete')
  }
  
  try {
    sh command.toString()
  } catch (Exception e) {
    logger.error("Exception occured while running delete command : ${command.toString()}")
    throw createException('RC306', e, 'delete', command.toString())
  }
  
}

/**
 * kubectl scale
 *
 * @param type
 * @param name
 * @param file
 * @param namespace
 * @param replicas required.  num of replicas.
 * @param wait : 300 
 */
def scale(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret, [wait: 300])
  
  def command = new StringBuffer('kubectl scale')
  
  if (config.type && config.name) {
    logger.debug("RESOURCE TYPE : ${config.type}")
    command.append(" ${config.type}")
    
    logger.debug("RESOURCE NAME : ${config.name}")
    command.append(" ${config.name}")
  } else if (config.file) {
    logger.debug("RESOURCE FILE : ${config.file}")
    command.append(" -f ${config.file}")
  } else {
    logger.error('scale : type and name values are required. or specify file value.')
    throw createException('RC303')
  }
  
  if (config.namespace) {
    logger.debug("NAMESPACE : ${config.namespace}")
    command.append(" -n ${config.namespace}")
  }
  
  if (config.replicas) {
    logger.debug("REPLICAS : ${config.replicas}")
    command.append(" --replicas=${config.replicas}")
  } else {
    logger.error('replicas is required.')
    throw createException('RC318')
  }
  
  def config2 = config.clone()
  def resourceKind
  def resourceName
  try {
    config2.put('jsonpath', '{.kind}/{.metadata.name}')
    def resource = getValue config2
    
    resourceKind = resource.tokenize('/')[0]
    resourceName = resource.tokenize('/')[1]
  } catch (Exception e2) {
    logger.error("Resource does not exists. Can not execute scale.")
    throw createException('RC316', 'scale')
  }
  
  def scalePossibleResources = ['deployment', 'deploy', 'replicaset', 'rs', 'replicationcontrollers', 'rc', 'jobs']
  if (!scalePossibleResources.contains(resourceKind.toLowerCase())) {
    logger.error("scale : Scale resource type must be ${scalePossibleResources}. But received ${resourceKind}")
    throw createException('RC317', scalePossibleResources, resourceKind)
  }
  
  try {
    sh command.toString()
  } catch (Exception e) {
    logger.error("Exception occured while running scale command : ${command.toString()}")
    throw createException('RC306', e, 'scale', command.toString())
  }
  
  // rollout
  if (config.wait > 0) {
    def config3 = config.clone()
    config3.put('throwException', true)
    rolloutStatus config3
  }
  
}

/**
 * excute apply command with file.
 */
private def executeApplyFile(command, config, logger) {
  if (!(config.wait instanceof Integer)) {
    logger.error('wait value must be Integer but received ${config.wait.getClass().toString()}')
    throw createException('RC313', config.wait.getClass().toString())
  }

  def exists = false
  def recoverConfig = config.clone()
  try {
    exists = resourceExists config
    
    // no need to recover
    sh command.toString()

    // rollout
    if (config.wait > 0) {
      def config2 = config.clone()
      def resourceKind
      def resourceName
      try {
        config2.put('jsonpath', '{.kind}/{.metadata.name}')
        def resource = getValue config2
        
        resourceKind = resource.tokenize('/')[0]
        recoverConfig.type = resourceKind
        resourceName = resource.tokenize('/')[1]
        recoverConfig.name = resourceName
      } catch (Exception e2) {
      }
      
      def rolloutPossibleResources = ['deployment', 'deploy', 'daemonset', 'ds', 'statefullset', 'sts']
      if (rolloutPossibleResources.contains(resourceKind.toLowerCase())) {
        def config3 = config.clone()
        config3.put('throwException', true)
        rolloutStatus config3
      }
    }
    // print
    if (!exists) {
      resourceExists config
    }

    
  } catch (Exception e) {
    // Exception from rollout status
    // need to rollback
    if (e instanceof RetortException) {
      logger.error('Exception occured while waiting rollout.')
      if (config.recoverOnFail) {
        logger.debug('RECOVER_ON_FAIL : true')
        logger.debug('Trying to recover.')
        recoverApply(exists, recoverConfig, logger)
      }
      throw createException('RC312', e, config.file)
      
    // Exception from command execution
    // can't rollback. just throw exception.
    } else {
      logger.error('Exception occured while applying.')
      throw createException('RC310', e, config.file)
    }

  }

}

/**
 * 
 */
private def recoverApply(exists, config, logger) {
  if (exists) {
    // rollback
    logger.debug('Resource rollback.')
     
    def rolloutPossibleResources = ['deployment', 'deploy', 'daemonset', 'ds', 'statefullset', 'sts']
    if (rolloutPossibleResources.contains(config.type.toLowerCase())) {
      rolloutUndo config
    } else if (config.recoverFile) {
      def recoverConfig = config.clone()
      recoverConfig.file = config.recoverFile
      recoverConfig.recoverOnFail = false
      recoverConfig.wait = 0
      apply recoverConfig
    }

  } else {
    // delete        
    logger.debug('Resource delete.')
    delete config
  }
}

/**
 * excute apply command with folder.
 */
private def executeApplyFolder(command, config, logger) {
  try {
    // no need to recover
    sh command.toString()
  } catch (Exception e) {
    logger.error("Exception occured while applying : ${config.folder}")
    throw createException('RC310', e, config.folder)
  }
}
