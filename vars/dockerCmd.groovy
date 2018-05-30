import retort.utils.logging.Logger
import static retort.utils.Utils.delegateParameters as getParam

def build(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret) {
    path = '.'
  }
  
  def command = new StringBuffer()
  command.append('docker buid')
  
  if (config.tag) {
    logger.debug("TAG : ${config.tag}")
    command.append(" -t ${config.tag}")
  }

  if (config.path) {
    command.append(" ${config.path}")
  }
  
  sh command.toString()
}
