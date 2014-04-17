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
 * Mocked {@link SshClient} for testing purposes.
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
        LOGGER.debug("Executing command '" + command + "' on " + userAtHost +
                " (stdin = " + (stdin != null ? stdin.array().length : 0) + " bytes)");
        if (configuration.reportConnectionFailure) {
            throw new SshClientException("Fake connection failure", new SocketException("Failed to establish bogus socket"));
        }
        sleepFor(configuration.commandDurationSeconds);
        return new Result(configuration.exitCode, configuration.stdout, configuration.stderr);
    }

    private void sleepFor(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    public static class Configuration {

        private int exitCode = 0;
        private String stdout = "";
        private String stderr = "";
        private int commandDurationSeconds = 5;
        private boolean reportConnectionFailure = false;

        public int getExitCode() {
            return exitCode;
        }

        public void setExitCode(int exitCode) {
            this.exitCode = exitCode;
        }

        public String getStdout() {
            return stdout;
        }

        public void setStdout(String stdout) {
            Assert.notNull(stdout, "Stdout must not be null (but may be empty)");
            this.stdout = stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public void setStderr(String stderr) {
            Assert.notNull(stderr, "Stderr must not be null (but may be empty)");
            this.stderr = stderr;
        }

        public int getCommandDurationSeconds() {
            return commandDurationSeconds;
        }

        public void setCommandDurationSeconds(int commandDurationSeconds) {
            Assert.isTrue(commandDurationSeconds > 0, "Command duration must be > 0 seconds");
            this.commandDurationSeconds = commandDurationSeconds;
        }

        public boolean isReportConnectionFailure() {
            return reportConnectionFailure;
        }

        public void setReportConnectionFailure(boolean reportConnectionFailure) {
            this.reportConnectionFailure = reportConnectionFailure;
        }
    }
}
