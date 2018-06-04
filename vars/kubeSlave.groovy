/*
 * Sandbox : https://github.com/jenkinsci/script-security-plugin/blob/master/src/main/resources/org/jenkinsci/plugins/scriptsecurity/sandbox/whitelists/blacklist
 */
import retort.agent.k8s.ContainerBuilder

def call(Map config, String script){
	def builder = new ContainerBuilder(this)
	echo "${builder}"

	def args  = builder.extend(config.agent).build()
	env.label = "modular-${UUID.randomUUID().toString()}"
	args.label = env.label
	echo "${args}"

	podTemplate(args) {
	  evaluate script
	}
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
