package org.martus.uploader.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

//@ConfigurationProperties("storage")
public class StorageProperties 
{
    private String fileStorageLLocation = "upload-dir";

    public String getLocationName() 
    {
        return fileStorageLLocation;
    }

    public void setLocation(String location) 
    {
        this.fileStorageLLocation = location;
    }
}
