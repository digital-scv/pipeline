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
def descr(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret)
  
  logger.debug('describe executed')
  
  def command = new StringBuffer('kubectl describe')
  
  

}


def testMethod(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret)
  
  logger.debug('test executed')
  
}
