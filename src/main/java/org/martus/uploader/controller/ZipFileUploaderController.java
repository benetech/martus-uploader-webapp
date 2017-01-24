package org.martus.uploader.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class ZipFileUploaderController 
{
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

        return "uploadForm";
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
    	if (uploadedZipFile.isEmpty())
    	{
    		return "fileNotChosenPage";
    	}
    	
        if (uploadedZipFile.getOriginalFilename().endsWith(".zip"))
        {
        	File newLocalZipFile = storageService.store(uploadedZipFile);
            File extractedFolder = ZipUtility.extractFolder(newLocalZipFile);
            File serverResponseFile = createUniqueFileNameForUser();
            Logger.LogInfo(this.getClass(), "Zip extracted out to: " + newLocalZipFile.getAbsolutePath());
            Logger.LogInfo(this.getClass(), "Server Response file: " + serverResponseFile.getAbsolutePath());
            try 
            {
            	uploadBulletins(extractedFolder, serverResponseFile);
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
            
            redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + uploadedZipFile.getOriginalFilename() + "!");
            redirectAttributes.addFlashAttribute("serverResultsFile", buildLinkFromFileName(serverResponseFile));
            
        	return "redirect:uploadedZipResultsPage";
        }
        
        return "attempToUploadNonZipFileError";
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
		
		BulletinUploader uploader = new BulletinUploader(MartusUploaderWebappApplication.getServerIp(), "nomagic", bulletinMbaFiles, serverResponseFile);
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
