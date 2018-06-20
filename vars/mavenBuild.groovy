import retort.utils.logging.Logger
import retort.exception.RetortException
import static retort.utils.Utils.delegateParameters as getParam

/**
 * mavenBuild
 *
 * @param jdkTool
 * @param mavenTool
 * @param pom
 * @param goal
 * @param profile
 * @param systemProperties
 * @param settingsID
 * @param globalSettingsID
 * @param options
 */
def call(ret) {
  Logger logger = Logger.getLogger(this)
  createException.init(logger)
  def config = [:]
  config = getParam(ret)

  def command = new StringBuffer()
  
  setJavaHome(command, config, logger)
  setMavenCommand(command, config, logger)
  setPom(command, config, logger)
  setGoal(command, config, logger)
  setProfile(command, config, logger)
  setSystemProperties(command, config, logger)

  setOptions(command, config, logger)
  
  executeMvn(command, config, logger)
  
}

/**
 * update maven pom version
 *
 * pom
 * version
 */
def version(ret) {
  Logger logger = Logger.getLogger(this)
  def config = getParam(ret, [pom: 'pom.xml'])
  
  if (!config.version) {
    logger.error('version value is required.')
    createException('RC104')
  }
  
  def pom = readMavenPom file: config.pom
  logger.info("Updating pom version ${pom.getVersion()} -> ${config.version}")
  pom.setVersion(config.version)
  
  writeMavenPom model: pom
}


private def executeMvn(command, config, logger) {
  def settingsCommand = new StringBuffer()
  def configFiles = []
  if (config.settingsID) {
    logger.debug("Settings ID : ${config.settingsID}")
    configFiles.add configFile(fileId: config.settingsID, variable: 'MAVEN_SETTINGS')
  }
  if (config.globalSettingsID) {
    logger.debug("Global Settings ID : ${config.globalSettingsID}")
    configFiles.add configFile(fileId: config.globalSettingsID, variable: 'MAVEN_GLOBAL_SETTINGS')
  }
  
  if (configFiles.size() > 0) {
    configFileProvider (configFiles) {
      if (config.settingsID) {
        command.append(" -s ${MAVEN_SETTINGS}")
      }
      if (config.globalSettingsID) {
        command.append(" -gs ${MAVEN_GLOBAL_SETTINGS}")
      }
      
      sh command.toString()
    }
  } else {
    sh command.toString()
  }
}


private def setJavaHome(command, config, logger) {
  // set 
  if (config.jdkTool) {
    try {
      def jdkHome = tool config.jdkTool
      logger.debug("JAVA_HOME : ${jdkHome}")
      command.append("export JAVA_HOME='${jdkHome}' && ")
    } catch (Exception e) {
      logger.error(e.getMessage())
      throw createException('RC101', e, config.jdkTool)
    }
  }
}

private def setMavenCommand(command, config, logger) {
  def mvnHome = ''
  if (config.mavenTool) {
    try {
      mvnHome = tool config.mavenTool
      logger.debug("MAVEN_HOME : ${mvnHome}")         
      command.append("${mvnHome}/bin/mvn")
    } catch (Exception e) {
      logger.error(e.getMessage())
      throw createException('RC102', e, config.mavenTool)
    }
  } else {
    command.append("mvn")
  }
}

@NonCPS
private def setPom(command, config, logger) {
  if (config.pom) {
    logger.debug("POM : ${config.pom}")
    command.append(" -f ${config.pom}")
  }
}

@NonCPS
private def setGoal(command, config, logger) {
  if (config.goal) {
    logger.debug("GOAL : ${config.goal}")
    command.append(" ${config.goal}")
  }
}

@NonCPS
private def setProfile(command, config, logger) {
  if (!config.profile) {
    return
  }
  
  def profiles = new StringBuffer()
  if ((config.profile instanceof List) || config.profile.getClass().isArray()) {
    profiles.append(config.profile.collect().join(","))
  } else {
    profiles.append(config.profile)
  }
  
  logger.debug("PROFILE : ${profiles.toString()}")
  command.append(" -P${profiles.toString()}")
}

@NonCPS
private def setSystemProperties(command, config, logger) {
  if (config.systemProperties) {
    if (config.systemProperties instanceof Map) {
      command.append(config.systemProperties.collect { k, v ->
        logger.debug("SYSTEM PROPERTY : ${k} = ${v}")
        return " -D${k}=${v}"
      }.join())
    } else {
      logger.error("System Properties only support Map type parameter.")
      logger.error("example : ['key1':'value1','key2':'value2']")
      
      throw new RetortException('RC103')
    }
  }
}

@NonCPS
private def setOptions(command, config, logger) {
  if (config.options) {
    logger.debug("OPTIONS : ${config.options}")
    command.append(" ${config.options}")
  }
}
