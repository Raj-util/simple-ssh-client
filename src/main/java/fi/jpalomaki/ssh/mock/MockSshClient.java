package fi.jpalomaki.ssh.mock;

import fi.jpalomaki.ssh.Result;
import fi.jpalomaki.ssh.SshClient;
import fi.jpalomaki.ssh.SshClientException;
import fi.jpalomaki.ssh.UserAtHost;
import fi.jpalomaki.ssh.util.Assert;
import org.slf4j.LoggerFactory;
import java.net.SocketException;
import java.nio.ByteBuffer;
import org.slf4j.Logger;

/**
 * Mocked SshClient for testing purposes.
 *
 * @author jpalomaki
 */
public final class MockSshClient implements SshClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockSshClient.class);

    private Configuration configuration = new Configuration();

    public void setConfiguration(Configuration configuration) {
        Assert.notNull(configuration, "Configuration must not be null");
        this.configuration = configuration;
    }

    @Override
    public Result executeCommand(String command, UserAtHost userAtHost) throws SshClientException {
         return executeCommand(command, null, userAtHost);
    }

    @Override
    public Result executeCommand(String command, ByteBuffer stdin, UserAtHost userAtHost) throws SshClientException {
        if (configuration.reportConnectionFailure) {
            throw new SshClientException("Connection failure", new SocketException("Failed to establish bogus socket"));
        }
        LOGGER.debug("Executing command '" + command + "' on " + userAtHost +
                " (stdin = " + (stdin != null ? stdin.array().length : 0) + " bytes)");
        sleepFor(configuration.commandDurationSeconds);
        return new Result(configuration.exitCode, configuration.stdout, configuration.stderr);
    }

    private void sleepFor(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
        }
    }

    public static class Configuration {

        private int exitCode = 0;
        private String stdout = "";
        private String stderr = "";
        private int commandDurationSeconds = 5;
        private boolean reportConnectionFailure = false;

        public void setExitCode(int exitCode) {
            this.exitCode = exitCode;
        }

        public void setStdout(String stdout) {
            Assert.notNull(stdout, "Stdout must not be null");
            this.stdout = stdout;
        }

        public void setStderr(String stderr) {
            Assert.notNull(stderr, "Stderr must not be null");
            this.stderr = stderr;
        }

        public void setCommandDurationSeconds(int commandDurationSeconds) {
            Assert.isTrue(commandDurationSeconds > 0, "Command duration must be > 0");
            this.commandDurationSeconds = commandDurationSeconds;
        }

        public void setReportConnectionFailure(boolean reportConnectionFailure) {
            this.reportConnectionFailure = reportConnectionFailure;
        }
    }
}
