import retort.utils.logging.Logger
import static retort.utils.Utils.delegateParameters as getParam

def build(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret) {
    path = '.'
  }
  
  def command = new StringBuffer()
  command.append('docker build')
  
  appendCommand(config, 'file', '-f', command, logger)
  setTag(command, config, logger)
  appendCommand(config, 'options', '', command, logger)
  appendCommand(config, 'path', '', command, logger)
  
  sh command.toString()
}

def tag(ret) {
  Logger logger = Logger.getLogger(this)
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
 
