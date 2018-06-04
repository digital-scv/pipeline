/* Will executed in node */
import jenkins.scm.api.SCMFileSystem

def call(def s) {
  SCMFileSystem fs = SCMFileSystem.of(currentBuild.rawBuild.getParent(), scm)
  println fs
  if (fs != null) {
    String script = fs.child('pipeline.yaml').contentAsString();
    println script
    env._config = script
  } else {
    //listener.getLogger().println("Lightweight checkout support not available, falling back to full checkout.");
  }
}
