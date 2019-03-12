package ac.uk.bristol.law.clinic.services;

import ac.uk.bristol.law.clinic.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService
{
    private Path basePath;

    public File getFile(String pathString)
    {
        Path fullPath = basePath.resolve(pathString);
        return fullPath.toFile();
    }

    public boolean fileExists(String pathString)
    {
        Path fullPath = basePath.resolve(pathString);
        if(Files.isRegularFile(fullPath) ){
            return true;
        }else{
            return false;
        }
    }

    public void storeFile(String pathString, InputStream instream, String fileName) throws IOException
    {
        //directory doesn't exist
        if (!Files.isDirectory(basePath.resolve(pathString)))
        {
            basePath.resolve(pathString).toFile().mkdirs();
        }
        try{
            Path fullPath = basePath.resolve(pathString).resolve(fileName);
            File file = fullPath.toFile();
            int c;
            OutputStream outstream = new FileOutputStream(file);//output stream to desired file
            while ((c = instream.read()) != -1) {
                outstream.write(c);
            }
            outstream.close();//leave instream open for caller to close.
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            throw new IOException(e);
        }
    }

    //retrieves the base path from config and creates it if it doesn't exist
    public FileStorageService(@Autowired StorageProperties storageProperties) throws IOException
    {
        File baseFile = Paths.get(storageProperties.getBasePath()).toFile();
        if (!baseFile.isDirectory())
        {
            if (!baseFile.mkdirs())
            {
                throw new IOException("Could not create base storage directory for some reason");
            }
        }
        basePath = baseFile.toPath();
    }
}
