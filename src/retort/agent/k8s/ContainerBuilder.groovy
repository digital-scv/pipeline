package retort.agent.k8s

@Grab('org.yaml:snakeyaml:1.17')
import org.yaml.snakeyaml.Yaml as Parser
import org.jenkinsci.plugins.workflow.cps.DSL
import retort.utils.logging.Logger

/*
 * Avoid "Caused: java.io.NotSerializableException"
 * - http://biouno.org/2016/11/11/what-happens-when-you-make-a-java-member-variable-transient-in-a-jenkins-plugin.html
 */
public class ContainerBuilder implements Serializable {
	Logger logger
	Script script  // currently running script
	Map config     // Map representing yaml
	Map alias      // mapping yaml field on matched method (eg. collectors -> collector)
	
	transient Parser parser = new Parser()

    public ContainerBuilder(Script script){
		//String _default = this.getClass().getResource('/retort/agent/k8s/config.yaml').text
		this(script,
"""
containers:
- name: maven
  image: maven:3.3.9-jdk-8-alpine
  ttyEnabled: true
  command: cat
- name: docker
  image: docker
  ttyEnabled: true
  command: cat
volumes:
- hostPathVolume:
    hostPath: '/var/run/docker.sock'
    mountPath: '/var/run/docker.sock'
- emptyDirVolume:
    mountPath: '/root/.m2'
    memoty: false
"""
		)
    }

    public ContainerBuilder(Script script, String yaml){
        this.script = script
		this.logger = Logger.getLogger(script);

		this.config = parser.load(yaml)

		//Method pointer operator  := http://groovy-lang.org/operators.html#method-pointer-operator
		this.alias = [
			containers: this.&container,
			volumes   : this.&volume
		]	
    }

	public def build(def ret){
		logger.info("config.default    = ${parser.dump(config)}")

		def _new = [:]
		_new.containers = config.containers.collect { script.containerTemplate(it) }
		_new.volumes = config.volumes.collect {it.collect {script."${it.key}"(it.value)} }
		_new.volumes = _new.volumes.flatten()  //Flatten Complex List (https://stackoverflow.com/a/11558564)
		_new.label = 'aaaa'

		logger.info("_new.containers   = ${_new.containers}")
		logger.info("_new.volumes      = ${_new.volumes}")

		return _new

		//TODO: Call a podTempalte step here. not outside.
		//script.podTemplate(config) ret

		/*
		 * Spread Collection          := http://mrhaki.blogspot.com/2010/01/groovy-goodness-apply-method-to-all.html
		 * Dynamic Method invocation  := https://stackoverflow.com/a/1357976
		 * With Statement             := http://mrhaki.blogspot.com/2009/09/groovy-goodness-with-method.html
		 * Functional Programing :: Map
		 * - https://functionalgroovy.wordpress.com/tag/map/
		 * - https://dzone.com/articles/functional-programming-groovy
		 */
		/*
		script.with {
			config.containers = config.containers.collect { containerTemplate(it) }
			config.voluems = config.volumes*.collect { "${it.key}"(it.value) }
			logger.info("${config.containers}")
			logger.info("${volumes}")
		}
		*/

		/*
		podTemplate(label:label,
		    serviceAccount: 'jenkins-release',
		    containers: [
		        containerTemplate(name: 'maven', image: 'maven:3.5.2-jdk-8-alpine', ttyEnabled: true, command: 'cat'),
		        containerTemplate(name: 'docker', image: 'docker', ttyEnabled: true, command: 'cat'),
		        containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl', ttyEnabled: true, command: 'cat')
		    ],
		    volumes: [
		        hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
		        persistentVolumeClaim(mountPath: '/root/.m2', claimName: MAVEN_REPO_PVC)
		    ])
		*/
	}

	/**
	 * extend all possible field to current config
	 */
	public ContainerBuilder extend(Map ext){
		ext.each {
			if (alias[it.key])
				alias[it.key](it.value)
		}

		return this;
	}

	/**
	 * extend containerTemplate config
	 */
	public ContainerBuilder container(Map... args){
		logger.debug("config = ${config}")
		merge(config.containers, args, {it.name == ext.name})

		return this;
	}

	/**
	 * extend each volume types config
	 */
	public ContainerBuilder volume(Map... args){
		logger.debug("config = ${config}")
		merge(config.volumes, args, { compVolumeName(it, ext) })

		return this;
	}

    
	/**
	 * check equality between two volumes for extending
	 */
	public def compVolumeName(Map v1, Map v2){
		//- http://mrhaki.blogspot.com/2011/04/groovy-goodness-see-if-sets-are-equal.html
		return v1.keySet() == v2.keySet() && volumeName(v1) == volumeName(v2)
	}

	/**
	 * extract each volumes name(identifier)
	 */
	public def volumeName(Map v){
		def res = v.collect {
			//Groovy Switch Statement  := http://mrhaki.blogspot.com/2009/08/groovy-goodness-switch-statement.html
			switch (it.key) {
				case 'hostPathVolume':        return it.value.hostPath
				case 'persistentVolumeClaim': return it.value.claimName
				default:
					logger.wran("${it.key} is not supported.")
			}
		}

		logger.info("${res} := key of ${v}")

		//TODO: validation if res.length == 0
		return res[0]
	}

	/**
	 * merge(extend) two List<Map> with comp that check those name(identifier) is same
	 */
	public merge(base, extend, comp){
		extend.each { ext ->
			//Closure variable scope  := http://m.blog.daum.net/stshms/7
			comp.delegate = [ext:ext]
			comp.resolveStrategy = Closure.DELEGATE_FIRST

			logger.debug("ext    = ${ext}")
			logger.debug("base   = ${base}")

			//Find list item  := http://grails.asia/groovy-find
			def found = base.find comp //{it.name == ext.name}
			logger.debug("found  = ${found}")

        	//Merge two maps or append item to array  := http://mrhaki.blogspot.com/2010/04/groovy-goodness-adding-maps-to-map_21.html
			(found ?: base) << ext
		}
	}
}