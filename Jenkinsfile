@Library(value='jenkins-pipeline-library@master', changelog=false) _
pipelineRDPCWorkflowRelay(
    buildImage: "openjdk:11",
    dockerRegistry: "ghcr.io",
    dockerRepo: "icgc-argo/workflow-relay",
    gitRepo: "icgc-argo/workflow-relay",
    testCommand: "./mvnw test --quiet"
)
