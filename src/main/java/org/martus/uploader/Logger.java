package org.martus.uploader;

import org.slf4j.LoggerFactory;

public class Logger 
{
	public static final void Log(Class<?> classToUse, Exception e) 
	{
		LoggerFactory.getLogger(classToUse).error(e.getMessage(), e);
		
		//FIXME this needs to do the right logging, and not print to console
		e.printStackTrace();
	}
	
	public static final void Log(Class<?> classToUse, String errorMessage) 
	{
		Log(classToUse, new Exception(errorMessage));
	}
	
	public static final void LogInfo(Class<?> classToUse, String infoMessage)
	{
		LoggerFactory.getLogger(classToUse).info(infoMessage);
	}
}
