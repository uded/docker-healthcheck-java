package pl.radical.tools.healthcheck;

import com.github.jenspiegsa.mockitoextension.ConfigureWireMock;
import com.github.jenspiegsa.mockitoextension.InjectServer;
import com.github.jenspiegsa.mockitoextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:lukasz.rzanek@radical.com.pl">Łukasz Rżanek</a>
 * @since 11.05.2018
 */
@Slf4j
@ExtendWith(WireMockExtension.class)
class HealthCheckTest {

    @InjectServer
    private WireMockServer serverMock;

    @ConfigureWireMock
    private Options options = wireMockConfig()
            .dynamicPort()
            .notifier(new Slf4jNotifier(true));

    @DisplayName("Correct Spring health check")
    @Test
    void testGoodCheckHealth() {
        stubFor(get(urlPathEqualTo("/actuator/health"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/vnd.spring-boot.actuator.v2+json;charset=UTF-8")
                        .withStatus(200)
                        .withBody("{\"status\":\"UP\",\"details\":{\"diskSpace\":{\"status\":\"UP\",\"details\":{\"total\":397974155264,\"free\":314121715712,\"threshold\":10485760}},\"db\":{\"status\":\"UP\",\"details\":{\"database\":\"PostgreSQL\",\"hello\":1}}}}"))
        );

        log.info(">>>>> Sending test request");
        assertTrue(HealthCheck.checkStatus("http://localhost:" + serverMock.port() + "/actuator/health"));

        log.info(">>>>> Test finished!");
    }

    @DisplayName("Bad Spring health check")
    @Test
    void testBadCheckHealth() {
        stubFor(get(urlPathEqualTo("/actuator/health"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/vnd.spring-boot.actuator.v2+json;charset=UTF-8")
                        .withStatus(200)
                        .withBody("{\"status\":\"DOWN\",\"details\":{\"diskSpace\":{\"status\":\"UP\",\"details\":{\"total\":397974155264,\"free\":314121715712,\"threshold\":10485760}},\"db\":{\"status\":\"DOWN\",\"details\":{\"database\":\"PostgreSQL\",\"hello\":1}}}}"))
        );

        log.info(">>>>> Sending test request");
        assertFalse(HealthCheck.checkStatus("http://localhost:" + serverMock.port() + "/actuator/health"));

        log.info(">>>>> Test finished!");
    }

    @DisplayName("Bad URL")
    @Test
    void testBadConnection() {
        log.info(">>>>> Sending test request");
        assertFalse(HealthCheck.checkStatus("http://localhost/testShouldNotExists"));
        log.info(">>>>> Test finished!");
    }
}