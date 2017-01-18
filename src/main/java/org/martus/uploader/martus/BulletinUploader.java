package org.martus.uploader.martus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipFile;

import org.json.JSONObject;
import org.martus.clientside.ClientPortOverride;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.NonSSLNetworkAPIWithHelpers;
import org.martus.common.network.OrchidTransportWrapper;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.util.StreamableBase64;

public class BulletinUploader 
{
	private static final int NUMBER_OF_THREADS = 1;
	private MartusSecurity martusCrypto;
	private ClientSideNetworkGateway gateway;
	private String serverIP;
	private String magicWord;
 	private ArrayList<File> bulletinMbaFilesToUpload;
	private File serverResponseFile;

	public BulletinUploader(String serverIP, String magicWord, ArrayList<File> bulletinMbaFilesToUpload, File serverResponseFile)
	{
		this.serverIP = serverIP;
		this.magicWord = magicWord;
		this.bulletinMbaFilesToUpload = bulletinMbaFilesToUpload;
		this.serverResponseFile = serverResponseFile;
		
		//FIXME this needs to happen via command line args
		if (true) 
		{
			ClientPortOverride.useInsecurePorts = true;
		}
		
		//FIXME does this need to happen
		//Martus.addThirdPartyJarsToClasspath();
	}

	public void startLoading()
	{
		try
		{
			martusCrypto = new MartusSecurity();
			getMartusSecurity().createKeyPair();
			MartusLogger.log("Uploader public key =" + getMartusSecurity().getPublicKeyString());
			MartusLogger.log("Uploader signature of public key =" + getMartusSecurity().getSignatureOfPublicKey());
		} 
		catch (Exception e) 
		{
			MartusLogger.log("Unable to create crypto");
			MartusLogger.logException(e);
		}

//		store = new ClientBulletinStore(getMartusSecurity());
//		try
//		{
//			tempDir = DirectoryUtils.createTempDir();
//			store.doAfterSigninInitialization(tempDir);
//			store.createFieldSpecCacheFromDatabase();
//		} 
//		catch (Exception e) 
//		{
//			MartusLogger.log("unable to initialize store");
//			MartusLogger.logException(e);
//		}

//		MartusLogger.log("tempDir is " + tempDir);
//		zippedBulletins = new File[numBulletins];
//		bulletinIds = new UniversalId[numBulletins];
		try
		{
//			createZippedBulletins();
//			loadBulletins();

			verifyMartusServer();

			uploadBulletins();
		} 
		catch (Exception e) 
		{
			MartusLogger.log("problem sending bulletins");
			MartusLogger.logException(e);
		}
//		DirectoryUtils.deleteEntireDirectoryTree(tempDir);
	}

	private void verifyMartusServer() throws Exception 
	{
		MartusLogger.log("Verifying server");
		try 
		{
			while (!verifyServer()) 
			{
				Thread.sleep(5 * 1000);
			}
		} 
		catch (InterruptedException e) 
		{
			MartusLogger.logException(e);
		}
	}
	
	//DO WE NEED THIS
//	private void loadBulletins() throws Exception 
//	{
//		System.out.println("----------- number of bulletins to load= " + bulletinMbaFilesToUpload.size());
//		for (File zipFile : bulletinMbaFilesToUpload) 
//		{
//			ZipFile zip = new ZipFile(zipFile);
//			BulletinHeaderPacket bhp = BulletinHeaderPacket.loadFromZipFile(zip, getMartusSecurity());
//			UniversalId uid = bhp.getUniversalId();
//			System.out.println("----------------------bulletin uid = " + uid.getAccountId());
//		}
//	}
//
	private HashMap<String, Future<String>> sendBulletins() throws Exception 
	{
		MartusLogger.log("Start sending bulletins.  Count = " + bulletinMbaFilesToUpload.size());
		
		ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		HashMap<String, Future<String>> bulletinIdToFutures = new HashMap<>();
		for (File mbaFileToUpload : bulletinMbaFilesToUpload) 
		{
			ZipFile mbaZipFile = new ZipFile(mbaFileToUpload);
			BulletinHeaderPacket bhp = BulletinHeaderPacket.loadFromZipFile(mbaZipFile, getMartusSecurity());
			UniversalId uId = bhp.getUniversalId();
			
			BulletinSenderRunnable sender = new BulletinSenderRunnable(getMartusSecurity(), getGateway(), mbaFileToUpload, uId);
			Future<String> future = executor.submit(sender);
			String uIdAsString = uId.toString();
			bulletinIdToFutures.put(uIdAsString, future);
		}

		executor.shutdown();
		
		while (!executor.isTerminated()) 
		{
			//do nothing - just waiting
		}
		
		MartusLogger.log("Finished sending bulletins to " + getServerIp());
		
		return bulletinIdToFutures;
	}

