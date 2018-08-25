package com.microsoft.azure.springcloudplayground;

import com.microsoft.azure.springcloudplayground.generator.MicroService;
import com.microsoft.azure.springcloudplayground.generator.ProjectGenerator;
import com.microsoft.azure.springcloudplayground.generator.ProjectRequest;
import com.microsoft.azure.springcloudplayground.module.ModuleNames;
import com.microsoft.azure.springcloudplayground.service.ServiceNames;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.ZipFileSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.NonNull;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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

    private static final String REFERENCE_FILE_NAME = "demo-reference.zip";

    private File zipFile;

    @Autowired
    private ProjectGenerator generator;

    @Before
    public void setup() {
        PROJECT_REQUEST.setName("demo");
        PROJECT_REQUEST.setGroupId("com.example");
        PROJECT_REQUEST.setArtifactId("demo");
        PROJECT_REQUEST.setPackageName("com.example");
        PROJECT_REQUEST.setBaseDir("demo");
        PROJECT_REQUEST.setDescription("Demo project for Spring Cloud Azure");

        PROJECT_REQUEST.setMicroServices(Arrays.asList(CLOUD_CONFIG_SERVER, CLOUD_GATEWAY,
                CLOUD_EUREKA_SERVER, CLOUD_HYSTRIX_DASHBOARD, AZURE_MESSAGE, AZURE_STORAGE));

        zipFile = toZipFile(generator.generate(PROJECT_REQUEST));
    }

    @After
    public void cleanup() {
        zipFile.delete();
    }

    @SneakyThrows
    private File toZipFile(@NonNull File dir) {
        File zipFile = this.generator.createDistributionFile(dir, ".zip");

        Zip zip = new Zip();
        zip.setProject(new Project());
        zip.setDefaultexcludes(false);

        ZipFileSet set = new ZipFileSet();
        set.setDir(dir);
        set.setFileMode("755");
        set.setDefaultexcludes(false);

        zip.addFileset(set);
        zip.setDestFile(zipFile.getCanonicalFile());
        zip.execute();

        return zipFile;
    }

    @Test
    public void testProjectGeneration() {
        String zipSha256Hex = getSha256Hex(zipFile);
        String referenceSha256Hex = getSha256Hex(getClass().getClassLoader().getResourceAsStream(REFERENCE_FILE_NAME));

        Assert.assertEquals(zipSha256Hex, referenceSha256Hex);
    }

    private String getSha256Hex(@NonNull File file) {
        try (InputStream zipInputStream = new FileInputStream(file)) {
            return DigestUtils.sha256Hex(zipInputStream);
        } catch (IOException omit) {
            // ignore this IOException
            return "";
        }
    }

    private String getSha256Hex(@NonNull InputStream inputStream) {
        try {
            return DigestUtils.sha256Hex(inputStream);
        } catch (IOException omit) {
            // ignore this IOException
            return "";
        }
    }
}
