package com.microsoft.azure.springcloudplayground.controller;

import com.microsoft.azure.springcloudplayground.generator.BasicProjectRequest;
import com.microsoft.azure.springcloudplayground.generator.ProjectGenerator;
import com.microsoft.azure.springcloudplayground.generator.ProjectRequest;
import com.microsoft.azure.springcloudplayground.dependency.DependencyMetadataProvider;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadataProvider;
import com.microsoft.azure.springcloudplayground.util.PropertyLoader;
import com.microsoft.azure.springcloudplayground.util.TemplateRenderer;
import com.samskivert.mustache.Mustache;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Tar;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.ZipFileSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Controller
public class MainController extends AbstractPlaygroundController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private final ProjectGenerator projectGenerator;

    public MainController(GeneratorMetadataProvider metadataProvider,
                          TemplateRenderer templateRenderer, ResourceUrlProvider resourceUrlProvider,
                          ProjectGenerator projectGenerator,
                          DependencyMetadataProvider dependencyMetadataProvider) {
        super(metadataProvider, resourceUrlProvider);
        this.projectGenerator = projectGenerator;
    }

    @ModelAttribute
    public BasicProjectRequest projectRequest(
            @RequestHeader Map<String, String> headers) {
        ProjectRequest request = new ProjectRequest();
        request.getParameters().putAll(headers);
        request.initialize(this.metadataProvider.get());
        return request;
    }

    // test method
    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }

    @ModelAttribute("linkTo")
    public Mustache.Lambda linkTo() {
        return (frag, out) -> out.write(this.getLinkTo().apply(frag.execute()));
    }

    private void addBuildInformation(@NonNull Map<String, Object> model) {
        final PropertyLoader loader = new PropertyLoader("/git.properties");
        final String developer = loader.getPropertyValue("git.commit.user.name");
        final String commitId = loader.getPropertyValue("git.commit.id.abbrev");
        final String buildTime = loader.getPropertyValue("git.build.time");
        final String buildInfo = String.format("%s:%s@%s", developer, commitId, buildTime);

        model.put("build.information", buildInfo);
    }

    @RequestMapping(path = "/", produces = "text/html")
    public String home(Map<String, Object> model) {
        this.addBuildInformation(model);
        this.renderHome(model);

        return "home";
    }

    @RequestMapping("/starter.zip")
    @ResponseBody
    public ResponseEntity<byte[]> springZip(BasicProjectRequest basicRequest)
            throws IOException {
        ProjectRequest request = (ProjectRequest) basicRequest;
        File dir = this.projectGenerator.generateProjectStructure(request);

        File download = this.projectGenerator.createDistributionFile(dir, ".zip");

        String wrapperScript = getWrapperScript(request);
        new File(dir, wrapperScript).setExecutable(true);
        Zip zip = new Zip();
        zip.setProject(new Project());
        zip.setDefaultexcludes(false);
        ZipFileSet set = new ZipFileSet();
        set.setDir(dir);
        set.setFileMode("755");
        set.setIncludes(wrapperScript);
        set.setDefaultexcludes(false);
        zip.addFileset(set);
        set = new ZipFileSet();
        set.setDir(dir);
        set.setIncludes("**,");
        set.setExcludes(wrapperScript);
        set.setDefaultexcludes(false);
        zip.addFileset(set);
        zip.setDestFile(download.getCanonicalFile());
        zip.execute();
        return upload(download, dir, generateFileName(request, "zip"), "application/zip");
    }

    @RequestMapping(path = "/starter.tgz", produces = "application/x-compress")
    @ResponseBody
    public ResponseEntity<byte[]> springTgz(BasicProjectRequest basicRequest)
            throws IOException {
        ProjectRequest request = (ProjectRequest) basicRequest;
        File dir = this.projectGenerator.generateProjectStructure(request);

        File download = this.projectGenerator.createDistributionFile(dir, ".tar.gz");

        String wrapperScript = getWrapperScript(request);
        new File(dir, wrapperScript).setExecutable(true);
        Tar zip = new Tar();
        zip.setProject(new Project());
        zip.setDefaultexcludes(false);
        Tar.TarFileSet set = zip.createTarFileSet();
        set.setDir(dir);
        set.setFileMode("755");
        set.setIncludes(wrapperScript);
        set.setDefaultexcludes(false);
        set = zip.createTarFileSet();
        set.setDir(dir);
        set.setIncludes("**,");
        set.setExcludes(wrapperScript);
        set.setDefaultexcludes(false);
        zip.setDestFile(download.getCanonicalFile());
        Tar.TarCompressionMethod method = new Tar.TarCompressionMethod();
        method.setValue("gzip");
        zip.setCompression(method);
        zip.execute();
        return upload(download, dir, generateFileName(request, "tar.gz"),
                "application/x-compress");
    }

    private static String generateFileName(ProjectRequest request, String extension) {
        String tmp = request.getArtifactId().replaceAll(" ", "_");
        try {
            return URLEncoder.encode(tmp, "UTF-8") + "." + extension;
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Cannot encode URL", e);
        }
    }

    private static String getWrapperScript(ProjectRequest request) {
        String script = "gradle".equals(request.getBuild()) ? "gradlew" : "mvnw";
        return request.getBaseDir() != null ? request.getBaseDir() + "/" + script
                : script;
    }

    private ResponseEntity<byte[]> upload(File download, File dir, String fileName,
                                          String contentType) throws IOException {
        byte[] bytes = StreamUtils.copyToByteArray(new FileInputStream(download));
        log.info("Uploading: {} ({} bytes)", download, bytes.length);
        ResponseEntity<byte[]> result = createResponseEntity(bytes, contentType,
                fileName);
        this.projectGenerator.cleanTempFiles(dir);
        return result;
    }

    private ResponseEntity<byte[]> createResponseEntity(byte[] content,
                                                        String contentType, String fileName) {
        String contentDispositionValue = "attachment; filename=\"" + fileName + "\"";
        return ResponseEntity.ok().header("Content-Type", contentType)
                .header("Content-Disposition", contentDispositionValue).body(content);
    }

}