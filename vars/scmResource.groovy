/* Will executed in node */
import java.nio.file.FileSystemNotFoundException
import jenkins.scm.api.SCMFileSystem
import retort.utils.logging.Logger

def call(String... paths) {
  Logger log = Logger.getLogger(this)

  SCMFileSystem fs = SCMFileSystem.of(currentBuild.rawBuild.getParent(), scm)
  log.debug('Loaded FileSystem :: $fs')
  if (fs == null) {
  	throw new FileSystemNotFoundException('Can not create SCMFileSystem with ${scm}')
  }

  for(path in paths){
  	try{
      String script = fs.child(path).contentAsString();
      log.debug(script)
      return script
	} catch (Exception ex) {
	  log.info("$ex")
	}
  }
}
