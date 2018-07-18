# Overview


# Usage
You don't need to create complex Jenkinsfile for Pipeline.
Instead, configure the pipline using a yaml and Jenkinsfile that contain single method call.

### Set up Your Project
Create two file in your project root.
#### pipeline.yaml (Pipeline Config)
[sample pipeline.yaml](https://github.com/yanghoon/jenkinsfile-test/blob/master/pipeline.yaml)
```yaml
# Define your dynamic pipeline stage
# - Details will be configured as same name
stage:
- build
- deploy

# configuration for 'build' stage.
# - keys are will be used as container name (maven and docker)
# - values are will be executed as shell command
build:
- maven: mvn --version
- docker: docker version

# configuration for 'build' stage. (no effect now)
deploy:
- k8s: k8s/*.yaml

# configuration for creating slave at k8s
agent:
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
```

#### Jenkinsfile (Scratch)
[sample Jenkinsfile](https://github.com/yanghoon/jenkinsfile-test/blob/master/Jenkinsfile)
```groovy
// Just add a below simple Jenkinsfile.
// This code will execute a pipeline that is prepared as pipeline shared library.
@Library('retort') _
heat()
```

### Execute Pipeline Job 
#### Create Pipeline Job with SCM
To use prepared pipeline, select definition type as `Pipeline script from SCM`.
Write down your repository info.
And also click `add` button for creating credentials, if you use private repository.
![](https://user-images.githubusercontent.com/21324361/39913559-40b52ed2-553d-11e8-85a1-fd6e7ed4a4cd.png)

#### Result of Job Execution
The library load your configration(.yaml) at `*prepared` stage.
And then detect two stages using those config.
Stages are run at k8s and master. In k8s, run and print version of tools by configured shell script.
![](https://user-images.githubusercontent.com/21324361/39915043-d5c95c60-5541-11e8-9955-618d931cd85e.png)

#### On Blue Ocean
You can also see pipelines throught Jenkins Blue Ocean.
![](https://user-images.githubusercontent.com/21324361/39915047-d82ab940-5541-11e8-926e-c000c0bd947a.png)

