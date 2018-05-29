package retort.utils.logging

import com.cloudbees.groovy.cps.NonCPS
import retort.utils.ConfigConstants
import org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException
import org.jenkinsci.plugins.workflow.cps.DSL

/**
 * Logging functionality for pipeline scripts.
 */
class Logger implements Serializable {

  private static final long serialVersionUID = 1L

  /**
   * Reference to the dsl/script object
   */
  private DSL dsl

  /**
   * Reference to the CpsScript/WorkflowScript
   */
  private Script script

  /**
   * The log level
   */
  private LogLevel level = LogLevel.TRACE

  /**
   * The name of the logger
   */
  private String name = ""

  /**
   * Flag if the logger is initialized
   */
  private Boolean initialized = false

  /**
   * @param name The name of the logger
   */
  Logger(String name = "", LogLevel logLevel) {
    this.name = name
    this.logLevel = logLevel
  }

  /**
   * @param logScope The object the logger is for. The name of the logger is autodetected.
   */
  Logger(Object logScope, LogLevel logLevel) {
    init((Script) logScope, logLevel)
    
    if (logScope instanceof Object) {
      this.name = getClassName(logScope)
      if (this.name == null) {
        this.name = "$logScope"
      }
    }
  }
  
  @NonCPS
  static Logger getLogger(Script script) {
    LogLevel logLevel
    if (script == null) {
      logLevel = LogLevel.INFO
    } else {
      def scriptName
      if (script instanceof Object) {
        scriptName = getClassName(script)
        if (scriptName == null) {
          scriptName = "$script"
        }
      }
      
      def localLevel = scriptName + '.LOG_LEVEL';
      
      if (script.env.getProperty(localLevel)) {
        logLevel = LogLevel.fromString(script.env.getProperty(localLevel))                                                                 
      } else if (script.env.LOG_LEVEL) {
        logLevel = LogLevel.fromString(script.env.LOG_LEVEL)
      } else {
        logLevel = LogLevel.INFO
      }
    }

    return new Logger(script, logLevel)
  }

  /**
   * Initializes the logger with CpsScript object and LogLevel
   *
   * @param script CpsScript object of the current pipeline script (available via this in pipeline scripts)
   * @param map The configuration object of the pipeline
   */
  @NonCPS
  private void init(Script script, LogLevel logLvl = LogLevel.INFO) {
    if (logLvl == null) logLvl = LogLevel.INFO
    level = logLvl
    if (initialized == true) {
      return
    }
    this.script = script
    this.dsl = (DSL) script.steps
    initialized = true
  }

  /**
   * Initializes the logger with CpsScript object and configuration map
   *
   * @param script CpsScript object of the current pipeline script (available via this in pipeline scripts)
   * @param map The configuration object of the pipeline
   */
  @NonCPS
  private void init(Script script, Map map) {
    LogLevel lvl
    if (map) {
      lvl = map[ConfigConstants.LOGLEVEL] ?: LogLevel.INFO
    } else {
      lvl = LogLevel.INFO
    }
    init(script, lvl)
  }

  /**
   * Initializes the logger with CpsScript object and loglevel as string
   *
   * @param script CpsScript object of the current pipeline script (available via this in pipeline scripts)
   * @param sLevel the log level as string
   */
  @NonCPS
  private void init(Script script, String sLevel) {
    if (sLevel == null) sLevel = LogLevel.INFO
    init(script, LogLevel.fromString(sLevel))
  }

  /**
   * Initializes the logger with DSL/steps object and loglevel as integer
   *
   * @param script CpsScript object of the current pipeline script (available via this in pipeline scripts)
   * @param iLevel the log level as integer
   *
   */
  @NonCPS
  private void init(Script script, Integer iLevel) {
    if (iLevel == null) iLevel = LogLevel.INFO.getLevel()
    init(script, LogLevel.fromInteger(iLevel))
  }

  /**
   * Logs a trace message followed by object dump
   *
   * @param message The message to be logged
   * @param object The object to be dumped
   */
  @NonCPS
  void trace(String message, Object object) {
    log(LogLevel.TRACE, message, object)
  }

  /**
   * Logs a info message followed by object dump
   *
   * @param message The message to be logged
   * @param object The object to be dumped
   */
  @NonCPS
  void info(String message, Object object) {
    log(LogLevel.INFO, message, object)
  }

  /**
   * Logs a debug message followed by object dump
   *
   * @param message The message to be logged
   * @param object The object to be dumped
   */
  @NonCPS
  void debug(String message, Object object) {
    log(LogLevel.DEBUG, message, object)
  }

  /**
   * Logs warn message followed by object dump
   *
   * @param message The message to be logged
   * @param object The object to be dumped
   */
  @NonCPS
  void warn(String message, Object object) {
    log(LogLevel.WARN, message, object)
  }

  /**
   * Logs a error message followed by object dump
   *
   * @param message The message to be logged
   * @param object The object to be dumped
   */
  @NonCPS
  void error(String message, Object object) {
    log(LogLevel.ERROR, message, object)
  }

  /**
   * Logs a fatal message followed by object dump
   *
   * @param message The message to be logged
   * @param object The object to be dumped
   */
  @NonCPS
  void fatal(String message, Object object) {
    log(LogLevel.FATAL, message, object)
  }

