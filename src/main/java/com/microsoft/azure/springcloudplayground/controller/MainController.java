package com.microsoft.azure.springcloudplayground.controller;

import com.microsoft.azure.springcloudplayground.exception.GithubProcessException;
import com.microsoft.azure.springcloudplayground.generator.MicroService;
import com.microsoft.azure.springcloudplayground.generator.ProjectGenerator;
import com.microsoft.azure.springcloudplayground.generator.ProjectRequest;
import com.microsoft.azure.springcloudplayground.github.GithubOperator;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadataProvider;
import com.microsoft.azure.springcloudplayground.util.PropertyLoader;
import com.microsoft.azure.springcloudplayground.util.TelemetryProxy;
import com.samskivert.mustache.Mustache;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.ZipFileSet;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

@Controller
@Slf4j
public class MainController extends AbstractPlaygroundController {

    private final TelemetryProxy telemetryProxy;
    private final ProjectGenerator projectGenerator;
    private final OAuth2AuthorizedClientService clientService;

    private static final String TELEMETRY_EVENT_ACCESS = "playground-access";
    private static final String TELEMETRY_EVENT_GENERATE = "playground-generate";
    private static final String TELEMETRY_EVENT_GITHUB_PUSH = "playground-github-push";
    private static final String GREETING_HTML = "greeting";

    public MainController(GeneratorMetadataProvider metadataProvider, ResourceUrlProvider resourceUrlProvider,
                          ProjectGenerator projectGenerator, OAuth2AuthorizedClientService clientService) {
        super(metadataProvider, resourceUrlProvider);
        this.projectGenerator = projectGenerator;
        this.telemetryProxy = new TelemetryProxy();
        this.clientService = clientService;
    }

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) {
        model.addAttribute("name", name);

        return GREETING_HTML;
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

    private void triggerGenerateEvent(@NonNull List<MicroService> services) {
        final Map<String, String> properties = new HashMap<>();

        services.forEach(s -> properties.put(s.getName(), "selected"));

        this.telemetryProxy.trackEvent(TELEMETRY_EVENT_GENERATE, properties);
    }

    private void triggerAccessEvent() {
        final Map<String, String> properties = new HashMap<>();

        this.telemetryProxy.trackEvent(TELEMETRY_EVENT_ACCESS, properties);
    }

    private void triggerGithubPushEventSuccess(@NonNull String username) {
        Map<String, String> properties = new HashMap<>();

        properties.put("uniqueId", sha256Hex(username));
        properties.put("success", "true");

        this.telemetryProxy.trackEvent(TELEMETRY_EVENT_GITHUB_PUSH, properties);
    }

    private void triggerGithubPushEventFailure(@NonNull String username, @NonNull String reason) {
        Map<String, String> properties = new HashMap<>();

        properties.put("uniqueId", sha256Hex(username));
        properties.put("success", "false");
        properties.put("reason", reason);

        this.telemetryProxy.trackEvent(TELEMETRY_EVENT_GITHUB_PUSH, properties);
    }

    private boolean isValidOAuth2Token(OAuth2AuthenticationToken token) {
        if (token == null) {
            return false;
        } else if (StringUtils.isEmpty(token.getName())) {
            return false;
        } else {
            return true;
        }
    }

    @RequestMapping(path = "/", produces = "text/html")
    public String home(Map<String, Object> model, OAuth2AuthenticationToken token) {
        if (isValidOAuth2Token(token)) {
            model.put("loggedInUser", token.getPrincipal().getAttributes().get("login"));
        }

        this.addBuildInformation(model);
        this.renderHome(model);
        this.triggerAccessEvent();

        return "home";
    }

    @PostMapping("/push-to-github")
    public ResponseEntity pushToGithub(@RequestBody @Nonnull ProjectRequest request, OAuth2AuthenticationToken token) {
        log.info("Project request received: " + request);

        if (isValidOAuth2Token(token)) {
            String username = token.getPrincipal().getAttributes().get("login").toString();
            GithubOperator operator = new GithubOperator(username, getAccessToken().getTokenValue());
            File dir = this.projectGenerator.generate(request);
            triggerGenerateEvent(request.getMicroServices());

            try {
                String repositoryUrl = operator.createRepository(dir, request.getRepoName());
                triggerGithubPushEventSuccess(username);

                return ResponseEntity.created(URI.create(repositoryUrl)).build();
            } catch (GithubProcessException e) {
                triggerGithubPushEventFailure(username, e.getMessage());

                return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
            }
        }

        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }

    @ResponseBody
    @PostMapping("/project.zip")
    public ResponseEntity<byte[]> getZipProject(@RequestBody @NonNull ProjectRequest request) throws IOException {
        File dir = this.projectGenerator.generate(request);
        File download = this.projectGenerator.createDistributionFile(dir, ".zip");

        this.triggerGenerateEvent(request.getMicroServices());

        Zip zip = new Zip();
        zip.setProject(new Project());
        zip.setDefaultexcludes(false);

        ZipFileSet set = new ZipFileSet();
        set.setDir(dir);
        set.setFileMode("755");
        set.setDefaultexcludes(false);

        zip.addFileset(set);
        zip.setDestFile(download.getCanonicalFile());
        zip.execute();

        return upload(download, dir, generateFileName(request, "zip"), "application/zip");
    }

    private static String generateFileName(ProjectRequest request, String extension) {
        String tmp = request.getArtifactId().replaceAll(" ", "_");

        try {
            return URLEncoder.encode(tmp, "UTF-8") + "." + extension;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Cannot encode URL", e);
        }
    }

    private ResponseEntity<byte[]> upload(File download, File dir, String fileName, String type) throws IOException {
        byte[] bytes = StreamUtils.copyToByteArray(new FileInputStream(download));
        log.info("Uploading: {} ({} bytes)", download, bytes.length);

        this.projectGenerator.cleanTempFiles(dir);

        return createResponseEntity(bytes, type, fileName);
    }

    private ResponseEntity<byte[]> createResponseEntity(byte[] content, String contentType, String fileName) {
        String contentDispositionValue = "attachment; filename=\"" + fileName + "\"";
        return ResponseEntity.ok().header("Content-Type", contentType)
                .header("Content-Disposition", contentDispositionValue).body(content);
    }

    private OAuth2AccessToken getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());

        return client.getAccessToken();
    }
}
