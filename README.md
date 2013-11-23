# Simple SSH client for Java

This library is minimal by design, with minimal dependencies. Client features:

 * Thread-safe (reusable)
 * Configurable (e.g. session timeout)
 * Only public key authentication is supported
 * Pseudo-terminal (pty) allocation (for sudo)
 * Stdin may be passed in to commands

For more advanced SSH usage, I recommend [Ansible](https://github.com/ansible/ansible). You can
use this Java library to invoke Ansible remotely, and have Ansible (playbooks) do the real work.

Utilizes the [Jsch](http://www.jcraft.com/jsch) SSH 2 library.

## Maven dependency

Java snapshot binaries available in the [Sonatype OSS repository](https://oss.sonatype.org/content/groups/public).

    <repositories>
        <repository>
            <id>sonatype-oss</id>
            <url>https://oss.sonatype.org/content/groups/public</url>
        </repository>
    </repositories>
    ...
    <dependencies>
        <dependency>
            <groupId>fi.jpalomaki.ssh</groupId>
            <artifactId>simple-ssh-client</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

## Simple usage

    // Think uname=$(ssh root@example.com uname -a)
    UserAtHost userAtHost = new UserAtHost("root", "example.com");
    SshClient sshClient = new JschSshClient("~/.ssh/id_rsa", "passphrase");
    Result result = sshClient.executeCommand("uname -a", userAtHost);
    String uname = result.stdoutAsText();

## Passing in stdin and using a non-standard port

    // Think echo secret | ssh -i path/to/ssh/private_key -p 2020 user@example.com "cat - > secret.txt"
    ByteBuffer stdin = ByteBuffer.wrap("secret".getBytes());
    UserAtHost userAtHost = new UserAtHost("user", "example.com", 2020);
    SshClient sshClient = new JschSshClient("path/to/ssh/private_key", "passphrase");
    sshClient.executeCommand("cat - > secret.txt", stdin, userAtHost);

## Usage with empty passphrase, a custom known hosts file and SSH client options

    // Think ssh -o "StrictHostKeyChecking=no UserKnownHostsFile=/dev/null" root@example.com sleep 5s
    Options options = new Options("2s", "30m", "64K", "64K", "StrictHostKeyChecking=no", false);
    UserAtHost userAtHost = new UserAtHost("root", "example.com");
    SshClient sshClient = new JschSshClient("~/.ssh/id_rsa", null, "/dev/null", options);
    sshClient.executeCommand("sleep 5s", userAtHost);

## Spring configuration (using the c-namespace and property placeholders)

    // E.g. META-INF/spring/config.xml:

    <bean id="sshClient" class="fi.jpalomaki.ssh.jsch.JschSshClient"
        c:privateKey="${ssh.privateKey}" c:knownHosts="${ssh.knownHosts}"
        c:passphrase="${ssh.passphrase}" c:options-ref="sshOptions"
    />
    
    <bean id="sshOptions" class="fi.jpalomaki.ssh.jsch.JschSshClient$Options"
        c:connectTimeout="3s" c:sessionTimeout="5m" c:maxStdoutSize="1M"
        c:maxStderrSize="1M" c:sshConfig="CompressionLevel=1;TCPKeepAlive=no"
        c:allocatePty="true" />
        
    <context:property-placeholder location="classpath:META-INF/spring/props/ssh-client.properties" />
    
    // E.g. META-INF/spring/props/ssh-client.properties:
    
    ssh.knownHosts = /path/to/.ssh/known_hosts
    ssh.privateKey = /path/to/.ssh/id_rsa
    ssh.passphrase = secret
