def shortName = 'base'
def projectName = "jak-${shortName}"
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
    shell("docker push localhost:5000/${projectName}:latest")
  }
}
