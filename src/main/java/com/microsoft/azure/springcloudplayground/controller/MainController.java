package com.microsoft.azure.springcloudplayground.controller;

import com.microsoft.azure.springcloudplayground.dependency.DependencyMetadataProvider;
import com.microsoft.azure.springcloudplayground.generator.BasicProjectRequest;
import com.microsoft.azure.springcloudplayground.generator.ProjectGenerator;
import com.microsoft.azure.springcloudplayground.generator.ProjectRequest;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadataProvider;
import com.microsoft.azure.springcloudplayground.util.PropertyLoader;
import com.microsoft.azure.springcloudplayground.util.TelemetryProxy;
import com.microsoft.azure.springcloudplayground.util.TemplateRenderer;
import com.samskivert.mustache.Mustache;
import lombok.extern.slf4j.Slf4j;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Tar;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.ZipFileSet;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.applicationinsights.core.dependencies.apachecommons.codec.digest.DigestUtils.sha256Hex;

@Controller
@Slf4j
public class MainController extends AbstractPlaygroundController {

    private final ProjectGenerator projectGenerator;
    private final TelemetryProxy telemetryProxy;
    private static final String TELEMETRY_EVENT_ACCESS = "SpringCloudPlaygroundAccess";
    private static final String TELEMETRY_EVENT_GENERATE = "SpringCloudPlaygroundGenerate";
    private static final String TELEMETRY_EVENT_LOGIN = "SpringCloudPlaygroundLogin";
    private static final String FREE_ACCOUNT = "FreeAccount";
    private static final String LOGIN_ACCOUNT = "LoginAccount";
    private static final String GREETING_HTML = "greeting";

    public MainController(GeneratorMetadataProvider metadataProvider,
                          TemplateRenderer templateRenderer, ResourceUrlProvider resourceUrlProvider,
                          ProjectGenerator projectGenerator,
                          DependencyMetadataProvider dependencyMetadataProvider) {
        super(metadataProvider, resourceUrlProvider);
        this.projectGenerator = projectGenerator;
        this.telemetryProxy = new TelemetryProxy();
    }

    @ModelAttribute
    public BasicProjectRequest projectRequest(@RequestHeader Map<String, String> headers) {
        ProjectRequest request = new ProjectRequest();

        request.getParameters().putAll(headers);
        request.initialize(this.metadataProvider.get());

        return request;
    }

    // test method
    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);

        return GREETING_HTML;
    }

    @GetMapping("/free-account")
    public String freeAccount(Model model, HttpServletRequest request) {
        this.triggerLoginEvent(FREE_ACCOUNT, sha256Hex(request.getRemoteAddr()));

        return this.greeting(FREE_ACCOUNT, model);
    }

    @GetMapping("/login-account")
    public String loginAccount(Model model, HttpServletRequest request) {
        this.triggerLoginEvent(LOGIN_ACCOUNT, sha256Hex(request.getRemoteAddr()));

        return this.greeting(LOGIN_ACCOUNT, model);
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

    private void triggerGenerateEvent(@NonNull List<String> services, @NonNull String accessId) {
        final Map<String, String> properties = new HashMap<>();

        properties.put("accessId", accessId);
        services.forEach(s -> properties.put(s, "selected"));

        this.telemetryProxy.trackEvent(TELEMETRY_EVENT_GENERATE, properties);
    }

    private void triggerAccessEvent(@NonNull String accessId) {
        final Map<String, String> properties = new HashMap<>();

        properties.put("accessId", accessId);

        this.telemetryProxy.trackEvent(TELEMETRY_EVENT_ACCESS, properties);
    }

    private void triggerLoginEvent(@NonNull String accountType, @NonNull String accessId) {
        final Map<String, String> properties = new HashMap<>();

        properties.put("accessId", accessId);
        properties.put("accountType", accountType);

        this.telemetryProxy.trackEvent(TELEMETRY_EVENT_LOGIN, properties);
    }

    @RequestMapping(path = "/", produces = "text/html")
    public String home(Map<String, Object> model, HttpServletRequest request) {

        this.addBuildInformation(model);
        this.renderHome(model);
        this.triggerAccessEvent(sha256Hex(request.getRemoteAddr()));

        return "home";
    }

    @RequestMapping("/microservice.zip")
    @ResponseBody
    public ResponseEntity<byte[]> springZip(BasicProjectRequest basicRequest, HttpServletRequest httpRequest)
            throws IOException {
        ProjectRequest request = (ProjectRequest) basicRequest;
        File dir = this.projectGenerator.generateProjectStructure(request);
        File download = this.projectGenerator.createDistributionFile(dir, ".zip");
        String wrapperScript = getWrapperScript(request);

        this.triggerGenerateEvent(request.getServices(), sha256Hex(httpRequest.getRemoteAddr()));
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

    @RequestMapping(path = "/microservice.tgz", produces = "application/x-compress")
    @ResponseBody
    public ResponseEntity<byte[]> springTgz(BasicProjectRequest basicRequest, HttpServletRequest httpRequest)
            throws IOException {
        ProjectRequest request = (ProjectRequest) basicRequest;
        File dir = this.projectGenerator.generateProjectStructure(request);
        File download = this.projectGenerator.createDistributionFile(dir, ".tar.gz");
        String wrapperScript = getWrapperScript(request);

        this.triggerGenerateEvent(request.getServices(), sha256Hex(httpRequest.getRemoteAddr()));
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

        return upload(download, dir, generateFileName(request, "tar.gz"), "application/x-compress");
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
        return request.getBaseDir() != null ? request.getBaseDir() + "/" + script : script;
    }

    private ResponseEntity<byte[]> upload(File download, File dir, String fileName,
                                          String contentType) throws IOException {
        byte[] bytes = StreamUtils.copyToByteArray(new FileInputStream(download));
        log.info("Uploading: {} ({} bytes)", download, bytes.length);

        this.projectGenerator.cleanTempFiles(dir);

        return createResponseEntity(bytes, contentType, fileName);
    }

    private ResponseEntity<byte[]> createResponseEntity(byte[] content, String contentType, String fileName) {
        String contentDispositionValue = "attachment; filename=\"" + fileName + "\"";
        return ResponseEntity.ok().header("Content-Type", contentType)
                .header("Content-Disposition", contentDispositionValue).body(content);
    }
}
