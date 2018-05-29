package retort.utils

class Utils {

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
}