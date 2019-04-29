package ac.uk.bristol.law.clinic;

import ac.uk.bristol.law.clinic.services.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LawClinicApplicationTests {

	@Autowired
	StorageProperties storagePropeties;

	@Test
	public void contextLoads() {
	}

	@Test
	public void storeFile() throws IOException {
		FileStorageService fileStore = new FileStorageService(storagePropeties);
		String path = "testdest/folder/";
		String filename = "testFile";
		String testData = "some test data for my input stream";

		InputStream inputStream = IOUtils.toInputStream(testData, "UTF-8");
		fileStore.storeFile(path, inputStream, filename);
		inputStream.close();
		assert (fileStore.fileExists(path + filename));


		String newPath = "testdest/folder2/";
		fileStore.cloneFile(path, filename, newPath, filename);
		assert(fileStore.fileExists(newPath + filename));

		fileStore.deleteFile(path, filename);
		assert (!fileStore.fileExists(path + filename));
	}

	@Test
	public void cutFilenameTest() throws IOException {
		System.out.println(FileStorageService.cutFilename("/testdest/folder/testFile"));
		assert (FileStorageService.cutFilename("/testdest/folder/testFile").equals("/testdest/folder/"));
	}
}
