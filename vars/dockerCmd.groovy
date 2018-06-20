import retort.utils.logging.Logger
import retort.exception.RetortException
import static retort.utils.Utils.delegateParameters as getParam

/**
 * docker push
 *
 * @param registry
 * @param imageName
 * @param imageVersion
 * @param credentialsId
 * @param username
 * @param password
 */
def push(ret) {
  Logger logger = Logger.getLogger(this)
  createException.init(logger)
  
  def config = getParam(ret)
  
  def command = new StringBuffer('docker push ')
  
  command.append(getFullRepository(config, logger))
  
  // login with docker credential or username/password
  // 1. credential
  // 2. username/password
  // 3. anonymous
  if (config.credentialsId) {
    pushWithCredentialsId(config, command, logger)
  } else if (config.username && config.password) {
    pushWithUsernameAndPassword(config, command, logger)                                    
  } else {
    sh command.toString()
  }

}


/**
 * docker build
 *
 * @param path
 * @param file
 * @param tag
 * @param buildArgs
 * @param options
 */
def build(ret) {
  Logger logger = Logger.getLogger(this)
  createException.init(logger)
  def config = getParam(ret) {
    // current workspace
    path = '.'
  }
  
  def command = new StringBuffer('docker build')
  
  appendCommand(config, 'file', '-f', command, logger)
  setTag(command, config, logger)
  setBuildArgs(command, config, logger)
  appendCommand(config, 'options', '', command, logger)
  appendCommand(config, 'path', '', command, logger)
  
  sh command.toString()
}

/**
 * docker tag
 *
 * @param source
 * @param target
 */
def tag(ret) {
  Logger logger = Logger.getLogger(this)
  createException.init(logger)
  def config = getParam(ret)
  
  def command = new StringBuffer('docker tag')
  
  if (!config.source) {
    logger.error("source is required. source: 'SOURCE_IMAGE[:TAG]'")
    throw createException('RC201')
  }
  
  if (!config.target) {
    logger.error("target is required. target: 'TARGET_IMAGE[:TAG]'")
    throw createException('RC202')
  }
  
  command.append " ${config.source}"
  command.append " ${config.target}"

  sh command.toString()
}


private def pushWithCredentialsId(config, command, logger) {
  def loginCommand
  logger.debug("Login with jenkins credential : ${config.credentialsId}")
  withCredentials([usernamePassword(credentialsId: config.credentialsId, passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USER')]) {
    if (config.registry) {
      logger.debug("Registry : ${config.registry}")
      loginCommand = "docker login ${config.registry} -u ${DOCKER_USER} -p ${DOCKER_PASSWORD}"
    } else {
      logger.debug("Registry : docker.io")
      loginCommand = "docker login -u ${DOCKER_USER} -p ${DOCKER_PASSWORD}"
    }

    sh """
      ${loginCommand}
      ${command}
      docker logout
    """
  }
}

private def pushWithUsernameAndPassword(config, command, logger) {
  def loginCommand
  logger.debug("Login with username/password")
  if (config.registry) {
    logger.debug("Registry : ${config.registry}")
    loginCommand = "docker login ${config.registry} -u ${config.username} -p ${config.password}"
  } else {
    logger.debug("Registry : docker.io")
    loginCommand = "docker login -u ${config.username} -p ${config.password}"
  }
  
  wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: config.password, var: 'foo']]]) {
    sh """
      ${loginCommand}
      ${command}
      docker logout
    """
  }
}

@NonCPS
private def getFullRepository(config, logger) {
  //config.registry
  //config.imageName
  //config.imageVersion
  
  if (!config.imageName) {
    logger.error("imageName is required.")
    throw new RetortException('RC203')
  }
  
  StringBuffer repository = new StringBuffer()
  if (config.registry) {
    repository.append("${config.registry}/")
  }
  
  repository.append(config.imageName)
  
  if (config.imageVersion) {
    repository.append(":${config.imageVersion}")
  }

  return repository.toString()
}

@NonCPS
private def setBuildArgs(command, config, logger) {
  if (!config.buildArgs) {
    return
  }

  if (config.buildArgs instanceof Map) {
    command.append(config.buildArgs.collect { k, v ->
      logger.debug("BUILD ARG : ${k} = ${v}")
      return " --build-arg ${k}=${v}"
    }.join())
  } else {
      logger.error("buildArgs option only supports Map type parameter.")
      logger.error("example : ['key1':'value1','key2':'value2']")
      throw new RetortException('RC204')
  }
}

@NonCPS
private def setTag(command, config, logger) {
  if (!config.tag) {
    return
  }

  if ((config.tag instanceof List) || config.tag.getClass().isArray()) {
    command.append config.tag.collect { t ->
      logger.debug("TAG : ${t}")
      return " -t ${t}"
    }.join()

  } else if (config.tag instanceof CharSequence) {
    appendCommand(config, 'tag', '-t', command, logger)
  }

}

@NonCPS
private def appendCommand(config, configName, option, command, logger) {
  def value = config.get(configName)
  if (value) {
    logger.debug("${configName.toUpperCase()} : ${value}")
    if (option) {
      command.append(" ${option} ${value}")
    } else {
      command.append(" ${value}")
    }
  }
}
 