	private boolean verifyServer() throws Exception
	{
		OrchidTransportWrapper transport = OrchidTransportWrapperWithActiveProperty.createWithoutPersistentStore();
		
		//IS THIS CORRECT, JUST SETTING IT TO TRUE?
		transport.setIsOnline(true);
		
        NonSSLNetworkAPIWithHelpers server = new ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer(getServerIp(), transport);
		try
		{
			String result = server.getServerPublicKey(getMartusSecurity());
			gateway = ClientSideNetworkGateway.buildGateway(getServerIp(), result, transport);
			NetworkResponse response = getGateway().getUploadRights(getMartusSecurity(), getMagicWord());
			if (response.getResultCode().equals(NetworkInterfaceConstants.OK))
			{
				MartusLogger.log("Verified Server.  Server response was: " + response.getResultCode());
				return true;
			}
			
			MartusLogger.log("Could not verify server. Server response was: " + response.getResultCode());
			return false;
		}
		catch (ServerNotAvailableException e)
		{
			MartusLogger.log("Martus Server is not available!");
			MartusLogger.logException(e);
		}
		catch (Exception e) 
		{
			MartusLogger.log("Couldn't verify server");
			MartusLogger.logException(e);
		}
		
		return false;
	}
	
	private void uploadBulletins() throws Exception 
	{
		HashMap<String, Future<String>> bulletinIdToUploadStatusMap = sendBulletins();
		Set<String> bulletinIds = bulletinIdToUploadStatusMap.keySet();
		JSONObject mapAsJson = new JSONObject();
		for (String bulletinId : bulletinIds)
		{
			Future<String> future = bulletinIdToUploadStatusMap.get(bulletinId);
			mapAsJson.put(bulletinId, future.get());
		}
		
		try (FileWriter fileWriter = new FileWriter(serverResponseFile)) 
		{
			fileWriter.write(mapAsJson.toString());
			MartusLogger.log("Json written here: " + serverResponseFile.getAbsolutePath());
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	private ClientSideNetworkGateway getGateway() 
	{
		return gateway;
	}

	private String getServerIp() 
	{
		return serverIP;
	}

	private MartusSecurity getMartusSecurity() 
	{
		return martusCrypto;
	}

	private String getMagicWord() 
	{
		return magicWord;
	}

	public static String uploadBulletinZipFile(UniversalId uid, File tempFile, ClientSideNetworkGateway gateway, MartusCrypto crypto) throws MartusUtilities.FileTooLargeException, IOException, MartusCrypto.MartusSignatureException
	{
		final int totalSize = MartusUtilities.getCappedFileLength(tempFile);
		int offset = 0;
		byte[] rawBytes = new byte[NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE];
		FileInputStream inputStream = new FileInputStream(tempFile);
		String result = null;
		
		while (true)
		{
			int chunkSize = inputStream.read(rawBytes);
			if(chunkSize <= 0)
				break;
			
			byte[] chunkBytes = new byte[chunkSize];
			System.arraycopy(rawBytes, 0, chunkBytes, 0, chunkSize);

			String authorId = uid.getAccountId();
			String bulletinLocalId = uid.getLocalId();
			String encoded = StreamableBase64.encode(chunkBytes);

			NetworkResponse response = gateway.putBulletinChunk(crypto, authorId, bulletinLocalId, totalSize, offset, chunkSize, encoded);
			result = response.getResultCode();
			System.out.println("Martus server result for uploading single mba : " + result);
			if(!result.equals(NetworkInterfaceConstants.CHUNK_OK) && !result.equals(NetworkInterfaceConstants.OK))
				break;
			
			offset += chunkSize;
		}
		
		inputStream.close();
		
		return result;
	}
}
