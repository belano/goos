package com.belano.testcontainers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class OpenfireContainer extends GenericContainer<OpenfireContainer> {

    public static final Logger log = LoggerFactory.getLogger("Openfire");

    public OpenfireContainer() {
        super("goos/openfire:latest");
    }

    @Override
    protected void configure() {
        this.withLogConsumer(new Slf4jLogConsumer(log))
                .withExposedPorts(5222, 7777, 9090)
                .waitingFor(
                        Wait.forLogMessage(".*Finished processing all plugins.*\\n", 1)
                );
    }

    public void setup() {
        final Container.ExecResult setupResult;
        try {
            setupResult = execInContainer("/sbin/setup.sh");
            int exitCode = setupResult.getExitCode();
            String stdout = setupResult.getStdout();
            log.info("STDOUT {}", stdout);
            log.info("Exit code {}", exitCode);
            assertThat(exitCode, is(0));
        } catch (IOException | InterruptedException e) {
            fail(e);
        }
    }

}
