package org.martus.uploader.martus;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Set;
//import java.util.Vector;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.zip.ZipFile;

//import org.json.JSONObject;
//import org.martus.clientside.ClientSideNetworkGateway;
//import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer;
//import org.martus.common.MartusUtilities;
//import org.martus.common.crypto.MartusCrypto;
//import org.martus.common.crypto.MartusSecurity;
//import org.martus.common.network.NetworkInterfaceConstants;
//import org.martus.common.network.NetworkResponse;
//import org.martus.common.network.NonSSLNetworkAPIWithHelpers;
//import org.martus.common.network.OrchidTransportWrapper;
//import org.martus.common.packet.BulletinHeaderPacket;
//import org.martus.common.packet.UniversalId;
//import org.martus.uploader.Logger;
//import org.martus.util.StreamableBase64;

public class BulletinUploader 
{
//	private static final int NUMBER_OF_THREADS = 1;
//	private MartusSecurity martusCrypto;
//	private ClientSideNetworkGateway gateway;
//	private String serverIP;
//	private String magicWord;
// 	private ArrayList<File> bulletinMbaFilesToUpload;
//	private File serverResponseFile;
//
//	public BulletinUploader(String serverIP, String magicWord, ArrayList<File> bulletinMbaFilesToUpload, File serverResponseFile)
//	{
//		this.serverIP = serverIP;
//		this.magicWord = magicWord;
//		this.bulletinMbaFilesToUpload = bulletinMbaFilesToUpload;
//		this.serverResponseFile = serverResponseFile;		
//	}
//
//	public void startLoading() throws Exception
//	{
//		try
//		{
//			Logger.LogInfo(getClass(), "About to construct a Martus Security object");
//			martusCrypto = new MartusSecurity();
//			Logger.LogInfo(getClass(), "MartusSecurity object instantiated.  Starting to create key pair");
//			getMartusSecurity().createKeyPair();
//			Logger.LogInfo(getClass(), "Uploader public key =" + getMartusSecurity().getPublicKeyString());
//			Logger.LogInfo(getClass(), "Uploader signature of public key =" + getMartusSecurity().getSignatureOfPublicKey());
//		} 
//		catch (Exception e) 
//		{
//			Logger.LogInfo(getClass(), "Unable to create crypto");
//			Logger.Log(getClass(), e);
//		}
//
//		if (verifyMartusServerViaMultipleAttempts())
//			uploadBulletins();		
//	}
//
//	private boolean verifyMartusServerViaMultipleAttempts() throws Exception 
//	{
//		final int NUMBER_OF_VERIFY_SERVER_ATTEMPTS = 5;
//		for (int index = 0; index <= NUMBER_OF_VERIFY_SERVER_ATTEMPTS; ++index)
//		{
//			try 
//			{
//				Logger.LogInfo(getClass(), "Verifying server.  Verify count = " + index);
//				if (verifyServer())
//					return true;
//				
//				Thread.sleep(5 * 1000);
//			}
//			catch (Exception e)
//			{
//				if (index < NUMBER_OF_VERIFY_SERVER_ATTEMPTS)
//					continue;
//
//				throw e;
//			}
//		}
//		
//		return false;
//	}
//	
//	private HashMap<String, Future<String>> sendBulletins() throws Exception 
//	{
//		Logger.LogInfo(getClass(), "Start sending bulletins.  Count = " + bulletinMbaFilesToUpload.size());
//		
//		ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
//		HashMap<String, Future<String>> bulletinIdToFutures = new HashMap<>();
//		for (File mbaFileToUpload : bulletinMbaFilesToUpload) 
//		{
//			ZipFile mbaZipFile = new ZipFile(mbaFileToUpload);
//			BulletinHeaderPacket bhp = BulletinHeaderPacket.loadFromZipFile(mbaZipFile, getMartusSecurity());
//			UniversalId uId = bhp.getUniversalId();
//			
//			BulletinSenderRunnable sender = new BulletinSenderRunnable(getMartusSecurity(), getGateway(), mbaFileToUpload, uId);
//			Future<String> future = executor.submit(sender);
//			String uIdAsString = uId.toString();
//			bulletinIdToFutures.put(uIdAsString, future);
//		}
//
//		executor.shutdown();
//		
//		while (!executor.isTerminated()) 
//		{
//			//do nothing - just waiting
//		}
//		
//		Logger.LogInfo(getClass(), "Finished sending bulletins to " + getServerIp());
//		
//		return bulletinIdToFutures;
//	}
//
//	private boolean verifyServer() throws Exception
//	{
//		OrchidTransportWrapper transport = OrchidTransportWrapperWithActiveProperty.createWithoutPersistentStore();
//		System.out.println("Is tor on  ? " + transport.isTorEnabled());
//		
//		//IS THIS CORRECT, JUST SETTING IT TO TRUE?
//		transport.setIsOnline(true);
//		transport.stopTor();
//		System.out.println("Is ready on? " + transport.isReady());
//        NonSSLNetworkAPIWithHelpers server = new ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer(getServerIp(), transport);
//		try
//		{
//			Vector serverInfo = server.getServerInformation();
//			String serverVersion = (String) serverInfo.get(0);
//			Logger.LogInfo(getClass(), "Connecting to martus server version: " + serverVersion);
//			String serverPublicKey = (String) serverInfo.get(1);
//			gateway = ClientSideNetworkGateway.buildGateway(getServerIp(), serverPublicKey, transport);
//			NetworkResponse response = getGateway().getUploadRights(getMartusSecurity(), getMagicWord());
//			Logger.LogInfo(getClass(), "Server response was: " + response.getResultCode());
//
//			return true;
//		}
//		catch (Exception e) 
//		{
//			Logger.LogInfo(getClass(), "Couldn't verify server");
//			Logger.Log(getClass(), e);
//			throw e;
//		}
//	}
//
//	private void uploadBulletins() throws Exception 
//	{
//		HashMap<String, Future<String>> bulletinIdToUploadStatusMap = sendBulletins();
//		Set<String> bulletinIds = bulletinIdToUploadStatusMap.keySet();
//		JSONObject mapAsJson = new JSONObject();
//		for (String bulletinId : bulletinIds)
//		{
//			Future<String> future = bulletinIdToUploadStatusMap.get(bulletinId);
//			mapAsJson.put(bulletinId, future.get());
//		}
//		
//		try (FileWriter fileWriter = new FileWriter(serverResponseFile)) 
//		{
//			fileWriter.write(mapAsJson.toString());
//			Logger.LogInfo(getClass(), "Json written here: " + serverResponseFile.getAbsolutePath());
//		}
//		catch (Exception e)
//		{
//			Logger.Log(getClass(), e);
//		}
//	}
//
//	private ClientSideNetworkGateway getGateway() 
//	{
//		return gateway;
//	}
//
//	private String getServerIp() 
//	{
//		return serverIP;
//	}
//
//	private MartusSecurity getMartusSecurity() 
//	{
//		return martusCrypto;
//	}
//
//	private String getMagicWord() 
//	{
//		return magicWord;
//	}
//
//	public static String uploadBulletinZipFile(UniversalId uid, File tempFile, ClientSideNetworkGateway gateway, MartusCrypto crypto) throws MartusUtilities.FileTooLargeException, IOException, MartusCrypto.MartusSignatureException
//	{
//		final int totalSize = MartusUtilities.getCappedFileLength(tempFile);
//		int offset = 0;
//		byte[] rawBytes = new byte[NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE];
//		FileInputStream inputStream = new FileInputStream(tempFile);
//		String result = null;
//		
//		while (true)
//		{
//			int chunkSize = inputStream.read(rawBytes);
//			if(chunkSize <= 0)
//				break;
//			
//			byte[] chunkBytes = new byte[chunkSize];
//			System.arraycopy(rawBytes, 0, chunkBytes, 0, chunkSize);
//
//			String authorId = uid.getAccountId();
//			String bulletinLocalId = uid.getLocalId();
//			String encoded = StreamableBase64.encode(chunkBytes);
//
//			NetworkResponse response = gateway.putBulletinChunk(crypto, authorId, bulletinLocalId, totalSize, offset, chunkSize, encoded);
//			result = response.getResultCode();
//			System.out.println("Martus server result for uploading single mba : " + result);
//			if(!result.equals(NetworkInterfaceConstants.CHUNK_OK) && !result.equals(NetworkInterfaceConstants.OK))
//				break;
//			
//			offset += chunkSize;
//		}
//		
//		inputStream.close();
//		
//		return result;
//	}
}
