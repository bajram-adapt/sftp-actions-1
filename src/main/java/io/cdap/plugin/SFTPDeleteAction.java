package io.cdap.plugin;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.etl.api.action.Action;
import io.cdap.cdap.etl.api.action.ActionContext;
import io.cdap.plugin.common.SFTPActionConfig;
import io.cdap.plugin.common.SFTPConnector;
import com.jcraft.jsch.ChannelSftp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link Action} to delete files on the SFTP server.
 */
@Plugin(type = Action.PLUGIN_TYPE)
@Name("SFTPDelete")
public class SFTPDeleteAction extends Action {

  private static final Logger LOG = LoggerFactory.getLogger(SFTPDeleteAction.class);
  private SFTPDeleteActionConfig config;
  public SFTPDeleteAction(SFTPDeleteActionConfig config) {
    this.config = config;
  }

  public class SFTPDeleteActionConfig extends SFTPActionConfig {
    @Description("Comma separated list of files to be deleted from FTP server.")
    @Macro
    public String filesToDelete;

    @Description("Boolean flag to determine if execution should continue if there is an error while deleting any file." +
      " Defaults to 'false'.")
    boolean continueOnError;

    public String getFilesToDelete() {
      return filesToDelete;
    }
  }

  @Override
  public void run(ActionContext context) throws Exception {
    String filesToDelete = config.getFilesToDelete();
    if (filesToDelete == null || filesToDelete.isEmpty()) {
      return;
    }
    try (SFTPConnector SFTPConnector = new SFTPConnector(config.getHost(), config.getPort(), config.getUserName(),
                                                      config.getPassword(), config.getSSHProperties())) {
      ChannelSftp channelSftp = SFTPConnector.getSftpChannel();
      for (String fileToDelete : filesToDelete.split(",")) {
        LOG.info("Deleting {}", fileToDelete);
        try {
          channelSftp.rm(fileToDelete);
        } catch (Throwable t) {
          if (config.continueOnError) {
            LOG.warn("Error deleting file {}.", fileToDelete, t);
          } else {
            throw t;
          }
        }
      }
    }
  }
}
