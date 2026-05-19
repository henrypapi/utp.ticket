package com.pe.limon.api.core.utils.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public interface IFileStorage {

   

    String saveFile(MultipartFile file, String fileName,String directory) throws IOException;

    Resource getFile(String fileName, String directory) throws MalformedURLException;

    boolean isValidFile(MultipartFile file, List<String> allowedExtensions);
}
