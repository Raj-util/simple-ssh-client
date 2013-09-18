package fi.jpalomaki.ssh.jsch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import fi.jpalomaki.ssh.SshClient;
import fi.jpalomaki.ssh.SshClientException;
import fi.jpalomaki.ssh.UserAtHost;
import fi.jpalomaki.ssh.Result;
import fi.jpalomaki.ssh.util.Assert;

/**
 * Jsch-based {@link SshClient} implementation.
 * 
 * Only publickey authentication is supported.
 * 
 * @author jpalomaki
 */
public final class JschSshClient implements SshClient {
    
    private static final long CHANNEL_CLOSED_POLL_INTERVAL = 100L;
    
    private final String privateKey;
    private final byte[] passphrase;
    private final String knownHosts;
    private final Options options;
    
    /**
     * Constructs a new {@link JschSshClient} with a default known hosts
     * file (<code>~/.ssh/known_hosts</code>) and default {@link Options}.
     * 
     * Note that strict host key checking is used by default, which means
     * that a valid host key must be present in the SSH known hosts file.
     */
    public JschSshClient(String privateKey, String passphrase) {
        this(privateKey, passphrase, "~/.ssh/known_hosts", new Options());
    }
    
    /**
     * Constructs a new {@link JschSshClient} with the given parameters.
     * 
     * @param privateKey Path to private key file, not <code>null</code> or empty
     * @param passphrase Private key passphrase, may be <code>null</code> for no passphrase
     * @param knownHosts Path to known hosts file, not <code>null</code> or empty
     * @param options Set of SSH client options, not <code>null</code>
     */
    public JschSshClient(String privateKey, String passphrase, String knownHosts, Options options) {
        Assert.hasText(privateKey, "Path to private key file must not be null");
        Assert.hasText(knownHosts, "Path to known hosts file must not be null");
        Assert.notNull(options, "Options must not be null");
        this.privateKey = privateKey;
        this.passphrase = passphrase != null ? passphrase.getBytes() : null;
        this.knownHosts = knownHosts;
        this.options = options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Result executeCommand(String command, UserAtHost userAtHost) throws SshClientException {
        return executeCommand(command, ByteBuffer.wrap(new byte[0]), userAtHost);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Result executeCommand(String command, ByteBuffer stdin, UserAtHost userAtHost) throws SshClientException {
        Assert.hasText(command, "Command must not be null or empty");
        Assert.notNull(stdin, "Standard in must not be null (but may be empty)");
        Assert.notNull(userAtHost, "User at host must not be null");
        Session session = null;
        try {
            session = newSessionFor(userAtHost);
            return doExecuteCommand(command, stdin.array(), session);
        } catch (JSchException e) {
            throw new SshClientException("Failed to execute command '" + command + "' on " + userAtHost, e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private Session newSessionFor(UserAtHost userAtHost) throws JSchException {
        JSch jsch = new JSch();
        jsch.setKnownHosts(knownHosts);
        jsch.addIdentity(privateKey, passphrase);
        Session session = jsch.getSession(userAtHost.user, userAtHost.host, userAtHost.port);
        for (Map.Entry<String, String> entry : options.sshConfig.entrySet()) {
            session.setConfig(entry.getKey(), entry.getValue());
        }
        session.setConfig("PreferredAuthentications", "publickey");
        session.connect(options.connectTimeout);
        return session;
    }
    
    private Result doExecuteCommand(String command, byte[] bytesToStdin, Session session) throws JSchException, SshClientException {
        ByteArrayInputStream stdin = new ByteArrayInputStream(bytesToStdin);
        ByteArrayOutputStream stdout = new ByteArrayOutputStream(options.maxStdoutBytes);
        ByteArrayOutputStream stderr = new ByteArrayOutputStream(options.maxStderrBytes);
        ChannelExec executionChannel = (ChannelExec)session.openChannel("exec");
        executionChannel.setCommand(command);
        if (stdin.available() > 0) {
            executionChannel.setInputStream(stdin);
        }
        executionChannel.setOutputStream(stdout);
        executionChannel.setErrStream(stderr);
        executionChannel.connect();
        waitUntilChannelClosed(executionChannel);
        return new Result(executionChannel.getExitStatus(), stdout.toByteArray(), stderr.toByteArray());
    }
    
    private void waitUntilChannelClosed(ChannelExec executionChannel) {
        long waitTimeThusFar = 0L;
        long sessionTimeout = options.sessionTimeout;
        do {
            try {
                Thread.sleep(CHANNEL_CLOSED_POLL_INTERVAL);
                waitTimeThusFar += CHANNEL_CLOSED_POLL_INTERVAL;
                if (sessionTimeout > 0L && waitTimeThusFar > sessionTimeout) {
                    break;
                }
            } catch (InterruptedException e) {
                // Ignore
            }
        } while (!executionChannel.isClosed());
        if (!executionChannel.isClosed()) {
            executionChannel.disconnect();
            throw new SshClientException("Session timeout (" + sessionTimeout + " ms) exceeded");
        }
    }
    
    /**
     * SSH client options (immutable).
     */
    public static class Options {
        
        /**
         * Connect timeout in milliseconds. Defaults to 5 seconds. A value of 0 designates no timeout.
         */
        public final int connectTimeout;
        
        /**
         * Session timeout in milliseconds. Defaults to 0, i.e. no timeout.
         * 
         * Note: This is a hard timeout to limit the duration of the SSH session
         * and it is enforced regardless of whether the session is idle or not.
         */
        public final int sessionTimeout;
        
        /**
         * Maximum stdout buffer size in bytes. Defaults to 1 MiB.
         */
        public final int maxStdoutBytes;
        
        /**
         * Maximum stderr buffer size in bytes. Defaults to 1 MiB.
         */
        public final int maxStderrBytes;
        
        /**
         * Map of SSH configuration options. See ssh_config(5).
         */
        public final Map<String, String> sshConfig;

        /**
         * Constructs default options.
         */
        public Options() {
            this(5000, 0, 1024 * 1024, 1024 * 1024, Collections.singletonMap("StrictHostKeyChecking", "yes"));
        }
        
        /**
         * Constructs new {@link Options} with the given parameters.
         * 
         * @param connectTimeout Connect timeout (in ms) >= 0, 0 for no timeout
         * @param sessionTimeout Session timeout (in ms) >= 0, 0 for no timeout
         * @param maxStdoutBytes Maximum buffer size for stdout (in bytes) >= 0
         * @param maxStderrBytes Maximum buffer size for stderr (in bytes) >= 0
         * @param sshConfig SSH configuration options, may be <code>null</code>
         */
        public Options(int connectTimeout, int sessionTimeout, int maxStdoutBytes, int maxStderrBytes, Map<String, String> sshConfig) {
            Assert.isTrue(connectTimeout >= 0, "Connect timeout must be >= 0 ms");
            Assert.isTrue(sessionTimeout >= 0, "Session timeout must be >= 0 ms");
            Assert.isTrue(maxStdoutBytes >= 0, "Max stdout buffer size must be >= 0 bytes");
            Assert.isTrue(maxStderrBytes >= 0, "Max stderr buffer size must be >= 0 bytes");
            this.connectTimeout = connectTimeout;
            this.sessionTimeout = sessionTimeout;
            this.maxStdoutBytes = maxStdoutBytes;
            this.maxStderrBytes = maxStderrBytes;
            this.sshConfig = sshConfig != null ? 
                    Collections.unmodifiableMap(sshConfig) : Collections.<String, String>emptyMap();
        }  
    }
}
