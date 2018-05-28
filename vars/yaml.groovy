@Grab('org.yaml:snakeyaml:1.17')
import org.yaml.snakeyaml.Yaml as Parser

//Expose static function like 'yaml.load()'
//- https://stackoverflow.com/a/25603288
static def load(String yaml){
  //Parse yaml file to collection
  //- https://stackoverflow.com/a/41731617
  new Parser().load(yaml)
}

static def dump(def yaml){
  new Parser().dump(yaml)
}
