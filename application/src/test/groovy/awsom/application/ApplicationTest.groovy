package awsom.application

import org.junit.Test

import static awsom.application.Application.*
import static java.util.UUID.randomUUID
import static org.assertj.core.api.Assertions.assertThat

class ApplicationTest {

    @Test
    void shouldCreatePipeline() {
        // Given
        def applicationName = randomUUID().toString()
        def gitUrl = 'https://github.com/hekonsek/awsom-spring-rest.git'

        // When
        application().name(applicationName).gitUrl(gitUrl).create()

        // Then
        assertThat(findApplication(applicationName)).isNotNull()

        deleteApplication(applicationName)
    }

}
