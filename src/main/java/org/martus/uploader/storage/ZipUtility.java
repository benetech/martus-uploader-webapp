package org.martus.uploader.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtility 
{
	private static final int BUFFER_SIZE = 2048;
	private static final String ZIP_EXTENSION = ".zip";
	private static final int EXTENSION_LENGTH = ZIP_EXTENSION.length();
	
	public static File extractFolder(File zipFileToExtract) throws Exception 
	{
	    ZipFile zip = new ZipFile(zipFileToExtract);
	    String zipFilePath = zipFileToExtract.getAbsolutePath();
		String desintationDirPath = zipFilePath.substring(0, zipFilePath.length() - EXTENSION_LENGTH);
	    File destinationDir = new File(desintationDirPath);
	    if (!destinationDir.exists())
	    	destinationDir.mkdir();
	    
	    Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
	    while (zipFileEntries.hasMoreElements())
	    {
	        ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
	        String currentEntryName = entry.getName();
	        File destinationFile = new File(desintationDirPath, currentEntryName);
	        File destinationParent = destinationFile.getParentFile();
	        destinationParent.mkdirs();

	        if (!entry.isDirectory())
	        {
	            extractFile(zip, entry, destinationFile);
	        }

	        if (currentEntryName.endsWith(ZIP_EXTENSION))
	        {
	            extractFolder(destinationFile);
	        }
	    }
	    
	    return destinationDir;
	}

	private static void extractFile(ZipFile zip, ZipEntry entry, File destFile) throws Exception 
	{
		byte data[] = new byte[BUFFER_SIZE];
		FileOutputStream fileOutputStream = new FileOutputStream(destFile);
		BufferedOutputStream destinationOutputStream = null;
		BufferedInputStream inputStream = null;
		try
		{
			destinationOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);
			inputStream = new BufferedInputStream(zip.getInputStream(entry));
			int currentByte;
			while ((currentByte = inputStream.read(data, 0, BUFFER_SIZE)) != -1) 
			{
				destinationOutputStream.write(data, 0, currentByte);
			}
		}
		finally 
		{
			if (destinationOutputStream != null)
			{
				destinationOutputStream.flush();
				destinationOutputStream.close();
			}
			if (inputStream != null)
			{
				inputStream.close();
			}
		}
	}
}