  /**
   * Logs a trace message
   *
   * @param message The message to be logged
   * @param object The object to be dumped
   */
  @NonCPS
  void trace(String message) {
    log(LogLevel.TRACE, message)
  }

  /**
   * Logs a trace message
   *
   * @param message The message to be logged
   * @param object The object to be dumped
   */
  @NonCPS
  void info(String message) {
    log(LogLevel.INFO, message)
  }

  /**
   * Logs a debug message
   *
   * @param message The message to be logged
   * @param object The object to be dumped
   */
  @NonCPS
  void debug(String message) {
    log(LogLevel.DEBUG, message)
  }

  /**
   * Logs a warn message
   *
   * @param message The message to be logged
   * @param object The object to be dumped
   */
  @NonCPS
  void warn(String message) {
    log(LogLevel.WARN, message)
  }

  /**
   * Logs a error message
   *
   * @param message The message to be logged
   * @param object The object to be dumped
   */
  @NonCPS
  void error(String message) {
    log(LogLevel.ERROR, message)
  }

  /**
   * Logs a fatal message
   *
   * @param message The message to be logged
   * @param object The object to be dumped
   */
  @NonCPS
  void fatal(String message) {
    log(LogLevel.FATAL, message)
  }

  /**
   * Helper function for logging/dumping a object at the given log level
   *
   * @param logLevel the loglevel to be used
   * @param message The message to be logged
   * @param object The object to be dumped
   */
  @NonCPS
  void log(LogLevel logLevel, String message, Object object) {
    if (doLog(logLevel)) {
      def objectName = getClassName(object)
      if (objectName != null) {
        objectName = "($objectName) "
      } else {
        objectName = ""
      }

      def objectString = object.toString()
      String msg = "$name : $message -> $objectName$objectString"
      writeLogMsg(logLevel, msg)
    }
  }

  /**
   * Helper function for logging at the given log level
   *
   * @param logLevel the loglevel to be used
   * @param message The message to be logged
   */
  @NonCPS
  void log(LogLevel logLevel, String message) {
    if (doLog(logLevel)) {
      String msg = "$name : $message"
      writeLogMsg(logLevel, msg)
    }
  }

  /**
   * Utility function for writing to the jenkins console
   *
   * @param logLevel the loglevel to be used
   * @param msg The message to be logged
   */
  @NonCPS
  private void writeLogMsg(LogLevel logLevel, String msg) {
    String lvlString = "[${logLevel.toString()}]"

    if (script != null) {
      // check if color can be used
      def canPrintColor = null
      try {
        canPrintColor = isUnderAnsiColorWrapper()
      } catch (Exception ex) {
        // ex.printStackTrace()
      }
      if (canPrintColor) {
        String colorCode = logLevel.getColorCode()
        lvlString = "\u001B[${colorCode}m${lvlString}\u001B[0m"
      }
    }
    if (dsl != null) {
      dsl.echo("$lvlString $msg")
    }
  }
  
  @NonCPS
  private boolean isUnderAnsiColorWrapper() {
    hudson.console.ConsoleLogFilter filter = org.jenkinsci.plugins.workflow.cps.CpsThread.current().getContextVariables().get(hudson.console.ConsoleLogFilter.class)
    
    if (filter) {
      def filterClassName = filter.getClass().getCanonicalName()
      if (ConfigConstants.ANSI_COLOR_LOG_FILTER == filterClassName) {
        return true
      } else if (ConfigConstants.MERGED_LOG_FILTER == filterClassName) {
        return containsAnsiColorConsoleLogFilter(filter)
      }
    }
    
    return false
  }

  @NonCPS
  private boolean containsAnsiColorConsoleLogFilter(def mergedFilter) {
    def original = mergedFilter.original
    def subsequent = mergedFilter.subsequent
    
    def originalFilterClassName = original.getClass().getCanonicalName()
    def subsequentFilterClassName = subsequent.getClass().getCanonicalName()  
    if (ConfigConstants.ANSI_COLOR_LOG_FILTER == originalFilterClassName) {
      return true
    } else if (ConfigConstants.ANSI_COLOR_LOG_FILTER == subsequentFilterClassName) {
      return true
    }
    
    if (ConfigConstants.MERGED_LOG_FILTER == originalFilterClassName && containsAnsiColorConsoleLogFilter(original)) {
      return true
    } else if (ConfigConstants.MERGED_LOG_FILTER == subsequentFilterClassName && containsAnsiColorConsoleLogFilter(subsequent)) {
      return true
    }

    return false
  }


  /**
   * Utiltiy function to determine if the given logLevel is active
   *
   * @param logLevel
   * @return true , when the loglevel should be displayed, false when the loglevel is disabled
   */
  @NonCPS
  private boolean doLog(LogLevel logLevel) {
    if (logLevel.getLevel() >= level.getLevel()) {
      return true
    }
    return false
  }
  
  LogLevel getLogLevel() {
    return level               
  }


  /**
   * Helper function to get the name of the object
   * @param object
   * @return
   */
  @NonCPS
  private static String getClassName(Object object) {
    String objectName = null
    // try to retrieve as much information as possible about the class
    try {
      Class objectClass = object.getClass()
      objectName = objectClass.getName().toString()
      objectName = objectClass.getCanonicalName().toString()
    } catch (RejectedAccessException e) {
      // do nothing
    }

    return objectName
  }
}