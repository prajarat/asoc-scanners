/**
 * © Copyright IBM Corporation 2016.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.plugin.core.scanners.sast;


public interface SASTConstants {

	String APPSCAN_INSTALL_DIR			= "APPSCAN_INSTALL_DIR";			//$NON-NLS-1$
	String ARSA_FILE_ID					= "ARSAFileId";						//$NON-NLS-1$
	String WIN_SCRIPT					= "appscan.bat";					//$NON-NLS-1$
	String UNIX_SCRIPT					= "appscan.sh";						//$NON-NLS-1$

	String IRX_EXTENSION				= ".irx";							//$NON-NLS-1$
	String SAST							= "Static Analyzer";				//$NON-NLS-1$
	String STATIC_ANALYZER				= "StaticAnalyzer";					//$NON-NLS-1$
	
	String PREPARE						= "prepare";						//$NON-NLS-1$
	String OPT_NAME						= "-n";								//$NON-NLS-1$
	String PREPARE_ONLY					= "prepareOnly";					//$NON-NLS-1$
	
	//Messages
	String DONE							= "message.done";					//$NON-NLS-1$
	String DOWNLOAD_COMPLETE			= "message.download.complete";		//$NON-NLS-1$
	String DOWNLOADING_CLIENT			= "message.downloading.client";		//$NON-NLS-1$
	String EXTRACTING_CLIENT			= "message.extracting.client";		//$NON-NLS-1$
	String PREPARING_IRX				= "message.preparing.irx";			//$NON-NLS-1$
	String SACLIENT_OUTDATED			= "message.saclient.old";			//$NON-NLS-1$
	
	//Errors
	String IRX_MISSING					= "error.irx.missing";				//$NON-NLS-1$
	String ERROR_CHECKING_SACLIENT_VER 	= "error.checking.local.version";	//$NON-NLS-1$
	String ERROR_GENERATING_IRX			= "error.generating.irx";			//$NON-NLS-1$
	String ERROR_SUBMITTING_IRX			= "error.submitting.irx";			//$NON-NLS-1$
	String DOWNLOAD_OUT_OF_MEMORY		= "error.out.of.memory";			//$NON-NLS-1$
}
