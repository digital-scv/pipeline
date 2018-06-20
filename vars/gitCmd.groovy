import retort.utils.logging.Logger
import static retort.utils.Utils.delegateParameters as getParam

/**
 * git checkout
 *
 * @param url
 * @param branch
 * @param credentialsId
 * @param poll
 * @param changelog
 */
def checkout(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret)
  
  def repo = git(config)
  
  env.SCM_INFO = repo.inspect()
  
  return repo
}


/**
 * @param file string or list
 * @param message
 * @param authorName
 * @param authorEmail
 */
def commit(ret) {
  Logger logger = Logger.getLogger(this)
  
  def config = [:]
  if (env.SCM_INFO) {
    def repo = Eval.me(env.SCM_INFO)
    config.authorName = repo.GIT_AUTHOR_NAME
    config.authorEmail = repo.GIT_AUTHOR_EMAIL
    config.message = """\"Commit from Jenkins system.
JOB : ${env.JOB_NAME}
BUILD_NUMBER : ${env.BUILD_NUMBER}
BUIlD_URL : ${env.BUILD_URL}\"
""" 
  }
  
  config = getParam(ret, config)
  
  def command = new StringBuffer()
  
  if (config.authorName) {
    command.append("git config --global user.name \"${config.authorName}\"\n")
  } else {
    command.append("git config --global user.name \"JENKINS-SYSTEM\"\n")
  }

  if (config.authorEmail) {
    command.append("git config --global user.email \"${config.authorEmail}\"\n")
  } else {
    command.append("git config --global user.email \"jenkins.system@jenkins.com\"\n")
  }
  
  if (config.file in CharSequence) {
    logger.debug("Staging ${config.file}")
    command.append("git add ${config.file}\n")
  } else if ((config.file instanceof List) || config.file.getClass().isArray()) {
    config.file.each {
      logger.debug("Staging ${it}")
      command.append("git add ${it}\n")
    }
  } else {
    logger.debug('commit: file only support List, Array or String type parameter.')
    createException('RC502')
  }
  
  if (config.message) {
    command.append("git commit -m '${config.message}'")
  } else {
    logger.debug('commit : message is required.')
    createException('RC501')
  }

  sh command.toString()
}

/**
 * @param gitUrl
 * @param credentialsId required.
 * @param tags : false
 */
def push(ret) {
  Logger logger = Logger.getLogger(this)
  def config = [:]
  if (env.SCM_INFO) {
    def repo = Eval.me(env.SCM_INFO)
    config.put('gitUrl', repo.GIT_URL)
  } else if (env.GIT_URL) {
    config.put('gitUrl', env.GIT_URL)
  }

  config = getParam(ret, config)
  
  if (!config.gitUrl) {
    logger.error('push : When calling push function, set gitUrl value or set it to property GIT_URL.')
    throwException('RC504')
  }

  def command = new StringBuffer('git push ')
  
  if (config.tags == true) {
    command.append('--tags ')
  }
  
  
  if (config.credentialsId) {
    URI gitUri = new URI(config.gitUrl)
    
    withCredentials([usernamePassword(credentialsId: config.credentialsId, passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USER')]) {
      // protocol
      command.append("${gitUri.getScheme()}")
      command.append("://")
      // username
      command.append("${GIT_USER?:''}")
      // password
      command.append("${GIT_PASSWORD?':'+GIT_PASSWORD+'@':GIT_USER?'@':''}")
      // host
      command.append("${gitUri.getHost()}")
      // port
      command.append("${gitUri.getPort()?':'+gitUri.getPort():''}")
      // path
      command.append("${gitUri.getPath()}")
      sh command.toString()
    }
  } else {
    command.append("${config.gitUrl}")
    sh command.toString()
  }


}

/**
 * @param tag env.VERSION
 * @param message
 * @param credentialsId required.
 */
def tag(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret, [ 
    message : """\"Release from Jenkins system.
JOB : ${env.JOB_NAME}
BUILD_NUMBER : ${env.BUILD_NUMBER}
BUIlD_URL : ${env.BUILD_URL}\"
""" ,
    tag: env.VERSION
  ])
  
  if (!config.tag) {
    logger.error('tag : When calling tag function, set tag value or set it to property VERSION.')
    createException('RC503')
  }
  
  def command = new StringBuffer('git tag ')

  command.append("-a ${config.tag} -m ${config.message}")
  sh command.toString()
  
  def config2 = config.clone()
  config2.put('tags', true)
  push(config2)
}

