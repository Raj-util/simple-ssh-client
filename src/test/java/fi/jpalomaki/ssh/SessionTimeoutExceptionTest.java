package fi.jpalomaki.ssh;

import org.junit.Test;

public final class SessionTimeoutExceptionTest {

    @Test(expected = IllegalArgumentException.class)
    public void test() {
        System.out.println(new SessionTimeoutException(0));
        System.out.println(new SessionTimeoutException(12));
        System.out.println(new SessionTimeoutException(999));
        System.out.println(new SessionTimeoutException(1000));
        System.out.println(new SessionTimeoutException(1001));
        System.out.println(new SessionTimeoutException(1001));
        System.out.println(new SessionTimeoutException(6000));
        System.out.println(new SessionTimeoutException(60000));
        System.out.println(new SessionTimeoutException(90000));
        System.out.println(new SessionTimeoutException(1000 * 60 * 60 * 24));
        System.out.println(new SessionTimeoutException(1000 * 60 * 60 * 24 + 1));
        System.out.println(new SessionTimeoutException(1000 * 60 * 60 + 36000));
        System.out.println(new SessionTimeoutException(-1));
    }
}
