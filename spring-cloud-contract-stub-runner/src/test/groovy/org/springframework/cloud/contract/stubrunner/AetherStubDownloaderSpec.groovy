package org.springframework.cloud.contract.stubrunner

import io.specto.hoverfly.junit.HoverflyRule
import org.eclipse.aether.RepositorySystemSession;
import org.junit.Rule
import org.springframework.util.ResourceUtils
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties


class AetherStubDownloaderSpec extends Specification {

    @Rule
    HoverflyRule hoverflyRule = HoverflyRule.buildFromClassPathResource("simulation.json").build()

    def 'Should be able to download from a repository using username and password authentication'() {
        given:
        StubRunnerOptions stubRunnerOptions = new StubRunnerOptionsBuilder()
            .withUsername("andrew.morgan")
            .withPassword("k+hbZp8rpolRucXB09dGE/CxPXxidQryQUYSGbeo6JE=")
            .withProxy("localhost", hoverflyRule.proxyPort)
            .withStubRepositoryRoot("https://test.jfrog.io/test/libs-snapshot-local")
            .build()

        AetherStubDownloader aetherStubDownloader = new AetherStubDownloader(stubRunnerOptions)

        when:
        def jar = aetherStubDownloader.downloadAndUnpackStubJar(new StubConfiguration("io.test", "test-simulations-svc", "1.0-SNAPSHOT"))

        then:
        jar != null
    }

    @RestoreSystemProperties
    def 'Should use local repository from settings.xml'() {
        given:
        File tempSettings = File.createTempFile("settings", ".xml")
        def m2repoFolder = 'm2repo' + File.separator + 'repository'
        tempSettings.text = '<settings><localRepository>' +
                ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + m2repoFolder).getAbsolutePath() + '</localRepository></settings>'
        System.setProperty("org.apache.maven.user-settings", tempSettings.getAbsolutePath())
        RepositorySystemSession repositorySystemSession =
                AetherFactories.newSession(AetherFactories.newRepositorySystem(), true);

        and:
        StubRunnerOptions stubRunnerOptions = new StubRunnerOptionsBuilder()
                .withWorkOffline(true)
                .build()
        AetherStubDownloader aetherStubDownloader = new AetherStubDownloader(stubRunnerOptions)

        when:
        def jar = aetherStubDownloader.downloadAndUnpackStubJar(
                new StubConfiguration("org.springframework.cloud.contract.verifier.stubs",
                        "bootService", "0.0.1-SNAPSHOT"))

        then:
        jar != null
        repositorySystemSession.getLocalRepository().getBasedir().getAbsolutePath().endsWith(m2repoFolder)
    }
}
