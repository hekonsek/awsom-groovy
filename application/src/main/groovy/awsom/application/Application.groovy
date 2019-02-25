package awsom.application

import awsom.codepipeline.Pipeline
import awsom.codebuild.Build

import static awsom.codebuild.Build.deleteBuild
import static awsom.codepipeline.Pipeline.findPipeline

class Application {

    private String name

    private String gitUrl

    static Application application() {
        new Application()
    }

    Application create() {
        def build = new Build().name(name).gitUrl(gitUrl).create()
        new Build().name("${name}-dockerize").
                gitUrl(gitUrl).buildSpec('buildspec-docker.yml').
                buildImage('aws/codebuild/docker:18.09.0').create()
        new Pipeline().name(name).gitUrl(gitUrl).buildName(build.name()).create()
        this
    }

    static deleteApplication(String name) {
        deleteBuild(name)
        deleteBuild("${name}-dockerize")
        new Pipeline().name(name).delete()
        this
    }

    static Application findApplication(String name) {
        findPipeline(name) != null ? new Application().name(name) : null
    }

    // Configuration getters and setters

    String name() {
        return name
    }

    Application name(String name) {
        this.name = name
        this
    }

    String gitUrl() {
        return gitUrl
    }

    Application gitUrl(String gitUrl) {
        this.gitUrl = gitUrl
        this
    }

}