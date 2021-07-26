def dockerHub = "ghcr.io/icgc-argo/workflow-relay"
def gitHubRepo = "icgc-argo/workflow-relay"
def commit = "UNKNOWN"
def version = "UNKNOWN"

pipeline {
    agent {
        kubernetes {
            label 'relay-executor'
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: jdk
    tty: true
    image: openjdk:11
    env:
      - name: DOCKER_HOST
        value: tcp://localhost:2375
  - name: dind-daemon
    image: docker:18.06-dind
    securityContext:
      privileged: true
      runAsUser: 0
    volumeMounts:
      - name: docker-graph-storage
        mountPath: /var/lib/docker
  - name: helm
    image: alpine/helm:2.12.3
    command:
    - cat
    tty: true
  - name: docker
    image: docker:18-git
    tty: true
    env:
      - name: DOCKER_HOST
        value: tcp://localhost:2375
      - name: HOME
        value: /home/jenkins/agent
  securityContext:
    runAsUser: 1000
  volumes:
  - name: docker-graph-storage
    emptyDir: {}
"""
        }
    }
    stages {
                stage('Prepare') {
                    steps {
                        script {
                            commit = sh(returnStdout: true, script: 'git describe --always').trim()
                            version = readMavenPom().getVersion()
                        }
                    }
                }
                stage('Test') {
                    steps {
                        container('jdk') {
                            sh "./mvnw test"
                        }
                    }
                }
                stage('Build & Publish Develop') {
                    when {
                        branch "develop"
                    }
                    steps {
                        container('docker') {
                            withCredentials([usernamePassword(credentialsId:'argoContainers', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                                sh 'docker login ghcr.io -u $USERNAME -p $PASSWORD'
                            }

                            // DNS error if --network is default
                            sh "docker build --network=host . -t ${dockerHub}:edge -t ${dockerHub}:${version}-${commit}"

                            sh "docker push ${dockerHub}:${version}-${commit}"
                            sh "docker push ${dockerHub}:edge"
                        }
                    }
                }

                stage('deploy to rdpc-collab-dev') {
                    when {
                        branch "develop"
                    }
                    steps {
                        build(job: "/provision/update-app-version", parameters: [
                            [$class: 'StringParameterValue', name: 'RDPC_ENV', value: 'dev' ],
                            [$class: 'StringParameterValue', name: 'TARGET_RELEASE', value: 'relay-weblog'],
                            [$class: 'StringParameterValue', name: 'https://github.com/icgc-argo/rdpc-gateway/pull/74', value: "${version}-${commit}" ]
                        ])
                        build(job: "/provision/update-app-version", parameters: [
                            [$class: 'StringParameterValue', name: 'RDPC_ENV', value: 'dev' ],
                            [$class: 'StringParameterValue', name: 'TARGET_RELEASE', value: 'relay-splitter'],
                            [$class: 'StringParameterValue', name: 'NEW_APP_VERSION', value: "${version}-${commit}" ]
                        ])
                        build(job: "/provision/update-app-version", parameters: [
                            [$class: 'StringParameterValue', name: 'RDPC_ENV', value: 'dev' ],
                            [$class: 'StringParameterValue', name: 'TARGET_RELEASE', value: 'relay-index'],
                            [$class: 'StringParameterValue', name: 'NEW_APP_VERSION', value: "${version}-${commit}" ]
                        ])
                    }
                }

                stage('Release & Tag') {
                    when {
                        branch "master"
                    }
                    steps {
                        container('docker') {
                            withCredentials([usernamePassword(credentialsId: 'argoGithub', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                                sh "git tag ${version}"
                              sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${gitHubRepo} --tags"
                            }

                            withCredentials([usernamePassword(credentialsId:'argoContainers', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                                sh 'docker login ghcr.io -u $USERNAME -p $PASSWORD'
                            }

                            // DNS error if --network is default
                            sh "docker build --network=host . -t ${dockerHub}:latest -t ${dockerHub}:${version}"

                            sh "docker push ${dockerHub}:${version}"
                            sh "docker push ${dockerHub}:latest"
                        }
                    }
                }

                stage('deploy to rdpc-collab-qa') {
                    when {
                        branch "master"
                    }
                    steps {
                        build(job: "/provision/update-app-version", parameters: [
                            [$class: 'StringParameterValue', name: 'RDPC_ENV', value: 'qa' ],
                            [$class: 'StringParameterValue', name: 'TARGET_RELEASE', value: 'relay-weblog'],
                            [$class: 'StringParameterValue', name: 'NEW_APP_VERSION', value: "${version}" ]
                        ])
                        build(job: "/provision/helm", parameters: [
                            [$class: 'StringParameterValue', name: 'RDPC_ENV', value: 'qa' ],
                            [$class: 'StringParameterValue', name: 'TARGET_RELEASE', value: 'relay-splitter'],
                            [$class: 'StringParameterValue', name: 'NEW_APP_VERSION', value: "${version}" ]
                        ])
                        build(job: "/provision/helm", parameters: [
                            [$class: 'StringParameterValue', name: 'RDPC_ENV', value: 'qa' ],
                            [$class: 'StringParameterValue', name: 'TARGET_RELEASE', value: 'relay-index'],
                            [$class: 'StringParameterValue', name: 'NEW_APP_VERSION', value: "${version}" ]
                        ])
                    }
                }

            }
}
