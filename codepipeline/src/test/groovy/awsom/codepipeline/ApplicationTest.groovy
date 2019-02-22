package awsom.codepipeline


import org.junit.Test

import static awsom.codepipeline.Application.*
import static java.util.UUID.randomUUID
import static org.assertj.core.api.Assertions.assertThat

class ApplicationTest {

    @Test
    void shouldCreatePipeline() {
        // Given
        def applicationName = randomUUID().toString()

        // When
        application().name(applicationName).create()

        // Then
        assertThat(findApplication(applicationName)).isNotNull()

        deleteApplication(applicationName)
    }

}
