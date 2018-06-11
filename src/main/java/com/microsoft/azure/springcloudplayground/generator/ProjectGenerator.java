package com.microsoft.azure.springcloudplayground.generator;

import com.microsoft.azure.springcloudplayground.dependency.Dependency;
import com.microsoft.azure.springcloudplayground.exception.GeneratorException;
import com.microsoft.azure.springcloudplayground.metadata.*;
import com.microsoft.azure.springcloudplayground.service.ConfigurableService;
import com.microsoft.azure.springcloudplayground.service.Service;
import com.microsoft.azure.springcloudplayground.util.TemplateRenderer;
import com.microsoft.azure.springcloudplayground.util.Version;
import com.microsoft.azure.springcloudplayground.util.VersionProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectGenerator {

    private static final Logger log = LoggerFactory.getLogger(ProjectGenerator.class);

    private static final Version VERSION_1_2_0_RC1 = Version.parse("1.2.0.RC1");

    private static final Version VERSION_1_3_0_M1 = Version.parse("1.3.0.M1");

    private static final Version VERSION_1_4_0_M2 = Version.parse("1.4.0.M2");

    private static final Version VERSION_1_4_0_M3 = Version.parse("1.4.0.M3");

    private static final Version VERSION_1_4_2_M1 = Version.parse("1.4.2.M1");

    private static final Version VERSION_1_5_0_M1 = Version.parse("1.5.0.M1");

    private static final Version VERSION_2_0_0_M1 = Version.parse("2.0.0.M1");

    private static final Version VERSION_2_0_0_M3 = Version.parse("2.0.0.M3");

    private static final Version VERSION_2_0_0_M6 = Version.parse("2.0.0.M6");

    private static final String KUBERNETES_FILE = "kubernetes.yaml";
    private static final String DOCKER_PATH = "docker";

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private GeneratorMetadataProvider metadataProvider;

    @Autowired
    private ProjectRequestResolver requestResolver;

    @Autowired
    private TemplateRenderer templateRenderer = new TemplateRenderer();

    @Autowired
    private ProjectResourceLocator projectResourceLocator = new ProjectResourceLocator();

    @Value("${TMPDIR:.}/playground")
    private String tmpdir;

    private File temporaryDirectory;

    private transient Map<String, List<File>> temporaryFiles = new LinkedHashMap<>();

    public GeneratorMetadataProvider getMetadataProvider() {
        return this.metadataProvider;
    }

    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void setMetadataProvider(GeneratorMetadataProvider metadataProvider) {
        this.metadataProvider = metadataProvider;
    }

    public void setRequestResolver(ProjectRequestResolver requestResolver) {
        this.requestResolver = requestResolver;
    }

    public void setTemplateRenderer(TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    public void setProjectResourceLocator(ProjectResourceLocator projectResourceLocator) {
        this.projectResourceLocator = projectResourceLocator;
    }

    public void setTmpdir(String tmpdir) {
        this.tmpdir = tmpdir;
    }

    public void setTemporaryDirectory(File temporaryDirectory) {
        this.temporaryDirectory = temporaryDirectory;
    }

    public void setTemporaryFiles(Map<String, List<File>> temporaryFiles) {
        this.temporaryFiles = temporaryFiles;
    }

    private void writeKubernetesFile(File dir, ProjectRequest request){
        List<Service> services = ServiceResolver.resolve(request.getServices());
        Map<String, Object> model = new HashMap<>();
        model.put("services", services);
        writeText(new File(dir, KUBERNETES_FILE), templateRenderer.process(Paths.get(DOCKER_PATH, KUBERNETES_FILE).toString(), model));
    }

    private void generateDockerStructure(@NonNull File rootDir, @NonNull String baseDir, Map<String, Boolean> modulesModel, ProjectRequest request) {
        final File dockerDir = Paths.get(rootDir.getPath(),baseDir, "docker").toFile();
        final String template = "docker";
        final String dockerComposeName = "docker-compose.yml";
        final String runBash = "run.sh";
        final String runCmd = "run.cmd";
        final String readMe = "README.md";

        dockerDir.mkdir();

        writeText(new File(dockerDir, dockerComposeName), templateRenderer.process(Paths.get(template, dockerComposeName).toString(), modulesModel));
        writeText(new File(dockerDir, runBash), templateRenderer.process(Paths.get(template, runBash).toString(), null));
        writeText(new File(dockerDir, runCmd), templateRenderer.process(Paths.get(template, runCmd).toString(), null));
        writeText(new File(dockerDir, readMe), templateRenderer.process(Paths.get(template, readMe).toString(), null));
        writeKubernetesFile(dockerDir, request);
    }

    /**
     * Generate a project structure for the specified {@link ProjectRequest}. Returns a
     * directory containing the project.
     * @param request the project request
     * @return the generated project structure
     */
    public File generateProjectStructure(ProjectRequest request) {
        try {
            Map<String, Object> model = resolveModel(request);
            File rootDir = generateProjectStructure(request, model);

            final Map<String, Boolean> modulesModel = new HashMap<>();
            for(ProjectRequest subModule: request.getModules()){
                generateProjectModuleStructure(subModule, resolveModel(subModule),
                        new File(rootDir, request.getBaseDir()), request);
                modulesModel.put(subModule.getName(), Boolean.TRUE);
            }

            generateDockerStructure(rootDir, request.getBaseDir(), modulesModel, request);
            //publishProjectGeneratedEvent(request);

            return rootDir;
        }
        catch (GeneratorException ex) {
            //publishProjectFailedEvent(request, ex);
            throw ex;
        }
    }

    /**
     * Generate a project structure for the specified {@link ProjectRequest} and resolved
     * model.
     * @param request the project request
     * @param model the source model
     * @return the generated project structure
     */
    protected File generateProjectStructure(ProjectRequest request,
                                            Map<String, Object> model) {
        File rootDir;
        try {
            rootDir = File.createTempFile("tmp", "", getTemporaryDirectory());
        }
        catch (IOException e) {
            throw new IllegalStateException("Cannot create temp dir", e);
        }
        addTempFile(rootDir.getName(), rootDir);
        rootDir.delete();
        rootDir.mkdirs();

        File dir = initializerProjectDir(rootDir, request);

        if (isGradleBuild(request)) {
            String gradle = new String(doGenerateGradleBuild(model));
            writeText(new File(dir, "build.gradle"), gradle);
            String settings = new String(doGenerateGradleSettings(model));
            writeText(new File(dir, "settings.gradle"), settings);
            writeGradleWrapper(dir, Version.safeParse(request.getBootVersion()));
        }
        else {
            String pom = new String(doGenerateMavenPom(model, "parent-pom.xml"));
            writeText(new File(dir, "pom.xml"), pom);
            writeMavenWrapper(dir);
        }

        generateGitIgnore(dir, request);

        return rootDir;
    }

    /**
     * Generate a module structure for the specified {@link ProjectRequest} and resolved
     * model.
     * @param request the project request
     * @param model the source model
     * @return the generated project structure
     */
    protected File generateProjectModuleStructure(ProjectRequest request,
                                                  Map<String, Object> model, File rootDir, ProjectRequest parentModule) {
        File dir = initializerProjectDir(rootDir, request);

        if (isGradleBuild(request)) {
            String gradle = new String(doGenerateGradleBuild(model));
            writeText(new File(dir, "build.gradle"), gradle);
            String settings = new String(doGenerateGradleSettings(model));
            writeText(new File(dir, "settings.gradle"), settings);
        }
        else {
            final String dockerFileName = "Dockerfile";
            final String exposePortVariable = "EXPOSE_PORT";
            final String dockerTemplateDir = "docker";
            final Map<String, Integer> portModel = new HashMap<>();
            portModel.put(exposePortVariable, ServiceMetadata.portMap.get(request.getName()));

            // Write Dockerfile to module
            log.info("Writing Dockerfile to module " + request.getName());
            writeText(new File(dir, dockerFileName),
                    templateRenderer.process(Paths.get(dockerTemplateDir, dockerFileName).toString(), portModel));

            // Write pom file
            String pom = new String(doGenerateMavenPom(model, "module-pom.xml"));
            writeText(new File(dir, "pom.xml"), pom);
        }

        String applicationName = request.getApplicationName();
        String language = request.getLanguage();

        File src = new File(new File(dir, "src/main/" + language),
                request.getPackageName().replace(".", "/"));
        src.mkdirs();
        if(request.getName().equalsIgnoreCase("cloud-hystrix-dashboard")){
            write(new File(src, applicationName + "." + language),
                    "HystrixDashboardApplication." + language, model);
            write(new File(src, "MockStreamServlet.java"),
                    "MockStreamServlet.java", model);
        } else {
            write(new File(src, applicationName + "." + language),
                    "Application." + language, model);
        }

        if (request.getDependencies().contains("web")) {
            write(new File(src, "Controller." + language), "Controller.java" , model);
        }

        // Write index page if is gateway module
        if(ModulePropertiesResolver.isGatewayModule(request.getName())) {
            // Only java supported, WebFilter for root request mapping
            write(new File(src, "CustomWebFilter.java"),"CustomWebFilter.java", model);

            File resourceFolder = new File(dir, "src/main/resources/static");
            resourceFolder.mkdirs();

            writeText(new File(resourceFolder, "index.html"),
                    templateRenderer.process("GatewayIndex.tmpl", ServiceMetadata.getLinksMap(parentModule)));
            writeText(new File(resourceFolder, "bulma.min.css"), templateRenderer.process("bulma.min.css",null));
        }

        File test = new File(new File(dir, "src/test/" + language),
                request.getPackageName().replace(".", "/"));
        test.mkdirs();
        setupTestModel(request, model);
        write(new File(test, applicationName + "Tests." + language),
                "ApplicationTests." + language, model);

        File resources = new File(dir, "src/main/resources");
        resources.mkdirs();
        writePropertiesFile(request, resources, parentModule);

        if(request.getName().equalsIgnoreCase("cloud-hystrix-dashboard")){
            writeTextResource(resources, "hystrix.stream", "hystrix.stream");
        }

        if (request.hasWebFacet()) {
            new File(dir, "src/main/resources/templates").mkdirs();
            new File(dir, "src/main/resources/static").mkdirs();
        }
        return rootDir;
    }

    private void writePropertiesFile(ProjectRequest request, File resourceDir, ProjectRequest parentRequest) {
        if(ModulePropertiesResolver.isConfigServer(request.getName())) {
            // Write bootstap.yml
            writeBootstrapYaml(request, resourceDir);

            // Write shared property files for all modules in config server
            File sharedPropFolder = new File(resourceDir, "shared");
            sharedPropFolder.mkdirs();

            // Write common properties among all microservices to application.yml
            String applicationYmlTemplate = ModulePropertiesResolver.getSharedCommonPropTemplate();
            String applicationYmlContent = templateRenderer.process(applicationYmlTemplate, null);
            writeText(new File(sharedPropFolder, "application.yml"), applicationYmlContent);

            // Write other microservice modules' yaml to shared folder
            // How many properties file will be written is decided by parent request
            List<ConfigurableService> azureServices = parentRequest.getModules().stream()
                    .filter(module -> !ModulePropertiesResolver.isInfraModule(module.getName()))
                    .map(module -> new ConfigurableService(module.getApplicationName(), "0"))
                    .collect(Collectors.toList());

            for(ProjectRequest module : parentRequest.getModules()) {
                String templateFile = ModulePropertiesResolver.getSharedPropTemplate(module.getName());
                Map<String, Object> model = new HashMap<>();

                String moduleName = module.getName();
                if(ModulePropertiesResolver.isConfigServer(moduleName)) {
                    // No shared properties file is required to be generated for config server itself
                    continue;
                }

                if(ModulePropertiesResolver.isGatewayModule(moduleName)) {
                    model.put("services", azureServices);
                } else if(!ModulePropertiesResolver.isInfraModule(moduleName)){
                    model.put("applicationName", module.getName());
                    model.put("port", getPort(module.getName()));
                }

                String content = templateRenderer.process(templateFile, model);
                writeText(new File(sharedPropFolder,  module.getName() + ".yml"), content);
            }
        } else {
            // Write bootstrap.yml
            writeBootstrapYaml(request, resourceDir);
        }
    }

    private void writeBootstrapYaml(ProjectRequest request, File resourceDir) {
        Map<String, String> model = new HashMap<>();
        model.put("applicationName", request.getName());

        String templateFile = ModulePropertiesResolver.getBootstrapTemplate(request.getName());
        String content = templateRenderer.process(templateFile, model);
        writeText(new File(resourceDir, "bootstrap.yml"), content);
    }

    private int getPort(String serviceName) {
        return ServiceMetadata.portMap.get(serviceName);
    }

    /**
     * Create a distribution file for the specified project structure directory and
     * extension.
     * @param dir the directory
     * @param extension the file extension
     * @return the distribution file
     */
    public File createDistributionFile(File dir, String extension) {
        File download = new File(getTemporaryDirectory(), dir.getName() + extension);
        addTempFile(dir.getName(), download);
        return download;
    }

    private File getTemporaryDirectory() {
        if (this.temporaryDirectory == null) {
            this.temporaryDirectory = new File(this.tmpdir, "playground");
            this.temporaryDirectory.mkdirs();
        }
        return this.temporaryDirectory;
    }

    /**
     * Clean all the temporary files that are related to this root directory.
     * @param dir the directory to clean
     * @see #createDistributionFile
     */
    public void cleanTempFiles(File dir) {
        List<File> tempFiles = this.temporaryFiles.remove(dir.getName());
        if (!tempFiles.isEmpty()) {
            tempFiles.forEach((File file) -> {
                if (file.isDirectory()) {
                    FileSystemUtils.deleteRecursively(file);
                }
                else if (file.exists()) {
                    file.delete();
                }
            });
        }
    }
/*
    private void publishProjectGeneratedEvent(ProjectRequest request) {
        ProjectGeneratedEvent event = new ProjectGeneratedEvent(request);
        this.eventPublisher.publishEvent(event);
    }

    private void publishProjectFailedEvent(ProjectRequest request, Exception cause) {
        ProjectFailedEvent event = new ProjectFailedEvent(request, cause);
        this.eventPublisher.publishEvent(event);
    }
*/
    /**
     * Generate a {@code .gitignore} file for the specified {@link ProjectRequest}.
     * @param dir the root directory of the project
     * @param request the request to handle
     */
    protected void generateGitIgnore(File dir, ProjectRequest request) {
        Map<String, Object> model = new LinkedHashMap<>();
        if (isMavenBuild(request)) {
            model.put("build", "maven");
            model.put("mavenBuild", true);
        }
        else {
            model.put("build", "gradle");
        }
        write(new File(dir, ".gitignore"), "gitignore.tmpl", model);
    }

    /**
     * Resolve the specified {@link ProjectRequest} and return the model to use to
     * generate the project.
     * @param originalRequest the request to handle
     * @return a model for that request
     */
    protected Map<String, Object> resolveModel(ProjectRequest originalRequest) {
        Assert.notNull(originalRequest.getBootVersion(), "boot version must not be null");
        Map<String, Object> model = new LinkedHashMap<>();
        GeneratorMetadata metadata = this.metadataProvider.get();

        ProjectRequest request = this.requestResolver.resolve(originalRequest, metadata);

        // request resolved so we can log what has been requested
        Version bootVersion = Version.safeParse(request.getBootVersion());
        List<Dependency> dependencies = request.getResolvedDependencies();
        List<String> dependencyIds = dependencies.stream().map(Dependency::getId)
                .collect(Collectors.toList());
        log.info("Processing request{type=" + request.getType() + ", dependencies="
                + dependencyIds);
        if (isMavenBuild(request)) {
            model.put("mavenBuild", true);
            GeneratorConfiguration.Env.Maven.ParentPom parentPom = metadata.getConfiguration().getEnv().getMaven()
                    .resolveParentPom(request.getBootVersion());
            if (parentPom.isIncludeSpringBootBom()
                    && !request.getBoms().containsKey("spring-boot")) {
                request.getBoms().put("spring-boot", metadata.createSpringBootBom(
                        request.getBootVersion(), "spring-boot.version"));
            }
            if(request.getParent() != null){
                model.put("mavenParentGroupId", request.getParent().getGroupId());
                model.put("mavenParentArtifactId", request.getParent().getArtifactId());
                model.put("mavenParentVersion", request.getParent().getVersion());
            } else {
                model.put("mavenParentGroupId", parentPom.getGroupId());
                model.put("mavenParentArtifactId", parentPom.getArtifactId());
                model.put("mavenParentVersion", parentPom.getVersion());
                model.put("includeSpringBootBom", parentPom.isIncludeSpringBootBom());
            }
        }

        model.put("repositoryValues", request.getRepositories().entrySet());
        if (!request.getRepositories().isEmpty()) {
            model.put("hasRepositories", true);
        }

        List<Map<String, String>> resolvedBoms = buildResolvedBoms(request);
        model.put("resolvedBoms", resolvedBoms);
        ArrayList<Map<String, String>> reversedBoms = new ArrayList<>(resolvedBoms);
        Collections.reverse(reversedBoms);
        model.put("reversedBoms", reversedBoms);

        model.put("compileDependencies",
                filterDependencies(dependencies, Dependency.SCOPE_COMPILE));
        model.put("runtimeDependencies",
                filterDependencies(dependencies, Dependency.SCOPE_RUNTIME));
        model.put("compileOnlyDependencies",
                filterDependencies(dependencies, Dependency.SCOPE_COMPILE_ONLY));
        model.put("providedDependencies",
                filterDependencies(dependencies, Dependency.SCOPE_PROVIDED));
        model.put("testDependencies",
                filterDependencies(dependencies, Dependency.SCOPE_TEST));

        request.getBoms().forEach((k, v) -> {
            if (v.getVersionProperty() != null) {
                request.getBuildProperties().getVersions()
                        .computeIfAbsent(v.getVersionProperty(), (key) -> v::getVersion);
            }
        });

        Map<String, String> versions = new LinkedHashMap<>();
        model.put("buildPropertiesVersions", versions.entrySet());
        request.getBuildProperties().getVersions().forEach(
                (k, v) -> versions.put(computeVersionProperty(request, k), v.get()));
        Map<String, String> gradle = new LinkedHashMap<>();
        model.put("buildPropertiesGradle", gradle.entrySet());
        request.getBuildProperties().getGradle()
                .forEach((k, v) -> gradle.put(k, v.get()));
        Map<String, String> maven = new LinkedHashMap<>();
        model.put("buildPropertiesMaven", maven.entrySet());
        request.getBuildProperties().getMaven().forEach((k, v) -> maven.put(k, v.get()));

        // Add various versions
        model.put("dependencyManagementPluginVersion", metadata.getConfiguration()
                .getEnv().getGradle().getDependencyManagementPluginVersion());

        model.put("isRelease", request.getBootVersion().contains("RELEASE"));
        setupApplicationModel(request, model);

        // Gradle plugin has changed as from 1.3.0
        model.put("bootOneThreeAvailable", VERSION_1_3_0_M1.compareTo(bootVersion) <= 0);

        model.put("bootTwoZeroAvailable", VERSION_2_0_0_M1.compareTo(bootVersion) <= 0);

        // Gradle plugin has changed again as from 1.4.2
        model.put("springBootPluginName", (VERSION_1_4_2_M1.compareTo(bootVersion) <= 0
                ? "org.springframework.boot" : "spring-boot"));

        // New testing stuff
        model.put("newTestInfrastructure", isNewTestInfrastructureAvailable(request));

        // Java versions
        model.put("java8OrLater", isJava8OrLater(request));

        // Append the project request to the model
        BeanWrapperImpl bean = new BeanWrapperImpl(request);
        for (PropertyDescriptor descriptor : bean.getPropertyDescriptors()) {
            if (bean.isReadableProperty(descriptor.getName())) {
                model.put(descriptor.getName(),
                        bean.getPropertyValue(descriptor.getName()));
            }
        }
        if (!request.getBoms().isEmpty()) {
            model.put("hasBoms", true);
        }

        setupModulesModel(request, model);

        return model;
    }

    private List<Map<String, String>> buildResolvedBoms(ProjectRequest request) {
        return request.getBoms().values().stream()
                .sorted(Comparator.comparing(BillOfMaterials::getOrder))
                .map((bom) -> toBomModel(request, bom)).collect(Collectors.toList());
    }

    private Map<String, String> toBomModel(ProjectRequest request, BillOfMaterials bom) {
        Map<String, String> model = new HashMap<>();
        model.put("groupId", bom.getGroupId());
        model.put("artifactId", bom.getArtifactId());
        model.put("versionToken",
                (bom.getVersionProperty() != null ? "${"
                        + computeVersionProperty(request, bom.getVersionProperty()) + "}"
                        : bom.getVersion()));
        return model;
    }

    private String computeVersionProperty(ProjectRequest request,
                                          VersionProperty property) {
        if (isGradleBuild(request)) {
            return property.toCamelCaseFormat();
        }
        return property.toStandardFormat();
    }

    protected void setupApplicationModel(ProjectRequest request,
                                         Map<String, Object> model) {
        Imports imports = new Imports(request.getLanguage());
        Annotations annotations = new Annotations();
        boolean useSpringBootApplication = VERSION_1_2_0_RC1
                .compareTo(Version.safeParse(request.getBootVersion())) <= 0;

        if (ServiceMetadata.importMap.containsKey(request.getName()) && ServiceMetadata.annotationMap.containsKey(request.getName())) {
            ServiceMetadata.importMap.get(request.getName()).forEach(imports::add);
            ServiceMetadata.annotationMap.get(request.getName()).forEach(annotations::add);
        }
        else {
            imports.add("org.springframework.boot.autoconfigure.EnableAutoConfiguration")
                    .add("org.springframework.context.annotation.ComponentScan")
                    .add("org.springframework.context.annotation.Configuration");
            annotations.add("@EnableAutoConfiguration").add("@ComponentScan")
                    .add("@Configuration");
        }
        model.put("applicationImports", imports.toString());
        model.put("applicationAnnotations", annotations.toString());

    }

    protected void setupTestModel(ProjectRequest request, Map<String, Object> model) {
        Imports imports = new Imports(request.getLanguage());
        Annotations testAnnotations = new Annotations();
        boolean newTestInfrastructure = isNewTestInfrastructureAvailable(request);
        if (newTestInfrastructure) {
            imports.add("org.springframework.boot.test.context.SpringBootTest")
                    .add("org.springframework.test.context.junit4.SpringRunner");
        }
        else {
            imports.add("org.springframework.boot.test.SpringApplicationConfiguration")
                    .add("org.springframework.test.context.junit4.SpringJUnit4ClassRunner");
        }
        if (request.hasWebFacet() && !newTestInfrastructure) {
            imports.add("org.springframework.test.context.web.WebAppConfiguration");
            testAnnotations.add("@WebAppConfiguration");
        }
        imports.add("org.junit.Ignore");
        testAnnotations.add("@Ignore");
        model.put("testImports", imports.withFinalCarriageReturn().toString());
        model.put("testAnnotations",
                testAnnotations.withFinalCarriageReturn().toString());
    }

    private void setupModulesModel(ProjectRequest request, Map<String, Object> model){
        if(!request.getServices().isEmpty()) {
            model.put("modules", request.getServices());
        }
    }

    private static boolean isJava8OrLater(ProjectRequest request) {
        return !request.getJavaVersion().equals("1.6")
                && !request.getJavaVersion().equals("1.7");
    }

    private static boolean isGradleBuild(ProjectRequest request) {
        return "gradle".equals(request.getBuild());
    }

    private static boolean isMavenBuild(ProjectRequest request) {
        return "maven".equals(request.getBuild());
    }

    private static boolean isWar(ProjectRequest request) {
        return "war".equals(request.getPackaging());
    }

    private static boolean isNewTestInfrastructureAvailable(ProjectRequest request) {
        return VERSION_1_4_0_M2
                .compareTo(Version.safeParse(request.getBootVersion())) <= 0;
    }

    private static boolean isGradle3Available(Version bootVersion) {
        return VERSION_1_5_0_M1.compareTo(bootVersion) <= 0;
    }

    private static boolean isGradle4Available(Version bootVersion) {
        return VERSION_2_0_0_M3.compareTo(bootVersion) < 0;
    }

    private byte[] doGenerateMavenPom(Map<String, Object> model, String template) {
        return this.templateRenderer.process(template, model).getBytes();
    }

    private byte[] doGenerateGradleBuild(Map<String, Object> model) {
        return this.templateRenderer.process("starter-build.gradle__NOTWORKING", model).getBytes();
    }

    private byte[] doGenerateGradleSettings(Map<String, Object> model) {
        return this.templateRenderer.process("starter-settings.gradle", model).getBytes();
    }

    private void writeGradleWrapper(File dir, Version bootVersion) {
        String gradlePrefix = isGradle4Available(bootVersion) ? "gradle4"
                : isGradle3Available(bootVersion) ? "gradle3" : "gradle";
        writeTextResource(dir, "gradlew.bat", gradlePrefix + "/gradlew.bat");
        writeTextResource(dir, "gradlew", gradlePrefix + "/gradlew");

        File wrapperDir = new File(dir, "gradle/wrapper");
        wrapperDir.mkdirs();
        writeTextResource(wrapperDir, "gradle-wrapper.properties",
                gradlePrefix + "/gradle/wrapper/gradle-wrapper.properties");
        writeBinaryResource(wrapperDir, "gradle-wrapper.jar",
                gradlePrefix + "/gradle/wrapper/gradle-wrapper.jar");
    }

    private void writeMavenWrapper(File dir) {
        writeTextResource(dir, "mvnw.cmd", "maven/mvnw.cmd");
        writeTextResource(dir, "mvnw", "maven/mvnw");

        File wrapperDir = new File(dir, ".mvn/wrapper");
        wrapperDir.mkdirs();
        writeTextResource(wrapperDir, "maven-wrapper.properties",
                "maven/wrapper/maven-wrapper.properties");
        writeBinaryResource(wrapperDir, "maven-wrapper.jar",
                "maven/wrapper/maven-wrapper.jar");
    }

    private File writeBinaryResource(File dir, String name, String location) {
        return doWriteProjectResource(dir, name, location, true);
    }

    private File writeTextResource(File dir, String name, String location) {
        return doWriteProjectResource(dir, name, location, false);
    }

    private File doWriteProjectResource(File dir, String name, String location,
                                        boolean binary) {
        File target = new File(dir, name);
        if (binary) {
            writeBinary(target, this.projectResourceLocator
                    .getBinaryResource("classpath:project/" + location));
        }
        else {
            writeText(target, this.projectResourceLocator
                    .getTextResource("classpath:project/" + location));
        }
        return target;
    }

    private File initializerProjectDir(File rootDir, ProjectRequest request) {
        if (request.getBaseDir() != null) {
            File dir = new File(rootDir, request.getBaseDir());
            dir.mkdirs();
            return dir;
        }
        else {
            return rootDir;
        }
    }

    public void write(File target, String templateName, Map<String, Object> model) {
        String body = this.templateRenderer.process(templateName, model);
        writeText(target, body);
    }

    private void writeText(File target, String body) {
        try (OutputStream stream = new FileOutputStream(target)) {
            StreamUtils.copy(body, Charset.forName("UTF-8"), stream);
        }
        catch (Exception e) {
            throw new IllegalStateException("Cannot write file " + target, e);
        }
    }

    private void writeBinary(File target, byte[] body) {
        try (OutputStream stream = new FileOutputStream(target)) {
            StreamUtils.copy(body, stream);
        }
        catch (Exception e) {
            throw new IllegalStateException("Cannot write file " + target, e);
        }
    }

    private void addTempFile(String group, File file) {
        this.temporaryFiles.computeIfAbsent(group, (key) -> new ArrayList<>()).add(file);
    }

    private static List<Dependency> filterDependencies(List<Dependency> dependencies,
                                                       String scope) {
        return dependencies.stream().filter((dep) -> scope.equals(dep.getScope()))
                .sorted(DependencyComparator.INSTANCE).collect(Collectors.toList());
    }

    private static class DependencyComparator implements Comparator<Dependency> {

        private static final DependencyComparator INSTANCE = new DependencyComparator();

        @Override
        public int compare(Dependency o1, Dependency o2) {
            if (isSpringBootDependency(o1) && isSpringBootDependency(o2)) {
                return o1.getArtifactId().compareTo(o2.getArtifactId());
            }
            if (isSpringBootDependency(o1)) {
                return -1;
            }
            if (isSpringBootDependency(o2)) {
                return 1;
            }
            int group = o1.getGroupId().compareTo(o2.getGroupId());
            if (group != 0) {
                return group;
            }
            return o1.getArtifactId().compareTo(o2.getArtifactId());
        }

        private boolean isSpringBootDependency(Dependency dependency) {
            return dependency.getGroupId().startsWith("org.springframework.boot");
        }

    }

    private static class Imports {

        private final List<String> statements = new ArrayList<>();

        private final String language;

        private boolean finalCarriageReturn;

        Imports(String language) {
            this.language = language;
        }

        public Imports add(String type) {
            this.statements.add(generateImport(type, this.language));
            return this;
        }

        public Imports withFinalCarriageReturn() {
            this.finalCarriageReturn = true;
            return this;
        }

        private String generateImport(String type, String language) {
            return "import " + type + ";";
        }

        @Override
        public String toString() {
            if (this.statements.isEmpty()) {
                return "";
            }
            String content = String.join(String.format("%n"), this.statements);
            return (this.finalCarriageReturn ? String.format("%s%n", content) : content);
        }

    }

    private static class Annotations {

        private final List<String> statements = new ArrayList<>();

        private boolean finalCarriageReturn;

        public Annotations add(String type) {
            this.statements.add(type);
            return this;
        }

        public Annotations withFinalCarriageReturn() {
            this.finalCarriageReturn = true;
            return this;
        }

        @Override
        public String toString() {
            if (this.statements.isEmpty()) {
                return "";
            }
            String content = String.join(String.format("%n"), this.statements);
            return (this.finalCarriageReturn ? String.format("%s%n", content) : content);
        }

    }

}
