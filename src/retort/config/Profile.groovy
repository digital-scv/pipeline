package retort.config

import retort.utils.logging.Logger

public class Profile {
	Logger logger
	Script script  // currently running script
	def env

	public Profile(def env){
		this.env = env
	}

	public Profile(Script script){
		this.script = script
		this.logger = Logger.getLogger(script);
		this.env = script.env
	}

	@NonCPS
	public String[] candidate(String[] names, String... defs){
		names = names ?: defs

		if(env.CONF_LOCATION){
			names = names.collect{return "${env.CONF_LOCATION}/$it"}	
		}

		if(!env.PROFILE)
			return names

		def cand = names.inject([]){r,e ->
			logger.debug("$e in $r")

			r << e.replaceAll(/\.(?=[^\.]+$)/, "-${env.PROFILE}.")
			r << e	
		}

		logger.info("Config files order :: ${cand} with [PROFILE:'$env.PROFILE', CONF_LOCATION:'$env.CONF_LOCATION' names=${names}]")
		return cand
	}
}