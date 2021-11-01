package modeal.deploy.backup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPResourceBackupTask extends Task {
	private ChannelSftp sftp;
    private Session session;
    private Channel channel;
    private JSch jsch;
    private Properties config;

	private String server;
	private int port;
	private String userId;
	private String password;
	private String remoteDir;
	private String backupDir;
	private FileSet fileset;
	private List<String> errorList;

	public SFTPResourceBackupTask() {
		this.port = 22;
		this.errorList = new ArrayList<String>();
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRemoteDir() {
		return remoteDir;
	}

	public void setRemoteDir(String remoteDir) {
		this.remoteDir = remoteDir;
	}

	public String getBackupDir() {
		return backupDir;
	}

	public void setBackupDir(String backupDir) {
		this.backupDir = backupDir;
	}

    public void addFileset(FileSet set)
    {
        this.fileset = set;
    }

	@Override
	public void execute() throws BuildException {
		if (StringUtils.isBlank(this.getServer()) || StringUtils.isBlank(this.getUserId()) || StringUtils.isBlank(this.getPassword())) {
			log("SFTP Connect Information is required. server/userId/password attribute is blank.");
			return;
		}
		if (this.fileset == null) {
			log("Fileset is required.");
			return;
		}

        try {
        	jsch = new JSch();
        	config = new Properties();
        	config.put("StrictHostKeyChecking", "no");
        	session = jsch.getSession(this.getUserId(), this.getServer(), this.getPort());
        	session.setPassword(this.getPassword());
        	session.setConfig(config);

        	session.connect();
        	channel = session.openChannel("sftp");
        	channel.connect();
        	sftp = (ChannelSftp)channel;

    		DirectoryScanner ds = this.fileset.getDirectoryScanner(getProject());
    		log(new StringBuilder().append("Include file total count is ").append(ds.getIncludedFilesCount()).toString());

    		String[] includeFiles = ds.getIncludedFiles();
    		for (String file : includeFiles) {
    			this.executeBackup(new File(new StringBuilder().append(this.fileset.getDir().getAbsolutePath()).append("/").append(file).toString()));
    		}
        } catch (JSchException je) {
        	log("Could not connect to server.");
        	je.printStackTrace();
        } catch (Exception e) {
        	log("SFTP Resource Backup Exception.");
            e.printStackTrace();
        } finally {
        	sftp.quit();
            if (sftp.isConnected()) {
            	sftp.disconnect();
            }

            if (channel.isConnected()) {
            	channel.disconnect();
            }
            if (session.isConnected()) {
            	session.disconnect();
            }

            if (!this.errorList.isEmpty()) {
        		for (String errorMsg : this.errorList) {
    				log(errorMsg, Project.MSG_ERR);
        		}
        		throw new BuildException();
        	}
        }

	}

	private void executeBackup(File file) {
		String absolutePath = file.getAbsolutePath();
		String fileName = StringUtils.substringAfterLast(absolutePath, "/");
		String path = StringUtils.substringBetween(absolutePath, this.fileset.getDir().getPath(), fileName);
		File backupFile = null;
		BufferedOutputStream bos = null;

		File backupDir = new File(new StringBuilder().append(this.getBackupDir()).append("/").append(path).toString());
		if (!backupDir.exists()) {
			backupDir.mkdirs();
		}

		try {			
			backupFile = new File(new StringBuilder().append(backupDir.getAbsolutePath()).append("/").append(fileName).toString());
			bos = new BufferedOutputStream(new FileOutputStream(backupFile));
			sftp.get(new StringBuilder().append(this.getRemoteDir()).append(path).append(fileName).toString(), bos);
			log(new StringBuilder().append("backup 1 file. path:").append(path).append(fileName).toString());
		} catch (SftpException se) {
			log(new StringBuilder().append("backup file Not Found. path:").append(path).append(fileName).toString());
			if (backupFile != null && backupFile.exists()) {backupFile.delete();}
			if (backupDir.listFiles().length == 0) {backupDir.delete();}
		} catch (Exception e) {
			this.addErrorMsg(new StringBuilder().append("backup failure. path:").append(path).append(fileName).append(", msg:").append(e.toString()).toString());
			if (backupFile != null && backupFile.exists()) {backupFile.delete();}
			if (backupDir.listFiles().length == 0) {backupDir.delete();}
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {}
			}
		}
	}

	private void addErrorMsg(String msg) {
		this.errorList.add(msg);
	}
	
	public static void main(String args[]) {
	}
}