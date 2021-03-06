package org.martus.uploader;

import java.io.File;

import org.martus.clientside.ClientPortOverride;
import org.martus.uploader.storage.FileSystemStorageService;
import org.martus.uploader.storage.StorageProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class MartusUploaderWebappApplication extends SpringBootServletInitializer
{
	private static final String VERSION_CODE = "20";
	private static File rootLocation;

	public static void main(String[] args) 
	{
		System.out.println("Running Martus uploader application via jar.  Version code: " + VERSION_CODE);
		SpringApplication.run(MartusUploaderWebappApplication.class, args);
		init();
	}
	
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
    	System.out.println("Running Martus uploader application via war. Version code: " + VERSION_CODE);
    	init();
        return applicationBuilder.sources(MartusUploaderWebappApplication.class);
    }
	
	private static void init() 
	{
		System.out.println("Connecting to martus server IP " + getServerIp());
		turnOnInsecurePortsForLocaluse();
	}

	private static void turnOnInsecurePortsForLocaluse() 
	{
		if (getServerIp().equals(SERVER_IP_LCOAL))
		{
			ClientPortOverride.useInsecurePorts = true;
			System.out.println("WARNING: Using Insecure ports: (Should be turned on for local use only): " + ClientPortOverride.useInsecurePorts);
		}
	}

	@Bean
	public CommandLineRunner init(FileSystemStorageService storageService) 
	{
		return (args) -> 
		{
			//storageService.deleteAll();
            storageService.init();
            rootLocation = storageService.getRootDir();
            Logger.LogInfo(getClass(), "Root folder set to = " + rootLocation.getAbsolutePath());
		};
	}
	
	public static File getRootLocation() 
	{
		return rootLocation;
	}
	
	public static String getServerIp()
	{
		return SERVER_IP_SL1_IE;
	}

	private static final String SERVER_IP_SL1_BR = "54.207.51.132";
	private static final String SERVER_IP_SL1_IE = "54.72.26.74";
	private static final String SERVER_IP_SL1_DEV = "54.213.152.140";
	private static final String SERVER_IP_LCOAL = "127.0.0.1";
}
