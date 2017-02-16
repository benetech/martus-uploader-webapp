package org.martus.uploader.martus;

//import java.io.File;
//import java.util.concurrent.Callable;
//
//import org.martus.clientside.ClientSideNetworkGateway;
//import org.martus.common.crypto.MartusSecurity;
//import org.martus.common.packet.UniversalId;
//import org.martus.uploader.Logger;

public class BulletinSenderRunnable// implements Callable<String> 
{
//	public BulletinSenderRunnable(MartusSecurity crypto, ClientSideNetworkGateway gateway, File zippedFile, UniversalId bulletinId)
//	{
//		this.martusCrypto = crypto;
//		this.gateway = gateway;
//		this.zippedFile = zippedFile;
//		this.bulletinId = bulletinId;
//	}
//
//	@Override
//	public String call() throws Exception
//	{
//		String uploadSingleBulletinResult = BulletinUploader.uploadBulletinZipFile(bulletinId, zippedFile, gateway, martusCrypto);
//		Logger.LogInfo(getClass(), "Upload server result is = " + uploadSingleBulletinResult);
//
//		postSentCleanup();
//
//		return uploadSingleBulletinResult;
//	}
//
//	private void postSentCleanup() 
//	{
//		//FIXME at some point, we need to delete the zip file, otherwise it will run out of storage
//		zippedFile.delete();
//	}
//		
//	private MartusSecurity martusCrypto;
//	private ClientSideNetworkGateway gateway;
//	private UniversalId bulletinId;
//	private File zippedFile;
}
