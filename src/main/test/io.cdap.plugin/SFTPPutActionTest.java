package io.cdap.plugin;


import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.cdap.cdap.etl.mock.action.MockActionContext;
import io.cdap.cdap.etl.mock.common.MockPipelineConfigurer;
import org.junit.*;
import software.sham.sftp.MockSftpServer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



public class SFTPPutActionTest {

    MockSftpServer server;
    Session sshSession;


    @Before
    public void initSftp() throws IOException {
        server = new MockSftpServer(9022);
    }

    @Before
    public void initSshClient() throws JSchException {
        Map<String, String> properties = new HashMap<>();
        properties.put("StrictHostKeyChecking", "no");
        JSch jsch = new JSch();
        sshSession = jsch.getSession("tester", "localhost", 9022);
        Properties config = new Properties();
        config.putAll(properties);
        sshSession.setConfig(config);
        sshSession.setPassword("testing");
        sshSession.connect();
    }

    @After
    public void stopSftp() throws IOException {
        server.stop();
    }

    @Test
    public void testPutFile() throws Exception {
        String filePath = "src/main/test/resources/";
        String destPath = server.getBaseDirectory().toString();
        SFTPPutAction.SFTPPutActionConfig config = new SFTPPutAction.SFTPPutActionConfig(
                "localhost",
                9022,
                "tester",
                "testing",
                "",
                filePath,
                destPath,
                "password");
        MockPipelineConfigurer configurer = new MockPipelineConfigurer(null);
        new SFTPPutAction(config).configurePipeline(configurer);
        new SFTPPutAction(config).run(new MockActionContext());
    }

}
