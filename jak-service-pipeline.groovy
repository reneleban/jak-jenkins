node {

    stage('Checkout') {
        checkout([$class: 'GitSCM', branches: [[name: '*/develop']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/reneleban/jak-services.git']]])
    }

    stage('build base') {
        sh "docker build -t jak-base -f base.docker ."
        sh "docker tag jak-base localhost:5000/jak-base:latest"
        sh "docker push localhost:5000/jak-base:latest"
    }

    stage('build services') {
        parallel (
            login: { buildService('login') },
            card: { buildService('card') },
            board: { buildService('board') },
            list: { buildService('list') }
        )
    }

    stage('deploy services') {
        parallel (
            login: { deployService('jak-login', 10030) },
            card: { deployService('jak-card', 10020) },
            board: { deployService('jak-board', 10000) },
            list: { deployService('jak-list', 10010) }
        )
    }
}

def buildService(name) {
    sh "docker build -t jak-${name} -f ${name}.docker ."
    sh "docker tag jak-${name} localhost:5000/jak-${name}:latest"
    sh "docker push localhost:5000/jak-${name}:latest"
}

def deployService(projectName, port) {
    sh "docker stop ${projectName}"
    sh "docker rm ${projectName}"
    sh "docker run -d -v ${projectName}-storage:/application/data -p ${port}:${port} --restart=always --name ${projectName} localhost:5000/${projectName}:latest"
}
