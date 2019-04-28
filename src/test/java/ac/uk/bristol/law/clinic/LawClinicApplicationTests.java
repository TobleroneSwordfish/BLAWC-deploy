package ac.uk.bristol.law.clinic;

import ac.uk.bristol.law.clinic.services.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

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
		fileStore.storeFile("testdest/folder", IOUtils.toInputStream("some test data for my input stream", "UTF-8"), "testFile");
		assert (fileStore.fileExists("testdest/folder/testFile"));
	}

	@Test
	public void cutFilenameTest() throws IOException {
		System.out.println(FileStorageService.cutFilename("/testdest/folder/testFile"));
		assert (FileStorageService.cutFilename("/testdest/folder/testFile").equals("/testdest/folder/"));
	}
}
