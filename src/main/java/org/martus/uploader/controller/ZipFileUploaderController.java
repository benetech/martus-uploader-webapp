package org.martus.uploader.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.uploader.Logger;
import org.martus.uploader.MartusUploaderWebappApplication;
import org.martus.uploader.martus.BulletinUploader;
import org.martus.uploader.storage.StorageService;
import org.martus.uploader.storage.ZipUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class ZipFileUploaderController 
{
	
	@RequestMapping("/")
    public String index() {
        return "Most basic spring boot here.  Its boots and takes forever to start!";
    }
	private final StorageService storageService;

    @Autowired
    public ZipFileUploaderController(StorageService storageService) 
    {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException 
    {
        model.addAttribute("files", storageService
                .loadAll()
                .map(path ->
                        	MvcUriComponentsBuilder
                            .fromMethodName(ZipFileUploaderController.class, "serveFile", path.getFileName().toString())
                            .build().toString())
                .collect(Collectors.toList()));

        return "index";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) 
    {
        Resource file = storageService.loadAsResource(filename);
        
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile uploadedZipFile, RedirectAttributes redirectAttributes) throws Exception 
    {
    	if (uploadedZipFile == null || uploadedZipFile.isEmpty())
    	{
    		return "fileNotChosenPage";
    	}

    	if (uploadedZipFile.getOriginalFilename().endsWith(".zip"))
    	{
    		return handleZipFile(uploadedZipFile, redirectAttributes);
    	}

    	return "attempToUploadNonZipFileError";
    }

	private String handleZipFile(MultipartFile uploadedZipFile, RedirectAttributes redirectAttributes) throws Exception 
	{
		File extractedFolder = null;
		File newLocalZipFile = null;
		try 
		{
			newLocalZipFile = storageService.store(uploadedZipFile);
			extractedFolder = ZipUtility.extractFolder(newLocalZipFile);
			File serverResponseFile = createUniqueFileNameForUser();
			Logger.LogInfo(this.getClass(), "Zip extracted out to: " + newLocalZipFile.getAbsolutePath());
			Logger.LogInfo(this.getClass(), "Server Response file: " + serverResponseFile.getAbsolutePath());
			uploadBulletins(extractedFolder, serverResponseFile);
			redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + uploadedZipFile.getOriginalFilename() + "!");
			redirectAttributes.addFlashAttribute("serverResultsFile", buildLinkFromFileName(serverResponseFile));

			return "redirect:uploadedZipResultsPage";
		}
		catch (ServerNotAvailableException e)
		{
			logException(e);
			return "martusServerNotAvailableErrorPage";
		}
		catch (Exception e)
		{
			logException(e);
			return "exceptionDuringUploadErrorPage";
		}
		finally 
		{
			if (extractedFolder != null)
				postUploadSafeCleanup(extractedFolder);
			
			if (newLocalZipFile != null)
				newLocalZipFile.delete();
		}
	}

	private void postUploadSafeCleanup(File extractedFolder) 
	{
		try 
		{
			FileUtils.deleteDirectory(extractedFolder);
		}
		catch (IOException e) 
		{
			logException(e);
		}
	}

	private String buildLinkFromFileName(File serverResponseFile) 
	{
		UriComponentsBuilder uriCompBuilder = MvcUriComponentsBuilder.fromMethodName(ZipFileUploaderController.class, "serveFile", serverResponseFile.getName().toString());
		
		return uriCompBuilder.build().toString();
	}
    
    @GetMapping("/uploadedZipResultsPage")
    public String uploadedZipResultsPage(Model model) throws IOException 
    {
        return "/uploadedZipResultsPage";
    }

	private void uploadBulletins(File folderWithExtractedMbaFiles, File serverResponseFile) throws Exception 
	{
		ArrayList<File> bulletinMbaFiles = new ArrayList<>();
		File[] mbaFiles = folderWithExtractedMbaFiles.listFiles();
		for (File mbaFile : mbaFiles) 
		{
			Logger.LogInfo(getClass(), "Mba file that will be uploaded: " + mbaFile.getName());
			bulletinMbaFiles.add(mbaFile);
		}
		
		Logger.LogInfo(getClass(), "About to start uploader");
		BulletinUploader uploader = new BulletinUploader(MartusUploaderWebappApplication.getServerIp(), "nomagic", bulletinMbaFiles, serverResponseFile);
		Logger.LogInfo(getClass(), "Uploader object constructed");
		uploader.startLoading();
	}
	
	private File createUniqueFileNameForUser() throws Exception 
	{
		File rootLocation = MartusUploaderWebappApplication.getRootLocation();		
		File martusServerResponseFile = File.createTempFile("MartusServerResponse", ".txt", rootLocation);
		
		return martusServerResponseFile;
	}
	
	private void logException(Exception e) 
	{
		Logger.Log(getClass(), e);
	}
}
