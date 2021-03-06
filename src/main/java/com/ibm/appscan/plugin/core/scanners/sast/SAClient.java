/**
 * © Copyright IBM Corporation 2016.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.plugin.core.scanners.sast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.ibm.appscan.plugin.core.CoreConstants;
import com.ibm.appscan.plugin.core.error.ScannerException;
import com.ibm.appscan.plugin.core.logging.DefaultProgress;
import com.ibm.appscan.plugin.core.logging.IProgress;
import com.ibm.appscan.plugin.core.logging.Message;
import com.ibm.appscan.plugin.core.scanners.Messages;
import com.ibm.appscan.plugin.core.utils.ArchiveUtil;
import com.ibm.appscan.plugin.core.utils.ServiceUtil;
import com.ibm.appscan.plugin.core.utils.SystemUtil;

public class SAClient implements SASTConstants {

	private static final File DEFAULT_INSTALL_DIR = new File(System.getProperty("user.home"), ".appscan"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final String SACLIENT = "SAClientUtil"; //$NON-NLS-1$
	private static final String VERSION_INFO = "version.info"; //$NON-NLS-1$
	
	private IProgress m_progress;
	private ProcessBuilder m_builder;
	private File m_installDir;
	
	public SAClient() {
		this(new DefaultProgress());
	}
	
	public SAClient(IProgress progress) {
		m_progress = progress;
		String install = System.getProperty(CoreConstants.SACLIENT_INSTALL_DIR);
		m_installDir = install == null ? DEFAULT_INSTALL_DIR : new File(install);
	}
	
	public int run(String workingDir, List<String> args) throws IOException, ScannerException {	
		ArrayList<String> arguments = new ArrayList<String>();
		arguments.add(getClientScript());
		arguments.addAll(args);
		m_builder = new ProcessBuilder(arguments);
		m_builder.directory(new File(workingDir));
		
		m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(PREPARING_IRX, getLocalClientVersion())));
		final Process proc = m_builder.start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String line;
				try {
					while((line = reader.readLine()) != null)
						m_progress.setStatus(new Message(Message.INFO, line));
				}
				catch(IOException e) {
					m_progress.setStatus(e);
				} 
				finally {
					try {
						reader.close();
					} catch (IOException e) {
						m_progress.setStatus(e);
					}
				}
			}
		}).start();
		
		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			m_progress.setStatus(e);
			return -1;
		}

		return proc.exitValue();
	}
	
	private String getClientScript() throws IOException, ScannerException {
		//See if we already have the client package.
		String scriptPath = "bin" + File.separator + getScriptName(); //$NON-NLS-1$
		File install = findClientInstall();
		
		if(install != null && new File(install, scriptPath).isFile() && !shouldUpdateClient())
			return new File(install, scriptPath).getAbsolutePath();
		
		//Download it.
		m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(DOWNLOADING_CLIENT)));
		if(install != null && install.isDirectory())
			deleteDirectory(install);
		
		File clientZip = new File(m_installDir, SACLIENT + ".zip"); //$NON-NLS-1$
		if(clientZip.exists())
			clientZip.delete();
		
		try {
			ServiceUtil.getSAClientUtil(clientZip);
		} catch(OutOfMemoryError e) {
			throw new ScannerException(Messages.getMessage(DOWNLOAD_OUT_OF_MEMORY), e);
		}	
		
		if(clientZip.isFile()) {
			m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(DOWNLOAD_COMPLETE)));
			m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(EXTRACTING_CLIENT)));
			ArchiveUtil.unzip(clientZip, m_installDir);
			m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(DONE)));
		}

		return new File(findClientInstall(), scriptPath).getAbsolutePath();
	}
	
	private String getScriptName() {
		return SystemUtil.isWindows() ? WIN_SCRIPT : UNIX_SCRIPT;
	}
	
	private boolean shouldUpdateClient() throws IOException {
		String serverVersion = ServiceUtil.getSAClientVersion();
		String localVersion = getLocalClientVersion();

		if(localVersion != null && serverVersion != null) {
			String[] local = localVersion.split("\\."); //$NON-NLS-1$
			String[] server = serverVersion.split("\\."); //$NON-NLS-1$

			for(int iter = 0; iter < local.length && iter < server.length; iter++) {
				int lVersion = Integer.parseInt(local[iter]);
				int sVersion = Integer.parseInt(server[iter]);
				
				if (((iter==0) && lVersion<sVersion)
						|| (iter==1 && lVersion<sVersion)
						|| (iter==2 && lVersion<sVersion)) {
					m_progress.setStatus(new Message(Message.INFO, Messages.getMessage(SACLIENT_OUTDATED, localVersion, serverVersion)));
					return true;
				}
			}
		}
		return false;
	}
	
	private File findClientInstall() {
		if(!m_installDir.isDirectory())
			return null;
		
		File files[] = m_installDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(SACLIENT) && new File(dir, name).isDirectory();
			}
		});
		return files.length == 0 ? null : files[0];
	}
	
	private String getLocalClientVersion() {
		File versionInfo = new File(findClientInstall(), VERSION_INFO);
		String version = null;
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(versionInfo));
			version = reader.readLine(); //The version is the first line of the version.info file.
		} catch (IOException e) {
			m_progress.setStatus(new Message(Message.ERROR, Messages.getMessage(ERROR_CHECKING_SACLIENT_VER,e)));
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
		return version;
	}
	
	private void deleteDirectory(File directory) {
		if(!directory.isDirectory())
			directory.delete();
		
		for(File file : directory.listFiles()) {
			if(file.isDirectory())
				deleteDirectory(file);
			else
				file.delete();
		}
		directory.delete();
	}
}
