import retort.utils.logging.Logger
import static retort.utils.Utils.delegateParameters as getParam

def build(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret) {
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

def tag(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret)
  
  def command = new StringBuffer('docker tag')
  
  if (!config.source) {
    logger.error("source is required. source: 'SOURCE_IMAGE[:TAG]'")
    throw new Exception("source is required. source: 'SOURCE_IMAGE[:TAG]'")
  }
  
  if (!config.target) {
    logger.error("target is required. target: 'TARGET_IMAGE[:TAG]'")
    throw new Exception("target is required. target: 'TARGET_IMAGE[:TAG]'")
  }
  
  command.append " ${config.source}"
  command.append " ${config.target}"

  sh command.toString()
}

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
      throw new Exception('buildArgs option only supports Map type parameter.')
  }
}


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
 
