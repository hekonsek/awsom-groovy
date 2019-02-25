package awsom.codebuild

import com.amazonaws.services.codebuild.AWSCodeBuildClient
import com.amazonaws.services.codebuild.model.BatchGetProjectsRequest
import com.amazonaws.services.codebuild.model.CreateProjectRequest
import com.amazonaws.services.codebuild.model.DeleteProjectRequest
import com.amazonaws.services.codebuild.model.ListBuildsForProjectRequest
import com.amazonaws.services.codebuild.model.ListProjectsRequest
import com.amazonaws.services.codebuild.model.ProjectArtifacts
import com.amazonaws.services.codebuild.model.ProjectEnvironment
import com.amazonaws.services.codebuild.model.ProjectSource
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest

import static com.amazonaws.services.codebuild.model.EnvironmentType.LINUX_CONTAINER
import static com.amazonaws.services.codebuild.model.SourceType.GITHUB

class Build {

    private String name

    private String gitUrl

    private String buildSpec = 'buildspec.yml'

    private String buildImage = 'aws/codebuild/java:openjdk-11'

    static Build build() {
        new Build()
    }

    // CRUD

    Build create() {
        if (gitUrl == null) {
            throw new RuntimeException('Git URL of project cannot be empty.')
        }

        def project = new CreateProjectRequest()

        if (name == null) {
            throw new RuntimeException('Build project name cannot be empty.')
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
            iam.attachRolePolicy(new AttachRolePolicyRequest().withRoleName('awsom-codebuild-default').
                    withPolicyArn('arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryFullAccess'))
        }
        project.serviceRole = role.arn

        project.source = new ProjectSource().
                withType(GITHUB).
                withLocation(gitUrl).
                withBuildspec(buildSpec)
        project.artifacts = new ProjectArtifacts().
                withType("s3").
                withLocation('capsilon-hekonsek')
        AWSCodeBuildClient.builder().build().createProject(project)

        this
    }

    static void deleteBuild(String name) {
        AWSCodeBuildClient.builder().build().deleteProject(new DeleteProjectRequest().withName(name))
    }

    static Build findBuild(String name) {
        def projects = AWSCodeBuildClient.builder().build().batchGetProjects(new BatchGetProjectsRequest().withNames(name)).projects
        if (projects.empty) {
            return null
        }
        new Build().name(name).gitUrl(projects.first().source.location)
    }

    // Configuration getters and setters

    String name() {
        return name
    }

    Build name(String name) {
        this.name = name
        this
    }

    String gitUrl() {
        return gitUrl
    }

    Build gitUrl(String gitUrl) {
        this.gitUrl = gitUrl
        this
    }

    String buildSpec() {
        return buildSpec
    }

    Build buildSpec(String buildSpec) {
        this.buildSpec = buildSpec
        this
    }

    String buildImage() {
        return buildImage
    }

    Build buildImage(String buildImage) {
        this.buildImage = buildImage
        this
    }

}