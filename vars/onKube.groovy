/*
 * Sandbox : https://github.com/jenkinsci/script-security-plugin/blob/master/src/main/resources/org/jenkinsci/plugins/scriptsecurity/sandbox/whitelists/blacklist
 */
import retort.agent.k8s.ContainerBuilder
import static groovy.json.JsonOutput.toJson

def run(Map config, String script){
	def builder = new ContainerBuilder(this)
	builder.extend(config.agent ?: config)

	run(builder, script)
}

def run(ContainerBuilder builder, String script){
	def args  = builder.build()
	env.label = "modular-${UUID.randomUUID().toString()}"
	args.label = env.label
	echo "$args"

	podTemplate(args) {
	  //Load prepared Jenkinsfiles (load and run string)
      //- https://github.com/jenkinsci/workflow-cps-global-lib-plugin/blob/master/src/main/java/org/jenkinsci/plugins/workflow/libs/LibraryAdder.java#L194
      //- https://stackoverflow.com/a/43327284
	  evaluate script
	}
}

def builder(String yaml){
	return new ContainerBuilder(this, yaml)
}

// For advenced pipeline user
//
// def args = new ContainerBuilder(this)
//   .container(name:'maven', command:'bash')
//   .container(name:'kubectl', image:'kamshak/jenkins-kubectl', command:'cat')
//   .volume(hostPathVolume: [hostPath: '/appdata/maven', mountPath: '/root/.m2'])
//   .volume(persistentVolumeClaim: [claimName: 'slave-workspace', mountPath: '/app/workspace'])
//   .build()
//
//
// def config = yaml.load('<yaml string>')
// def args = new ContainerBuilder(this)
//   .extend(config)
//   .container(name:'maven', command:'bash')
//   .volume(persistentVolumeClaim: [claimName: 'slave-workspace', mountPath: '/app/workspace'])
//   .build()
//
//
// def yaml = '<yaml string>'
// def args = new ContainerBuilder(this, yaml)
//   .container(name:'maven', command:'bash')
//   .volume(persistentVolumeClaim: [claimName: 'slave-workspace', mountPath: '/app/workspace'])
//   .build()
