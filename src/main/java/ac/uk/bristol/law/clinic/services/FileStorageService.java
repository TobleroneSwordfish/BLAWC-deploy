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

    //just cuts the filename off a path
    public static String cutFilename(String path)
    {
        int i = path.length() - 1;
        while (path.charAt(i) != '/')
        {
            i--;
        }
        return path.substring(0, i + 1);
    }

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

    //path does not include filename
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

    //todo
    public void deleteFile(String path, String filename)
    {
        if (basePath.resolve(path).resolve(filename).toFile().exists())
        {

        }
        else
        {
            System.out.println();
        }
    }

    public void cloneFile(String path, String filename, String newPath, String newFilename)
    {
        System.out.println("Cloning file " + filename +" from " + path + " to " + newPath);
        try
        {
            path = path.substring(1); //don't question it
            newPath = newPath.substring(1); //they come with "/"s on the front, and .resolve goes full linux and treats that as an absolute path
            File file = basePath.resolve(path).resolve(filename).toFile();
            System.out.println("old file: " + file);
            System.out.println("file exists: " + file.exists());
            System.out.println("file is file: " + file.isFile());
            if (file.exists() && file.isFile())
            {
                Path pathVar = basePath.resolve(newPath).resolve(newFilename);
                basePath.resolve(newPath).toFile().mkdirs();
                File newFile = pathVar.toFile();
                System.out.println("new file: " + newFile);
                OutputStream outstream = new FileOutputStream(newFile);//output stream to desired file
                InputStream instream = new FileInputStream(file); //read from old file
                int c;
                while ((c = instream.read()) != -1) {
                    outstream.write(c);
                }
                outstream.close();
                instream.close();
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
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
