def projects = []
projects.add([name: 'login', port: 10030])
projects.add([name: 'card', port: 10020])
projects.add([name: 'list', port: 10010])
projects.add([name: 'board', port: 10000])

projects.each {
  def projectName = 'jak-' + it.name
  def shortName = it.name
  def infrastructureName = it.name + '-infrastructure'
  def port = it.port
  def gitUrl = 'https://github.com/reneleban/jak-services.git'
  def branch = 'develop'

  job(projectName) {
    scm {
      git {
        remote {
          url(gitUrl)
          branches(branch)
        }
      }
    }

    triggers {
      scm('H/15 * * * *')
    }

    steps {
      shell("docker build -t ${projectName} -f ${shortName}.docker .")
      shell("docker tag ${projectName} localhost:5000/${projectName}:latest")
      shell("docker push localhost:5000/${projectName}-:latest")
    }

    postBuildSteps {
      queue(infrastructureName)
    }
  }

  job(infrastructureName) {
    steps {
      shell("docker stop ${projectName}")
      shell("docker rm ${projectName}")
      shell("docker run -d -v ${projectName}-storage:/application/data -p ${port}:${port} --restart=always --name ${projectName} localhost:5000/${projectName}:latest")
    }
  }
}
