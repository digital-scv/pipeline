//Not work
// this.xxx = 'xxx'
// def String xxx = 'xxx'

def eval(String name){
	switch(name){
	  case 'maven':
	  case 'maven2':
	    return mavenBuild
	}
}

def eval(String name, Object args){
	//At blueocean, every log is on single step. (actually no step)
	eval2(name, args)()
}

def eval2(String name, Object args){
	if(args in String){
	  // maven: mvn clean install -Dmaven.test.skip=true

	  println "Run as single string shell. ${args}"
	  
	  def closure = {sh args}
	  //closure.delegate = env.VARIABLES?: [:]
	  return closure
	} else if ( consistOf(args, String) ){
	  // maven:
	  // - mvn clean install
	  // - -Dmaven.test.skip=true
	  // - -Dmaven.xxx.yyy=zzz

	  //println "Run as multi line string shell. ${args}"
	  def closure = { args.each {sh it} }
	  //closure.delegate = env.VARIABLES?: [:]
	  return closure
	}

	def func = eval(name)
	if(!func){
	  throw new UnsupportedOperationException("Can not find step : ${name}")
	}

    // maven
	//   goal: clean install
	//   options: -Dmaven.test.skip=true

	//println "Run as mapped arguments. ${name}, ${func.class}, ${args}"
	return {func args}
}

private def consistOf(def obj, def type){
	//Reduce (http://mrhaki.blogspot.com/2009/09/groovy-goodness-using-inject-method.html)
	obj in List && obj.inject(true){r,e -> r && e in type}	
}