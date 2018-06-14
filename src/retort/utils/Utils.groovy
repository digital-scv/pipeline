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

  static def consistOf(def obj, Class... type){
    //Reduce (http://mrhaki.blogspot.com/2009/09/groovy-goodness-using-inject-method.html)
    obj in List && obj.inject(true){r,e -> r && type.find{e in it}}  
  }

  static def isIn(def obj, Class... type){
    //Reduce (http://mrhaki.blogspot.com/2009/09/groovy-goodness-using-inject-method.html)
    return obj && type.find{obj in it}  
  }

  static def isString(def obj){
    return isIn(obj, String, GString)
  }
}