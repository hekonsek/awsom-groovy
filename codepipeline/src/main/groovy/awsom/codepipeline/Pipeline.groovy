package awsom.codepipeline

import com.amazonaws.services.codebuild.AWSCodeBuildClient
import com.amazonaws.services.codebuild.model.DeleteProjectRequest
import com.amazonaws.services.codepipeline.AWSCodePipelineClient
import com.amazonaws.services.codepipeline.model.*
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest
import com.google.common.collect.ImmutableMap

import java.nio.channels.Pipe

class Pipeline {

    private String name

    private String buildName

    static pipeline() {
        new Pipeline()
    }

    Pipeline create() {
        def iam = AmazonIdentityManagementClientBuilder.standard().build()
        def role = iam.listRoles().roles.find { it.roleName == 'awsom-codepipeline-default' }
        if (role == null) {
            def policy = '''{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "codepipeline.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
} '''
            role = iam.createRole(
                    new CreateRoleRequest().withRoleName('awsom-codepipeline-default').withAssumeRolePolicyDocument(policy)
            ).role
            iam.attachRolePolicy(new AttachRolePolicyRequest().withRoleName('awsom-codepipeline-default').
                    withPolicyArn('arn:aws:iam::aws:policy/AmazonS3FullAccess'))
            iam.attachRolePolicy(new AttachRolePolicyRequest().withRoleName('awsom-codepipeline-default').
                    withPolicyArn('arn:aws:iam::aws:policy/AWSCodeBuildDeveloperAccess'))
        }

        new AWSCodePipelineClient().createPipeline(new CreatePipelineRequest().withPipeline(
                new PipelineDeclaration().withName(name).withRoleArn(role.arn).withStages(
                        new StageDeclaration().withName("source").withActions(
                                new ActionDeclaration().withName('source').withActionTypeId(
                                        new ActionTypeId().withOwner(ActionOwner.ThirdParty).
                                                withProvider('GitHub').withCategory('Source').
                                                withVersion("1")
                                ).withConfiguration(ImmutableMap.of("Owner", "hekonsek", "Repo", "spring-boot-rest-prometheus-java11",
                                "Branch", "master", 'OAuthToken', System.getenv('GITHUB_TOKEN'))).
                                withOutputArtifacts(new OutputArtifact().withName('xxx'))
                        ),
                        new StageDeclaration().withName("build").withActions(
                                new ActionDeclaration().withName('build').withActionTypeId(
                                        new ActionTypeId().withOwner(ActionOwner.AWS).
                                                withProvider('CodeBuild').withCategory('Build').
                                                withVersion("1")
                                ).withConfiguration(ImmutableMap.of('ProjectName', buildName)).withInputArtifacts(new InputArtifact().withName('xxx'))
                        )
                ).withArtifactStore(new ArtifactStore().withType('S3').withLocation('capsilon-hekonsek'))
        ))

        this
    }

    Pipeline delete() {
        AWSCodePipelineClient.builder().build().deletePipeline(new DeletePipelineRequest().withName(name))
        this
    }

    static Pipeline findPipeline(String name) {
        def pipeline = AWSCodePipelineClient.builder().build().listPipelines(new ListPipelinesRequest()).pipelines.find{ it.name == name }
        pipeline == null ? null : new Pipeline().name(name)
    }

    String name() {
        return name
    }

    Pipeline name(String name) {
        this.name = name
        this
    }

    String buildName() {
        return buildName
    }

    Pipeline buildName(String buildName) {
        this.buildName = buildName
        this
    }

}