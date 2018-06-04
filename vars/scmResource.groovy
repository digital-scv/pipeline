/* Will executed in node */
import jenkins.scm.api.SCMFileSystem

def call(String name) {
  SCMFileSystem fs = SCMFileSystem.of(currentBuild.rawBuild.getParent(), scm)
  println fs
  if (fs != null) {
    String script = fs.child(name).contentAsString();
    println script
    return script
  } else {
    //listener.getLogger().println("Lightweight checkout support not available, falling back to full checkout.");
  }
}
