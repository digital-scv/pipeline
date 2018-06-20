//Not work
// this.xxx = 'xxx'
// def String xxx = 'xxx'
import retort.utils.logging.Logger
import static retort.utils.Utils.consistOf
import static retort.utils.Utils.isString

def eval(String name){
	switch(name){
	  case 'maven':  return mavenBuild
	  case 'docker.build':  return dockerCmd.&build
	  case 'docker.push':   return dockerCmd.&push
	  case 'kubectl.apply':           return kubeCmd.&apply
	  case 'kubectl.describe':        return kubeCmd.&describe
	  case 'kubectl.scale':           return kubeCmd.&scale
	  case 'kubectl.delete':          return kubeCmd.&delete
	  case 'kubectl.resourceExists':  return kubeCmd.&resourceExists
	  case 'kubectl.getValue':        return kubeCmd.&getValue
	  case 'kubectl.rolloutStatus':   return kubeCmd.&rolloutStatus
	  case 'kubectl.rolloutUndo':     return kubeCmd.&rolloutUndo
	}
}

def eval(String name, Object args){
	//At blueocean, every log is on single step. (actually no step)
	eval2(name, args)()
}

def eval2(String name, Object args){
	Logger log = Logger.getLogger(this)

	// Evaluate Groovy Expression
	args = interpolation(args)

	// Run by Matched Argument Type
	if( isString(args) ){
	  // maven: mvn clean install -Dmaven.test.skip=true
	  log.info "Run as single line script. ($args)"
	  return {sh args}
	} else if ( consistOf(args, String, GString) ){
	  // maven:
	  // - mvn clean install
	  // - -Dmaven.test.skip=true
	  // - -Dmaven.xxx.yyy=zzz
	  log.info "Run as multi line script loop. ($args)"
	  return { args.each {sh it} }
	}

	def func = eval(name)
	if(!func){
	  throw new UnsupportedOperationException("Can not find step : ${name} := $func")
	}

    // maven
	//   goal: clean install
	//   options: -Dmaven.test.skip=true
	log.info "Run as modular method '$name'. ($args)"
	return {func args}
}

private def interpolation(Object args){
	Logger log = Logger.getLogger(this)

    // Evaluate Groovy Expression
	Binding b = new Binding()
    GroovyShell sh = new GroovyShell(b)

    env.getEnvironment().each {
      log.debug "$it.key(${it.key.getClass()}) = $it.value(${it.value.getClass()})"

      def val = it.value
      if(val in String && val.startsWith('[') && val.endsWith(']'))
	    val = sh.evaluate(val)
      b.setVariable(it.key, val)
    }

	if(args in Map){
      args.each {
      	log.debug "$it.key(${it.key.getClass()}) = $it.value(${it.value.getClass()})"
        args[it.key] = sh.evaluate("\"$it.value\"")
      }
    } else if( consistOf(args, String, GString) ){
      args = args.collect {
      	log.debug "$it(${it.getClass()})"
        return sh.evaluate("\"$it\"")
      }
    } else if( isString(args) ){
	  args = sh.evaluate("\"$args\"")
	  log.debug "$args(${args.getClass()})"
    }

    return args
}