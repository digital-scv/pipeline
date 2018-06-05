package retort.utils

/**
 * Utility class.
 */
class Utils {

  /**
   * parameter to map with default values.
   */
  static def delegateParameters(ret, defaultConfig) {
    def config = [:]
    if (defaultConfig) {
      if (defaultConfig instanceof Map) {
        config = defaultConfig
      } else if (defaultConfig instanceof Closure) {
        defaultConfig.resolveStrategy = Closure.DELEGATE_FIRST
        defaultConfig.delegate = config
        defaultConfig()
      }
    }
    
    if (ret instanceof Map) {
        config.putAll(ret)
    } else if (ret instanceof Closure) {
        ret.resolveStrategy = Closure.DELEGATE_FIRST
        ret.delegate = config
        ret()
    }
    
    return config
  }
  
  /**
   * parameter to map.
   */
  static def delegateParameters(ret) {
    def config = [:]
    
    if (ret instanceof Map) {
        config.putAll(ret)
    } else if (ret instanceof Closure) {
        ret.resolveStrategy = Closure.DELEGATE_FIRST
        ret.delegate = config
        ret()
    }
    
    return config
  }
  
  /**
   * get Filename from full path.
   */
  static def getFilename(fileWithPath) {
    return fileWithPath.lastIndexOf(File.separator).with {it != -1 ? fileWithPath.substring(it+1) : fileWithPath}
  }
  
  /**
   * get file extension from full path.
   */
  static def getFileExtension(fileWithPath) {
    def filename = getFilename(fileWithPath)
    return fileWithPath.lastIndexOf('.').with {it != -1 ? fileWithPath.substring(it+1) : fileWithPath}
  }

  /**
   * return file has yaml extension or not.
   */
  static def isYamlfile(fileWithPath) {
    def yamlExtensionList = ['yaml', 'yml']
    def extension = getFileExtension(fileWithPath).toLowerCase()
    
    return yamlExtensionList.contains(extension)
  }
  
  /**
   * return file has json extension or not.
   */
  static def isJsonfile(fileWithPath) {
    def jsonExtensionList = ['json']
    def extension = getFileExtension(fileWithPath).toLowerCase()
    
    return jsonExtensionList.contains(extension)
  }

}