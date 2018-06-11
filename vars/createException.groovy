import retort.utils.logging.Logger
import retort.utils.MessageUtil
import retort.exception.RetortException

def call(errorCode) {
  Logger logger = Logger.getLogger(this)
  init(logger)
  return new RetortException(errorCode)
}


def call(errorCode, String... args) {
  Logger logger = Logger.getLogger(this)
  init(logger)
  return new RetortException(errorCode, args)
}

def call(errorCode, Throwable throwable) {
  Logger logger = Logger.getLogger(this)
  init(logger)
  return new RetortException(errorCode, throwable)
}

def call(errorCode, Throwable throwable, String... args) {
  Logger logger = Logger.getLogger(this)
  init(logger)
  return new RetortException(errorCode, throwable, args)
}


def init(logger) {
  if (MessageUtil.isInitialized()) {
    return
  }

  logger.debug('MessageUtil initialize')
  
  def r = "message/errorMessage.properties"
    
  def errorMessage = libraryResource r
    
  def messageSource = readProperties text: errorMessage
  
  if (messageSource == null) {
    messageSource = [:]
  }

  MessageUtil.setMessages(messageSource) 
}

