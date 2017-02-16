package org.martus.uploader;

import java.io.File;

//import org.martus.client.swingui.Martus;
//import org.martus.common.crypto.MartusSecurity;
//import org.martus.uploader.storage.FileSystemStorageService;
import org.martus.uploader.storage.StorageProperties;
//import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;
//import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
//@EnableConfigurationProperties(StorageProperties.class)
public class MartusUploaderWebappApplication extends SpringBootServletInitializer
{
	private static final String VERSION_CODE = "15";
	private static File rootLocation;

	public static void main(String[] args) 
	{
		System.out.println("Running Martus uploader application via jar.  Version code: " + VERSION_CODE);
		SpringApplication.run(MartusUploaderWebappApplication.class, args);
//		init();
	}
	
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
    	System.out.println("Running Martus uploader application via war. Version code: " + VERSION_CODE);
//    	init();
        return applicationBuilder.sources(MartusUploaderWebappApplication.class);
    }
	
//	private static void init() 
//	{
//		try 
//		{
//			Martus.addThirdPartyJarsToClasspath();
//		}
//		catch (Exception e) 
//		{
//			Logger.Log(MartusUploaderWebappApplication.class, e);
//		}
//	}
//
//	@Bean
//	public CommandLineRunner init(FileSystemStorageService storageService) 
//	{
//		return (args) -> 
//		{
//			//storageService.deleteAll();
//            storageService.init();
//            rootLocation = storageService.getRootDir();
//            Logger.LogInfo(getClass(), "Root folder set to = " + rootLocation.getAbsolutePath());
//		};
//	}
//	
//	public static File getRootLocation() 
//	{
//		return rootLocation;
//	}
//	
//	public static String getServerIp()
//	{
//		return SERVER_IP_SL1_DEV;
//	}
//
//	private static final String SERVER_IP_SL1_DEV = "54.213.152.140";
//	//private static final String SERVER_IP_LCOAL = "127.0.0.1";
}
