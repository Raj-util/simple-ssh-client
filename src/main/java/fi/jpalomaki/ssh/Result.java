package fi.jpalomaki.ssh;

import java.nio.ByteBuffer;
import java.io.UnsupportedEncodingException;

/**
 * Abstraction for an SSH command result.
 * 
 * @author jpalomaki
 */
public final class Result {
    
    /**
     * Integer exit code.
     */
    public final int exitCode;
    
    /**
     * Captured standard output.
     */
    public final ByteBuffer stdout;
    
    /**
     * Captured standard error.
     */
    public final ByteBuffer stderr;
    
    /**
     * Constructs a new {@link Result}.
     * 
     * @param exitCode Integer exit (return) code
     * @param stdout Standard output, not <code>null</code>
     * @param stderr Standard error, not <code>null</code>
     */
    public Result(int exitCode, byte[] stdout, byte[] stderr) {
        this.exitCode = exitCode;
        this.stdout = ByteBuffer.wrap(stdout);
        this.stderr = ByteBuffer.wrap(stderr);
    }

    /**
     * Constructs a new {@link Result}, converting the given stdout
     * and stderr strings into bytes using platform-default charset.
     *
     * @param exitCode Integer exit (return) code
     * @param stdout Standard output, not <code>null</code>
     * @param stderr Standard error, not <code>null</code>
     */
    public Result(int exitCode, String stdout, String stderr) {
        this(exitCode, stdout.getBytes(), stderr.getBytes());
    }

    /**
     * Returns stdout as text, using default charset (UTF-8). Never <code>null</code>.
     */
    public String stdoutAsText() {
        return stdoutAsText("UTF-8");
    }
    
    /**
     * Returns stderr as text, using default charset (UTF-8). Never <code>null</code>.
     */
    public String stderrAsText() {
        return stderrAsText("UTF-8");
    }
    
    /**
     * Returns stdout as text, decoded using the given charset. Never <code>null</code>.
     */
    public String stdoutAsText(String charset) {
        return toString(stdout, charset);
    }
    
    /**
     * Returns stderr as text, decoded using the given charset. Never <code>null</code>.
     */
    public String stderrAsText(String charset) {
        return toString(stderr, charset);
    }
    
    private String toString(ByteBuffer buffer, String charset) {
        try {
            return new String(buffer.array(), charset);
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedUnsupportedEncodingException(e);
        }
    }

    @Override
    public String toString() {
        int stdoutSizeInBytes = stdout.array().length;
        int stderrSizeInBytes = stderr.array().length;
        String hashCode = Integer.toHexString(hashCode());
        String instance = getClass().getSimpleName() + "@" + hashCode; 
        return String.format("%s [exitCode=%s,stdoutSizeInBytes=%s,stderrSizeInBytes=%s]", 
                instance, exitCode, stdoutSizeInBytes, stderrSizeInBytes);
    }
}

/**
 * Unchecked replacement for {@link UnsupportedEncodingException}.
 */
final class UncheckedUnsupportedEncodingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UncheckedUnsupportedEncodingException(UnsupportedEncodingException cause) {
        super(cause);
    }
}
