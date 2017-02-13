package org.martus.uploader;

import java.io.File;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.clientside.ClientPortOverride;
import org.martus.common.crypto.MartusSecurity;
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
	private static MartusSecurity martusCrypto;
//	private static ClientBulletinStore store;
	private static File rootLocation;

	public static void main(String[] args) 
	{
		System.out.println("Running Martus uploader application via main 5");
		SpringApplication.run(MartusUploaderWebappApplication.class, args);
		init();
	}
	
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
    	System.out.println("Running Martus uploader application via war 8");
//    	init();
        return applicationBuilder.sources(MartusUploaderWebappApplication.class);
    }
	
	private static void init() 
	{
//		store = new ClientBulletinStore(martusCrypto);
//        try {
//            store.doAfterSigninInitialization(getRootLocation());
//        } catch (Exception e) {
//            Logger.Log(MartusUploaderWebappApplication.class, new Exception("Unable to initialize bulletin store"));
//        }

        //FIXME this should/might need to be changed when going live
        ClientPortOverride.useInsecurePorts = true;
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
	
//	public static MartusSecurity getMartusSecurity() 
//	{
//		if (martusCrypto == null) 
//		{
//			createMartusSecurity();
//		}
//		
//		return martusCrypto;
//	}
//
//	private static void createMartusSecurity() 
//	{
//		try 
//		{
//			martusCrypto = new MartusSecurity();
//		} 
//		catch (Exception e) 
//		{
//			Logger.Log(MartusUploaderWebappApplication.class, e);
//		}
//	}
	
	public static String getServerIp()
	{
		return SERVER_IP_LCOAL;
	}

//	private static final String SERVER_IP_SL1_DEV = "54.213.152.140";
	private static final String SERVER_IP_LCOAL = "127.0.0.1";
}
