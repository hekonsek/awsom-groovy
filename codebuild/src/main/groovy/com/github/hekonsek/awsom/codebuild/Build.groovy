package com.github.hekonsek.awsom.codebuild

import com.amazonaws.services.codebuild.AWSCodeBuildClient
import com.amazonaws.services.codebuild.model.CreateProjectRequest
import com.amazonaws.services.codebuild.model.DeleteProjectRequest
import com.amazonaws.services.codebuild.model.ProjectArtifacts
import com.amazonaws.services.codebuild.model.ProjectEnvironment
import com.amazonaws.services.codebuild.model.ProjectSource
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest

import static com.amazonaws.services.codebuild.model.EnvironmentType.LINUX_CONTAINER
import static com.amazonaws.services.codebuild.model.SourceType.GITHUB

class Build {

    String name

    String buildImage = 'aws/codebuild/java:openjdk-11'

    static Build build() {
        new Build()
    }

    Build create() {
        def project = new CreateProjectRequest()

        if (name == null) {
            throw new RuntimeException("Project name cannot be empty.")
        }
        project.name = name

        project.environment = new ProjectEnvironment().
                withType(LINUX_CONTAINER).
                withImage(buildImage)

        def iam = AmazonIdentityManagementClientBuilder.standard().build()

        def role = iam.listRoles().roles.find { it.roleName == 'awsom-codebuild-default' }
        if (role == null) {
            def policy = '''{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "codebuild.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
} '''
            role = iam.createRole(
                    new CreateRoleRequest().withRoleName('awsom-codebuild-default').withAssumeRolePolicyDocument(policy)
            ).role
            iam.attachRolePolicy(new AttachRolePolicyRequest().withRoleName('awsom-codebuild-default').
                    withPolicyArn('arn:aws:iam::aws:policy/CloudWatchLogsFullAccess'))
            iam.attachRolePolicy(new AttachRolePolicyRequest().withRoleName('awsom-codebuild-default').
                    withPolicyArn('arn:aws:iam::aws:policy/AmazonS3FullAccess'))
        }
        project.serviceRole = role.arn

        project.source = new ProjectSource().
                withType(GITHUB).
                withLocation('https://github.com/hekonsek/spring-boot-rest-prometheus-java11')
        project.artifacts = new ProjectArtifacts().
                withType("s3").
                withLocation('capsilon-hekonsek')
        AWSCodeBuildClient.builder().build().createProject(project)

        this
    }

    Build delete() {
        AWSCodeBuildClient.builder().build().deleteProject(new DeleteProjectRequest().withName(name))
        this
    }

    String name() {
        return name
    }

    Build name(String name) {
        this.name = name
        this
    }

    String buildImage() {
        return buildImage
    }

    Build buildImage(String buildImage) {
        this.buildImage = buildImage
        this
    }

    static void main(String[] args) {
        build().name("foo11").create()
    }

}