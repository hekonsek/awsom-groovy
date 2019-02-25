package awsom.codebuild

import org.junit.Test

import static awsom.codebuild.Build.build
import static awsom.codebuild.Build.deleteBuild
import static awsom.codebuild.Build.findBuild
import static java.util.UUID.randomUUID
import static org.assertj.core.api.Assertions.assertThat

class BuildTest {

    @Test
    void shouldCreateBuild() {
        // Given
        def buildName = randomUUID().toString()
        def gitUrl = 'https://github.com/hekonsek/awsom-spring-rest.git'

        // When
        build().name(buildName).gitUrl(gitUrl).create()

        // Then
        assertThat(findBuild(buildName)).isNotNull()

        deleteBuild(buildName)
    }

}
