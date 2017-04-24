
package org.martus.uploader.storage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.martus.uploader.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService 
{
    private final Path rootLocation;
	private File tempDir;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) throws Exception 
    {
    	tempDir = File.createTempFile("martusUploadTemp", "").getParentFile();
//        this.rootLocation = Paths.get(properties.getLocationName());
    	rootLocation = tempDir.toPath();
        System.out.println("File system storage service constructor called.  rootLocation set to: " + rootLocation.toAbsolutePath());
    }

    @Override
    public File store(MultipartFile file) 
    {
        try 
        {
            if (file.isEmpty()) 
            {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }
            
            Path newTempFilePath = this.rootLocation.resolve(file.getOriginalFilename());
            if (newTempFilePath.toFile().exists())
            {
            	Logger.LogInfo(getClass(), "Found a matching file name as what user is attempting to upload.  Deleting duplicate on server!");
            	newTempFilePath.toFile().delete();
            }
            
			Files.copy(file.getInputStream(), newTempFilePath);
			
			return new File(newTempFilePath.toString());
        } 
        catch (IOException e) 
        {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public Stream<Path> loadAll() 
    {
        try 
        {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(path -> this.rootLocation.relativize(path));
        }
        catch (IOException e) 
        {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) 
    {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) 
    {
        try 
        {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) 
            {
                return resource;
            }
            else 
            {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } 
        catch (MalformedURLException e) 
        {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() 
    {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }
    
    public File getRootDir()
    {
    	return rootLocation.toFile();
    }

    @Override
    public void init() 
    {
        try 
        {
        	System.out.println("Is rootLocation readonly?" + rootLocation.getFileSystem().isReadOnly());
        	if (!Files.exists(rootLocation))
        	{
        		Files.createDirectory(rootLocation);
        		Logger.LogInfo(getClass(), "Created root dir for temp storage:" + rootLocation.toFile().getAbsolutePath());
        	}
        	else
        	{
        		Logger.LogInfo(getClass(), "Using existing root dir for temp storage:" + rootLocation.toFile().getAbsolutePath());
        	}
            
        } 
        catch (IOException e) 
        {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
