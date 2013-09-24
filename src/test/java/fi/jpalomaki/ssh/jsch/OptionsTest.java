package fi.jpalomaki.ssh.jsch;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import fi.jpalomaki.ssh.jsch.JschSshClient.Options;

public final class OptionsTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullConnectTimeout() {
        new Options(null, "0s", "1K", "1K", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMalformedConnectTimeout() {
        new Options("-5", "0s", "1K", "1K", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMalformedConnectTimeout2() {
        new Options("ad", "0s", "1K", "1K", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMalformedSessionTimeout() {
        new Options("ad", "a", "1K", "1K", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeConnectTimeout() {
        new Options("-5s", "0s", "1K", "1K", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testTooLongConnectTimeout() {
        new Options("25d", "0s", "1K", "1K", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullMaxStdoutBytes() {
        new Options("0s", "0s", null, "1K", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMalformedMaxStdoutBytes() {
        new Options("0s", "0s", "K", "1K", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMalformedMaxStdoutBytes2() {
        new Options("0s", "0s", "1 K", "1K", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMaxStdoutBytes() {
        new Options("0s", "0s", "-1K", "1K", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMaxStdoutBytesOverMaxValue() {
        new Options("0s", "0s", "3G", "1K", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMaxStderrBytesOverMaxValue() {
        new Options("1s", "5s", "1G", "2147483648B", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMalformedSshConfigString() {
        new Options("1s", "5s", "1G", "1G", "a;b");
    }
    
    @Test
    public void testDefaults() {
        Options defaults = new Options();
        assertEquals(5000L, defaults.connectTimeout);
        assertEquals(0L, defaults.sessionTimeout);
        assertEquals(1024L * 1024, defaults.maxStdoutBytes);
        assertEquals(1024L * 1024, defaults.maxStderrBytes);
        assertEquals(Collections.singletonMap("StrictHostKeyChecking", "yes"), defaults.sshConfig);
    }
    
    @Test
    public void testCustomSshConfig() {
        Options options = new Options("5m", "1d", "128B", "1K", "CompressionLevel=1;TCPKeepAlive=no");
        assertEquals(1000L * 60 * 5, options.connectTimeout);
        assertEquals(1000L * 60 * 60 * 24, options.sessionTimeout);
        assertEquals(128L, options.maxStdoutBytes);
        assertEquals(1024L, options.maxStderrBytes);
        Map<String, String> expected = new HashMap<String, String>(2);
        expected.put("CompressionLevel", "1");
        expected.put("TCPKeepAlive", "no");
        assertEquals(expected, options.sshConfig);
    }
}
