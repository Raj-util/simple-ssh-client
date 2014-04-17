package fi.jpalomaki.ssh.jsch;

import fi.jpalomaki.ssh.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

import fi.jpalomaki.ssh.jsch.JschSshClient.Options;

/**
 * Tests for {@link JschSshClient}. Tests assume user "test" is available on the local host,
 * and that appropriate SSH public keys have been appended to /home/test/.ssh/authorized_keys.
 */
public final class JschSshClientTest {
    
    private final UserAtHost userAtHost = new UserAtHost("test", "localhost");
    
    @Test(expected = SshClientException.class)
    public void testNonExistentPrivateKeyFile() {
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_testos", "ankka");
        sshClient.executeCommand("whoami", userAtHost);
    }
    
    @Test(expected = SshClientException.class)
    public void testIncorrectPrivateKeyPassphrase() {
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka2");
        sshClient.executeCommand("whoami", userAtHost);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullCommand() {
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka");
        sshClient.executeCommand(null, userAtHost);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testEmptyCommand() {
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka");
        sshClient.executeCommand(" ", userAtHost);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullUserAtHost() {
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka");
        sshClient.executeCommand("whoami", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullStdin() {
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka");
        sshClient.executeCommand("cat -", null, userAtHost);
    }
    
    @Test
    public void testAutoCreatedKnownHostsFile() throws IOException {
        Options options = new Options("0s", "0s", "64B", "64B", "StrictHostKeyChecking=no", false);
        File knownHosts = File.createTempFile("testAutoCreatedKnownHostsFile", "known_hosts", new File("/tmp"));
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test_nopass", null, knownHosts.getAbsolutePath(), options);
        Result result = sshClient.executeCommand("sleep 1s; whoami", userAtHost);
        assertEquals(0, result.exitCode);
    }
    
    @Test
    public void testLsAtSlashTmp() {
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka");
        Result result = sshClient.executeCommand("cd /tmp; ls -la", userAtHost);
        assertEquals(0, result.exitCode);
        assertFalse(result.stdoutAsText().isEmpty());
        assertTrue(result.stderrAsText().isEmpty());
        System.out.println(result.stdoutAsText());
    }
    
    @Test
    public void testLsAtSlashTmpNoPassphrase() {
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test_nopass", null);
        Result result = sshClient.executeCommand("cd /tmp; ls -la", userAtHost);
        assertEquals(0, result.exitCode);
        assertFalse(result.stdoutAsText().isEmpty());
        assertTrue(result.stderrAsText().isEmpty());
        System.out.println(result.stdoutAsText());
    }
    
    @Test
    public void testNonZeroExitCodeWithStderr() {
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test_nopass", null);
        Result result = sshClient.executeCommand("cat /tmp2/no_such_file_for_sure", userAtHost);
        assertTrue(result.exitCode != 0);
        assertTrue(result.stdoutAsText().isEmpty());
        assertFalse(result.stderrAsText().isEmpty());
        System.out.println(result.stderrAsText());
    }
    
    @Test
    public void testKnownHostsAtDevNullNoStrictHostKeyChecking() {
        Options options = new Options("0s", "0s", "64B", "64B", "StrictHostKeyChecking=no", false);
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test_nopass", null, "/dev/null", options);
        Result result = sshClient.executeCommand("whoami", userAtHost);
        assertEquals(0, result.exitCode);
        assertEquals("test", result.stdoutAsText().trim());
    }
    
    @Test
    public void testCatStdin() {
        ByteBuffer stdin = ByteBuffer.wrap("secretÄ".getBytes());
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka");
        Result result = sshClient.executeCommand("cat -", stdin, userAtHost);
        assertEquals(0, result.exitCode);
        assertEquals("secretÄ", result.stdoutAsText().trim());
    }
    
    @Test(expected = SshClientException.class, timeout = 5000)
    public void testConnectTimeout() {
        Options options = new Options("4500ms", "0s", "64B", "64B", "StrictHostKeyChecking=no", false);
        UserAtHost atUnreachableHost = new UserAtHost("test", "1.2.3.4");
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka", "/dev/null", options);
        sshClient.executeCommand("whoami", atUnreachableHost);
    }
    
    @Test(expected = SessionTimeoutException.class, timeout = 3000)
    public void testSessionTimeout() {
        Options options = new Options("0s", "2500ms", "64B", "64B", "StrictHostKeyChecking=no", false);
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka", "/dev/null", options);
        sshClient.executeCommand("sleep 5s", userAtHost);
    }
    
    @Test
    public void testStdoutOverflow() {
        Options options = new Options("0s", "0s", "2B", "1M", "StrictHostKeyChecking=no", false);
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka", "/dev/null", options);
        Result result = sshClient.executeCommand("echo mygodthisisalongstringindeed", userAtHost);
        System.out.println(result.stdoutAsText());
    }
    
    @Test
    public void testUname() {
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka");
        Result result = sshClient.executeCommand("uname -a", userAtHost);
        String uname = result.stdoutAsText();
        System.out.println(uname);
    }
    
    @Test
    public void testNonStandardPort() {
        ByteBuffer stdin = ByteBuffer.wrap("secret".getBytes());
        UserAtHost userAtHost = new UserAtHost("test", "localhost", 2020);
        Options options = new Options("0s", "0s", "64K", "64K", "StrictHostKeyChecking=no", false);
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka", "/dev/null", options);
        Result result = sshClient.executeCommand("cat - > secret.txt; cat secret.txt", stdin, userAtHost);
        assertEquals("secret", result.stdoutAsText());
    }
    
    @Test
    public void testSudoFailsWithoutPty() {
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka");
        Result result = sshClient.executeCommand("sudo ls /root", userAtHost);
        assertTrue(result.exitCode != 0);
    }
    
    @Test
    public void testSudoWithPty() {
        ByteBuffer sudoPassword = ByteBuffer.wrap("lampola1\n".getBytes());
        Options options = new Options("0s", "5s", "1K", "1K", "StrictHostKeyChecking=no", true);
        SshClient sshClient = new JschSshClient("src/test/resources/id_rsa_test", "ankka", "/dev/null", options);
        Result result = sshClient.executeCommand("sudo -S ls -la /root", sudoPassword, userAtHost);
        assertEquals(0, result.exitCode);
        assertTrue(!result.stdoutAsText().isEmpty());
        System.out.println(result.stdoutAsText());
    }
}
