pipeline {
    agent {
        kubernetes {
            yaml """
kind: Pod
spec:
  containers:
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug-539ddefcae3fd6b411a95982a830d987f4214251
    imagePullPolicy: Always
    command:
    - /busybox/cat
    tty: true
    volumeMounts:
      - name: kaniko-secret
        mountPath: /kaniko/.docker
  - name: helm
    image: alpine/helm:3.2.0
    imagePullPolicy: Always
    command: ["cat"]
    tty: true
  volumes:
  - name: kaniko-secret
    secret:
      secretName: kaniko-secret
"""

        }

    }
    stages {
        stage('build and push') {
            steps {
                container('kaniko'){
                    git 'https://github.com/NikolayMarusenko/example-dockerfile-python.git'
                    sh 'ls -la'
                    sh 'pwd'
                    sh "/kaniko/executor --dockerfile `pwd`/Dockerfile --context `pwd` --destination=1545662258668/python-app:latest --destination=1545662258668/python-app:${BUILD_NUMBER}"
                }
            }
        }
        stage('helm deploy'){
            steps{
                container('helm'){
                    sh "helm upgrade example-dockerfile-python helm/ --install --debug --namespace deploy --set registry=1545662258668 --set replicas=1"
                    sh "helm ls -n deploy"
                }
            }
        }
    }

}