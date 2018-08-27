package com.microsoft.azure.springcloudplayground;

import com.microsoft.azure.springcloudplayground.generator.MicroService;
import com.microsoft.azure.springcloudplayground.generator.ProjectGenerator;
import com.microsoft.azure.springcloudplayground.generator.ProjectRequest;
import com.microsoft.azure.springcloudplayground.module.ModuleNames;
import com.microsoft.azure.springcloudplayground.service.ServiceNames;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.NonNull;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringCloudPlaygroundApplicationTests {

    private static final MicroService CLOUD_CONFIG_SERVER = new MicroService(ServiceNames.CLOUD_CONFIG_SERVER,
            Arrays.asList(ModuleNames.CLOUD_CONFIG_SERVER), 8888);

    private static final MicroService CLOUD_GATEWAY = new MicroService(ServiceNames.CLOUD_GATEWAY,
            Arrays.asList(ModuleNames.CLOUD_GATEWAY), 9999);

    private static final MicroService CLOUD_EUREKA_SERVER = new MicroService(ServiceNames.CLOUD_EUREKA_SERVER,
            Arrays.asList(ModuleNames.CLOUD_EUREKA_SERVER), 8761);

    private static final MicroService CLOUD_HYSTRIX_DASHBOARD = new MicroService(ServiceNames.CLOUD_HYSTRIX_DASHBOARD,
            Arrays.asList(ModuleNames.CLOUD_HYSTRIX_DASHBOARD), 7979);

    private static final MicroService AZURE_MESSAGE = new MicroService("azure-message",
            Arrays.asList(ModuleNames.AZURE_EVNET_HUB_BINDER, ModuleNames.AZURE_CACHE), 8999);

    private static final MicroService AZURE_STORAGE = new MicroService("azure-storage",
            Arrays.asList(ModuleNames.AZURE_CACHE, ModuleNames.AZURE_STORAGE, ModuleNames.AZURE_SQL_SERVER), 9000);

    private static final ProjectRequest PROJECT_REQUEST = new ProjectRequest();

    private static final String PROJECT_BASE_DIR = "demo";

    private static final String REFERENCE_FILE_NAME = "demo-reference.zip";

    private File project;

    private File referenceDir;

    private File referenceZip;

    private File referenceProject;

    @Autowired
    private ProjectGenerator generator;

    @Before
    public void setup() {
        PROJECT_REQUEST.setName("demo");
        PROJECT_REQUEST.setGroupId("com.example");
        PROJECT_REQUEST.setArtifactId("demo");
        PROJECT_REQUEST.setPackageName("com.example.demo");
        PROJECT_REQUEST.setBaseDir(PROJECT_BASE_DIR);
        PROJECT_REQUEST.setDescription("Demo project for Spring Cloud Azure");

        PROJECT_REQUEST.setMicroServices(Arrays.asList(CLOUD_CONFIG_SERVER, CLOUD_GATEWAY,
                CLOUD_EUREKA_SERVER, CLOUD_HYSTRIX_DASHBOARD, AZURE_MESSAGE, AZURE_STORAGE));

        project = generator.generate(PROJECT_REQUEST);
        referenceDir = new File("reference");

        FileUtils.deleteQuietly(referenceDir);

        referenceZip = new File(referenceDir, "reference.zip");
        referenceProject = new File(referenceDir, PROJECT_BASE_DIR);
        referenceDir.mkdir();
    }

    @After
    public void cleanup() {
        FileUtils.deleteQuietly(referenceDir);
    }

    private void inputStreamToFile(@NonNull File file, @NonNull InputStream inputStream) {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            int count;
            byte[] buffer = new byte[8192];

            while ((count = inputStream.read(buffer, 0, 8192)) != -1) {
                outputStream.write(buffer, 0, count);
            }
        } catch (IOException omit) {
            // Ignore this exception
        }
    }

    private void initializeReferenceProject() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(REFERENCE_FILE_NAME)) {
            inputStreamToFile(referenceZip, inputStream);
            ZipFile zipFile = new ZipFile(referenceZip);

            zipFile.extractAll(referenceDir.getName());
        } catch (ZipException | IOException omit) {
            // Ignore this exception
        }
    }

    private void assertFileEquals(@NonNull File target, @NonNull File reference) {
        try (BufferedReader targetReader = new BufferedReader(new FileReader(target));
             BufferedReader referenceReader = new BufferedReader(new FileReader(reference))) {
            String targetLine = targetReader.readLine();
            String referenceLine = referenceReader.readLine();

            while (targetLine != null && referenceLine != null) {
                Assert.assertEquals(targetLine, referenceLine);

                targetLine = targetReader.readLine();
                referenceLine = referenceReader.readLine();
            }

            Assert.assertNull(targetLine);
            Assert.assertNull(referenceLine);
        } catch (IOException omit) {
            // Ignore this exception
        }
    }

    private void assertProjectEquals(@NonNull File target, @NonNull File reference) {
        List<File> targetFiles = new ArrayList<>(FileUtils.listFiles(target, null, true));
        List<File> referenceFiles = new ArrayList<>(FileUtils.listFiles(reference, null, true));

        Assert.assertEquals(targetFiles.size(), referenceFiles.size());

        for (int i = 0; i < targetFiles.size(); i++) {
            assertFileEquals(targetFiles.get(i), referenceFiles.get(i));
        }
    }

    @Test
    @Ignore
    public void testProjectGeneration() {
        initializeReferenceProject();

        assertProjectEquals(this.project, referenceProject);
    }
}
