package awsom.codepipeline


import com.github.hekonsek.awsom.codebuild.Build

import static awsom.codepipeline.Pipeline.findPipeline

class Application {

    private String name

    static Application application() {
        new Application()
    }

    Application create() {
        def build = new Build().name(name).create()
        new Pipeline().name(name).buildName(build.name()).create()
        this
    }

    static deleteApplication(String name) {
        new Build().name(name).delete()
        new Pipeline().name(name).delete()
        this
    }

    static Application findApplication(String name) {
        findPipeline(name) != null ? new Application().name(name) : null
    }

    String name() {
        return name
    }

    Application name(String name) {
        this.name = name
        this
    }

}