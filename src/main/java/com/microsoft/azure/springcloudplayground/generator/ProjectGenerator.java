package com.microsoft.azure.springcloudplayground.generator;

import com.microsoft.azure.springcloudplayground.dependency.Dependency;
import com.microsoft.azure.springcloudplayground.exception.GeneratorException;
import com.microsoft.azure.springcloudplayground.metadata.*;
import com.microsoft.azure.springcloudplayground.service.ConfigurableService;
import com.microsoft.azure.springcloudplayground.service.Service;
import com.microsoft.azure.springcloudplayground.util.TemplateRenderer;
import com.microsoft.azure.springcloudplayground.util.Version;
import com.microsoft.azure.springcloudplayground.util.VersionProperty;
import lombok.Getter;
import lombok.Setter;
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
import org.springframework.util.StringUtils;

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

    private static final Map<String, List<String>> serviceToDependencies = new HashMap<>();

    static {
        serviceToDependencies.put("cloud-config-server",
                Arrays.asList("cloud-config-server"));
        serviceToDependencies.put("cloud-gateway",
                Arrays.asList("cloud-gateway", "cloud-eureka-client", "cloud-config-client"));
        serviceToDependencies.put("cloud-eureka-server",
                Arrays.asList("cloud-eureka-server", "cloud-config-client"));
        serviceToDependencies.put("cloud-hystrix-dashboard",
                Arrays.asList("cloud-hystrix-dashboard", "cloud-config-client", "cloud-eureka-client", "web"));
        serviceToDependencies.put("azure-service-bus",
                Arrays.asList("azure-service-bus", "cloud-config-client", "cloud-eureka-client", "web"));
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    @Getter
    private GeneratorMetadataProvider metadataProvider;

    @Autowired
    private ProjectRequestResolver requestResolver;

    @Autowired
    private TemplateRenderer templateRenderer = new TemplateRenderer();

    @Autowired
    private ProjectResourceLocator projectResourceLocator = new ProjectResourceLocator();

    @Value("${TMPDIR:.}/playground")
    @Setter
    private String tmpdir;

    @Setter
    private File temporaryDirectory;

    @Setter
    private transient Map<String, List<File>> temporaryFiles = new LinkedHashMap<>();

    private void resolveMicroServiceBuildProperties(@NonNull Map<String, Object> serviceModel) {
        Map<String, String> properties = new HashMap<>();

        properties.put("project.build.sourceEncoding", "UTF-8");
        properties.put("project.reporting.outputEncoding", "UTF-8");

        serviceModel.put("buildPropertiesMaven", properties.entrySet());
    }

    @NonNull
    private Dependency getSpringBootStarterDependency() {
        Dependency bootStarter = new Dependency();

        bootStarter.setId("root_starter");
        bootStarter.asSpringBootStarter("");

        return bootStarter;
    }

    @NonNull
    private void resolveMicroServiceDependency(@NonNull Map<String, Object> serviceModel, @NonNull String bootVersion) {
        Version version = Version.parse(bootVersion);
        GeneratorMetadata metadata = this.metadataProvider.get();
        HashSet<String> set = new HashSet<>();
        List<String> modules = (List<String>) serviceModel.get("modules");

        modules.forEach(s -> set.addAll(serviceToDependencies.get(s)));

        List<String> dependencyIdList = new ArrayList<>(set);
        List<Dependency> dependencies = dependencyIdList.stream().map(
                d -> metadata.getDependencies().get(d).resolve(version)).collect(Collectors.toList());

        dependencies.add(getSpringBootStarterDependency());

        serviceModel.put("dependencyIdList", dependencyIdList);
        serviceModel.put("compileDependencies", filterDependencies(dependencies, Dependency.SCOPE_COMPILE));
        serviceModel.put("runtimeDependencies", filterDependencies(dependencies, Dependency.SCOPE_RUNTIME));
        serviceModel.put("compileOnlyDependencies", filterDependencies(dependencies, Dependency.SCOPE_COMPILE_ONLY));
        serviceModel.put("providedDependencies", filterDependencies(dependencies, Dependency.SCOPE_PROVIDED));
        serviceModel.put("testDependencies", filterDependencies(dependencies, Dependency.SCOPE_TEST));
    }

    @NonNull
    private String resolveMicroServiceSourceImports(@NonNull String serviceName) {
        Imports imports = new Imports("java");

        if (ServiceMetadata.importMap.containsKey(serviceName)) {
            ServiceMetadata.importMap.get(serviceName).forEach(imports::add);
        } else {
            imports.add("org.springframework.boot.autoconfigure.EnableAutoConfiguration");
            imports.add("org.springframework.context.annotation.ComponentScan");
            imports.add("org.springframework.context.annotation.Configuration");
        }

        return imports.toString();
    }

    @NonNull
    private String resolveMicroServiceSourceAnnotations(@NonNull String serviceName) {
        Annotations annotations = new Annotations();

        if (ServiceMetadata.annotationMap.containsKey(serviceName)) {
            ServiceMetadata.annotationMap.get(serviceName).forEach(annotations::add);
        } else {
            annotations.add("@EnableAutoConfiguration");
            annotations.add("@ComponentScan");
            annotations.add("@Configuration");
        }

        return annotations.toString();
    }

    @NonNull
    private String resolveMicroServiceTestImports() {
        Imports imports = new Imports("java");

        imports.add("org.springframework.boot.test.context.SpringBootTest");
        imports.add("org.springframework.test.context.junit4.SpringRunner");
        imports.add("org.junit.Ignore");

        return imports.withFinalCarriageReturn().toString();
    }

    @NonNull
    private String resolveMicroServiceTestAnnotations() {
        Annotations annotations = new Annotations();

        annotations.add("@Ignore");

        return annotations.withFinalCarriageReturn().toString();
    }

    private List<String> getMicroServiceNames(@NonNull Map<String, Object> model) {
        return (List<String>) model.get("microServiceNames");
    }

    @NonNull
    private Map<String, Object> getMicroServiceByName(@NonNull String name, @NonNull Map<String, Object> model) {
        return (Map<String, Object>) model.get(name);
    }

    @NonNull
    private Map<String, Object> resolveMicroServiceModel(@NonNull MicroService service,
                                                         @NonNull Map<String, Object> model) {
        GeneratorMetadata metadata = this.metadataProvider.get();
        Map<String, Object> serviceModel = (Map<String, Object>) model.get(service.getName());
        Service serviceData = Service.toService(service.getName());

        model.put(service.getName(), serviceModel);

        serviceModel.put("groupId", model.get("groupId"));
        serviceModel.put("artifactId", String.format("%s.%s", model.get("artifactId").toString(), service.getName()));
        serviceModel.put("version", model.get("version"));
        serviceModel.put("packaging", "jar");
        serviceModel.put("packageName", model.get("packageName"));

        serviceModel.put("description", model.get("description"));

        serviceModel.put("mavenParentGroupId", model.get("groupId"));
        serviceModel.put("mavenParentArtifactId", model.get("artifactId"));
        serviceModel.put("mavenParentVersion", model.get("version"));

        this.resolveMicroServiceBuildProperties(serviceModel);
        this.resolveMicroServiceDependency(serviceModel, model.get("bootVersion").toString());

        serviceModel.put("applicationName", metadata.getConfiguration().generateApplicationName(service.getName()));

        serviceModel.put("applicationImports", resolveMicroServiceSourceImports(service.getName()));
        serviceModel.put("applicationAnnotations", resolveMicroServiceSourceAnnotations(service.getName()));
        serviceModel.put("testImports", resolveMicroServiceTestImports());
        serviceModel.put("testAnnotations", resolveMicroServiceTestAnnotations());

        serviceModel.put("healthCheckPath", serviceData.getHealthCheckPath());
        serviceModel.put("image", serviceData.getImage());

        return serviceModel;
    }

    private void writeKubernetesFile(File dir, ProjectRequest request) {
        List<Service> services = ServiceResolver.resolve(request.getServices());
        Map<String, Object> model = new HashMap<>();
        model.put("services", services);
        writeText(new File(dir, KUBERNETES_FILE), templateRenderer.process(Paths.get(DOCKER_PATH, KUBERNETES_FILE).toString(), model));
    }

    private void generateDockerStructure(@NonNull File rootDir, @NonNull String baseDir, Map<String, Object> modulesModel, ProjectRequest request) {
        final File dockerDir = Paths.get(rootDir.getPath(), baseDir, "docker").toFile();
        final String template = "docker";
        final String dockerComposeName = "docker-compose.yml";
        final String runBash = "run.sh";
        final String runCmd = "run.cmd";
        final String readMe = "README.md";

        dockerDir.mkdir();

        request.getModules().forEach(m -> modulesModel.put(m.getName() + "-" + "port", m.getPort()));

        writeText(new File(dockerDir, dockerComposeName), templateRenderer.process(Paths.get(template, dockerComposeName).toString(), modulesModel));
        writeText(new File(dockerDir, runBash), templateRenderer.process(Paths.get(template, runBash).toString(), null));
        writeText(new File(dockerDir, runCmd), templateRenderer.process(Paths.get(template, runCmd).toString(), null));
        writeText(new File(dockerDir, readMe), templateRenderer.process(Paths.get(template, readMe).toString(), null));
        writeKubernetesFile(dockerDir, request);
    }

    private void generateBaseDirectory(@NonNull File rootDir, @NonNull SimpleProjectRequest request,
                                       @NonNull Map<String, Object> model) {
        Assert.hasText(request.getBaseDir(), "Basedir should have text.");

        File dir = new File(rootDir, request.getBaseDir());
        dir.mkdir();

        String pom = new String(doGenerateMavenPom(model, "parent-pom.xml"));
        writeText(new File(dir, "pom.xml"), pom);

        writeMavenWrapper(dir);
        write(new File(dir, ".gitignore"), "gitignore.tmpl", model);
    }

    private File generateRootProject(@NonNull SimpleProjectRequest request, @NonNull Map<String, Object> model) {
        File rootDir;

        try {
            rootDir = File.createTempFile("tmp", "", getTemporaryDirectory());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create temp dir", e);
        }

        addTempFile(rootDir.getName(), rootDir);
        rootDir.delete();
        rootDir.mkdir();

        this.generateBaseDirectory(rootDir, request, model);

        return rootDir;
    }

    private void generateMicroServiceDockerfile(@NonNull File serviceDir, @NonNull Map<String, Object> serviceModel) {
        String dockerFile = "Dockerfile";
        String dockerTemplate = Paths.get("docker", dockerFile).toString();

        writeText(new File(serviceDir, dockerFile), templateRenderer.process(dockerTemplate, serviceModel));
    }

    private void generateMicroServicePom(@NonNull File serviceDir, @NonNull Map<String, Object> serviceModel) {
        String pom = new String(doGenerateMavenPom(serviceModel, "module-pom.xml"));
        writeText(new File(serviceDir, "pom.xml"), pom);
    }

    private void generateCloudConfigSourceCode(@NonNull File serviceDir, @NonNull File src,
                                               @NonNull Map<String, Object> serviceModel,
                                               @NonNull Map<String, Object> model) {
        File resources = new File(serviceDir, "src/main/resources/");
        File shared = new File(resources, "shared");

        shared.mkdir();

        String yamlFile = ModulePropertiesResolver.getSharedCommonPropTemplate();
        writeText(new File(shared, "application.yml"), templateRenderer.process(yamlFile, null));

        List<String> serviceNames = getMicroServiceNames(model);
        String configServer = serviceModel.get("name").toString();

        serviceNames.stream().filter(s -> !s.equalsIgnoreCase(configServer)).forEach(s -> {
            final Map<String, Object> microService = getMicroServiceByName(s, model);
            final String yamlTemplate = ModulePropertiesResolver.getSharedPropTemplate(s);
            writeText(new File(shared, s + ".yml"), templateRenderer.process(yamlTemplate, microService));
        });
    }

    private void generateCloudGatewaySourceCode(File serviceDir, File src, Map<String,Object> serviceModel,
                                                Map<String,Object> model) {
        write(new File(src, "CustomWebFilter.java"), "CustomWebFilter.java", serviceModel);
        File staticResources = new File(serviceDir, "src/main/resources/static");

        staticResources.mkdirs();

        List<String> serviceNames = getMicroServiceNames(model);
        writeText(new File(staticResources, "index.html"),
                templateRenderer.process("GatewayIndex.tmpl", ServiceMetadata.getLinksMap(serviceNames)));
        writeText(new File(staticResources, "bulma.min.css"), templateRenderer.process("bulma.min.css", null));
    }

    private void generateMicroServiceSourceCode(@NonNull File serviceDir, @NonNull Map<String, Object> serviceModel,
                                                @NonNull Map<String, Object> model) {
        List<String> dependencies = (List<String>) serviceModel.get("dependencyIdList");
        String applicationName = serviceModel.get("applicationName").toString();
        String serviceName = serviceModel.get("name").toString();
        File resources = new File(serviceDir, "src/main/resources");
        File src = new File(new File(serviceDir, "src/main/java"),
                serviceModel.get("packageName").toString().replace(".", "/"));
        src.mkdirs();
        resources.mkdirs();

        if (serviceName.equalsIgnoreCase("cloud-hystrix-dashboard")) {
            write(new File(src, applicationName + ".java"), "HystrixDashboardApplication.java", serviceModel);
            write(new File(src, "MockStreamServlet.java"), "MockStreamServlet.java", serviceModel);
            writeTextResource(resources, "hystrix.stream", "hystrix.stream");
        } else {
            write(new File(src, applicationName + ".java"), "Application.java", serviceModel);
        }

        if (dependencies.contains("web")) {
            new File(serviceDir, "src/main/resources/templates").mkdirs();
            new File(serviceDir, "src/main/resources/static").mkdirs();
            write(new File(src, "Controller.java"), "Controller.java", serviceModel);
        }

        if (serviceName.equalsIgnoreCase("cloud-config-server")) {
            this.generateCloudConfigSourceCode(serviceDir, src, serviceModel, model);
        } else if (serviceName.equalsIgnoreCase("cloud-gateway")) {
            this.generateCloudGatewaySourceCode(serviceDir, src, serviceModel, model);
        }

        String yamlFile = ModulePropertiesResolver.getBootstrapTemplate(serviceModel.get("name").toString());
        writeText(new File(resources, "bootstrap.yml"), templateRenderer.process(yamlFile, serviceModel));
    }

    private void generateMicroServiceTestCode(@NonNull File serviceDir, @NonNull Map<String, Object> serviceModel) {
        String applicationName = serviceModel.get("applicationName").toString();
        File test = new File(new File(serviceDir, "src/test/java"),
                serviceModel.get("packageName").toString().replace(".", "/"));
        test.mkdirs();

        write(new File(test, applicationName + "Tests.java"), "ApplicationTests.java", serviceModel);
    }

    private void generateMicroServiceCode(@NonNull File serviceDir, @NonNull Map<String, Object> serviceModel,
                                          @NonNull Map<String, Object> model) {
        generateMicroServiceSourceCode(serviceDir, serviceModel, model);
        generateMicroServiceTestCode(serviceDir, serviceModel);
    }

    private void generateMicroService(@NonNull MicroService service, @NonNull Map<String, Object> model,
                                      @NonNull File projectDir) {
        File serviceDir = new File(projectDir, service.getName());
        serviceDir.mkdir();

        Map<String, Object> serviceModel = resolveMicroServiceModel(service, model);

        generateMicroServiceDockerfile(serviceDir, serviceModel);
        generateMicroServicePom(serviceDir, serviceModel);
        generateMicroServiceCode(serviceDir, serviceModel, model);
    }

    private void generateMicroServicesProject(@NonNull List<MicroService> microServices, @NonNull File projectDir,
                                              @NonNull Map<String, Object> model) {
        microServices.forEach(s -> generateMicroService(s, model, projectDir));
    }

    private void generateDockerDirectory(@NonNull File projectDir, @NonNull Map<String, Object> model) {
        String docker = "docker";
        File dockerDir = new File(projectDir, docker);
        String dockerCompose = "docker-compose.yml";
        String runBash = "run.sh";
        String runCmd = "run.cmd";
        String readMe = "README.md";
        String kubernetes = "kubernetes.yaml";

        dockerDir.mkdirs();

        writeText(new File(dockerDir, dockerCompose), templateRenderer.process(docker + "/" + dockerCompose, model));
        writeText(new File(dockerDir, runBash), templateRenderer.process(docker + "/" + runBash, null));
        writeText(new File(dockerDir, runCmd), templateRenderer.process(docker + "/" + runCmd, null));
        writeText(new File(dockerDir, readMe), templateRenderer.process(docker + "/" + readMe, null));
        writeText(new File(dockerDir, kubernetes), templateRenderer.process(docker + "/" + kubernetes, model));
    }

    public File generate(@NonNull SimpleProjectRequest request) {
        Map<String, Object> model = this.resolveModel(request);
        File rootDir = this.generateRootProject(request, model);
        File projectDir = new File(rootDir, request.getBaseDir());

        generateMicroServicesProject(request.getMicroServices(), projectDir, model);
        generateDockerDirectory(projectDir, model);

        return rootDir;
    }

    /**
     * Generate a project structure for the specified {@link ProjectRequest}. Returns a
     * directory containing the project.
     *
     * @param request the project request
     * @return the generated project structure
     */
    public File generateProjectStructure(ProjectRequest request) {
        try {
            Map<String, Object> model = resolveModel(request);
            File rootDir = generateProjectStructure(request, model);
            final Map<String, Object> modulesModel = new HashMap<>();

            for (ProjectRequest subModule : request.getModules()) {
                final File subModuleDir = new File(rootDir, request.getBaseDir());
                generateProjectModuleStructure(subModule, resolveModel(subModule), subModuleDir, request);
                modulesModel.put(subModule.getName(), Boolean.TRUE);
            }

            generateDockerStructure(rootDir, request.getBaseDir(), modulesModel, request);

            return rootDir;
        } catch (GeneratorException ex) {
            //publishProjectFailedEvent(request, ex);
            throw ex;
        }
    }

    private void servicesPortsUpdate(@NonNull ProjectRequest request) {
        Assert.isNull(request.getParent(), "should be parent project.");

        final Map<String, Integer> servicesPortsMap = request.getServicesPortsMap();
        final List<String> services = request.getServices();

        final long configuredCount = services.stream().filter(s -> servicesPortsMap.get(s) != null).count();

        if (configuredCount < services.size()) {
            servicesPortsMap.forEach((key, value) -> servicesPortsMap.put(key, ServiceMetadata.portMap.get(key)));
        }

        request.getModules().forEach(m -> m.setPort(servicesPortsMap.get(m.getName())));
    }

    /**
     * Generate a project structure for the specified {@link ProjectRequest} and resolved
     * model.
     *
     * @param request the project request
     * @param model   the source model
     * @return the generated project structure
     */
    protected File generateProjectStructure(ProjectRequest request, Map<String, Object> model) {
        File rootDir;
        try {
            rootDir = File.createTempFile("tmp", "", getTemporaryDirectory());
        } catch (IOException e) {
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
        } else {
            String pom = new String(doGenerateMavenPom(model, "parent-pom.xml"));
            writeText(new File(dir, "pom.xml"), pom);
            writeMavenWrapper(dir);
        }

        generateGitIgnore(dir, request);
        servicesPortsUpdate(request);

        return rootDir;
    }

    /**
     * Generate a module structure for the specified {@link ProjectRequest} and resolved
     * model.
     *
     * @param request the project request
     * @param model   the source model
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
        } else {
            // Write Dockerfile to module
            final String dockerFileName = "Dockerfile";
            final String exposePortVariable = "EXPOSE_PORT";
            final String dockerTemplateDir = "docker";
            final Map<String, Integer> portModel = new HashMap<>();
            final String path = Paths.get(dockerTemplateDir, dockerFileName).toString();

            portModel.put(exposePortVariable, request.getPort());
            writeText(new File(dir, dockerFileName), templateRenderer.process(path, portModel));

            log.info("Writing Dockerfile to module " + request.getName());

            // Write pom file
            String pom = new String(doGenerateMavenPom(model, "module-pom.xml"));
            writeText(new File(dir, "pom.xml"), pom);
        }

        String applicationName = request.getApplicationName();
        String language = request.getLanguage();

        File src = new File(new File(dir, "src/main/" + language),
                request.getPackageName().replace(".", "/"));
        src.mkdirs();
        if (request.getName().equalsIgnoreCase("cloud-hystrix-dashboard")) {
            write(new File(src, applicationName + "." + language), "HystrixDashboardApplication." + language, model);
            write(new File(src, "MockStreamServlet.java"), "MockStreamServlet.java", model);
        } else {
            write(new File(src, applicationName + "." + language), "Application." + language, model);
        }

        if (request.getDependencies().contains("web")) {
            write(new File(src, "Controller." + language), "Controller.java", model);
        }

        // Write index page if is gateway module
        if (ModulePropertiesResolver.isGatewayModule(request.getName())) {
            // Only java supported, WebFilter for root request mapping
            write(new File(src, "CustomWebFilter.java"), "CustomWebFilter.java", model);

            File resourceFolder = new File(dir, "src/main/resources/static");
            resourceFolder.mkdirs();

            writeText(new File(resourceFolder, "index.html"),
                    templateRenderer.process("GatewayIndex.tmpl", ServiceMetadata.getLinksMap(parentModule)));
            writeText(new File(resourceFolder, "bulma.min.css"), templateRenderer.process("bulma.min.css", null));
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

        if (request.getName().equalsIgnoreCase("cloud-hystrix-dashboard")) {
            writeTextResource(resources, "hystrix.stream", "hystrix.stream");
        }

        if (request.hasWebFacet()) {
            new File(dir, "src/main/resources/templates").mkdirs();
            new File(dir, "src/main/resources/static").mkdirs();
        }
        return rootDir;
    }

    private void writePropertiesFile(ProjectRequest request, File resourceDir, ProjectRequest parentRequest) {
        if (ModulePropertiesResolver.isConfigServer(request.getName())) {
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
                    .map(module -> new ConfigurableService(module.getName(), "0"))
                    .collect(Collectors.toList());

            for (ProjectRequest module : parentRequest.getModules()) {
                String templateFile = ModulePropertiesResolver.getSharedPropTemplate(module.getName());
                Map<String, Object> model = new HashMap<>();

                String moduleName = module.getName();
                if (ModulePropertiesResolver.isConfigServer(moduleName)) {
                    // No shared properties file is required to be generated for config server itself
                    continue;
                }

                if (ModulePropertiesResolver.isGatewayModule(moduleName)) {
                    model.put("services", azureServices);
                } else if (!ModulePropertiesResolver.isInfraModule(moduleName)) {
                    model.put("applicationName", module.getName());
                    model.put("port", request.getPort());
                }

                String content = templateRenderer.process(templateFile, model);
                writeText(new File(sharedPropFolder, module.getName() + ".yml"), content);
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

    /**
     * Create a distribution file for the specified project structure directory and
     * extension.
     *
     * @param dir       the directory
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
     *
     * @param dir the directory to clean
     * @see #createDistributionFile
     */
    public void cleanTempFiles(File dir) {
        List<File> tempFiles = this.temporaryFiles.remove(dir.getName());
        if (!tempFiles.isEmpty()) {
            tempFiles.forEach((File file) -> {
                if (file.isDirectory()) {
                    FileSystemUtils.deleteRecursively(file);
                } else if (file.exists()) {
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
     *
     * @param dir     the root directory of the project
     * @param request the request to handle
     */
    protected void generateGitIgnore(File dir, ProjectRequest request) {
        Map<String, Object> model = new LinkedHashMap<>();
        if (isMavenBuild(request)) {
            model.put("build", "maven");
            model.put("mavenBuild", true);
        } else {
            model.put("build", "gradle");
        }
        write(new File(dir, ".gitignore"), "gitignore.tmpl", model);
    }

    private Map<String, String> getBomModel(@NonNull BillOfMaterials bom) {
        Map<String, String> model = new HashMap<>();

        model.put("groupId", bom.getGroupId());
        model.put("artifactId", bom.getArtifactId());

        String version = bom.getVersion();

        if (bom.getVersionProperty() != null) {
            version = String.format("${%s}", bom.getVersionProperty().toStandardFormat());
        }

        model.put("versionToken", version);

        return model;
    }

    private BillOfMaterials getBomBillofMaterials(@NonNull String bom, @NonNull GeneratorMetadata metadata,
                                                  @NonNull Version version) {
        return metadata.getConfiguration().getEnv().getBoms().get(bom).resolve(version);
    }

    private Map<String, BillOfMaterials> getBoms(@NonNull String bootVersion) {
        Version version = Version.parse(bootVersion);
        Map<String, BillOfMaterials> boms = new LinkedHashMap<>();
        GeneratorMetadata metadata = this.metadataProvider.get();

        metadata.getDependencies().getAll().stream().filter(d -> d.getBom() != null)
                .forEach(d -> boms.putIfAbsent(d.getBom(), getBomBillofMaterials(d.getBom(), metadata, version)));

        return boms;
    }

    private void resolveBomsModel(@NonNull SimpleProjectRequest request, @NonNull Map<String, Object> model) {
        Map<String, BillOfMaterials> boms = getBoms(request.getBootVersion());

        model.put("hasBoms", true);
        model.put("resolvedBoms", boms.values().stream().sorted(Comparator.comparing(BillOfMaterials::getOrder))
                .map(this::getBomModel).collect(Collectors.toList()));
    }

    private void resolveBuildPropertiesModel(@NonNull SimpleProjectRequest request, @NonNull Map<String, Object> model) {
        Map<String, String> versions = new HashMap<>();
        Map<String, String> maven = new HashMap<>();

        maven.put("project.build.sourceEncoding", "UTF-8");
        maven.put("project.reporting.outputEncoding", "UTF-8");

        versions.put("java.version", request.getJavaVersion());
        this.getBoms(request.getBootVersion()).entrySet().stream()
                .filter(e -> e.getValue().getVersionProperty() != null)
                .forEach(e -> versions.putIfAbsent(e.getValue().getVersionProperty().toStandardFormat(),
                        e.getValue().getVersion()));

        model.put("buildPropertiesVersions", versions.entrySet());
        model.put("buildPropertiesMaven", maven.entrySet());
    }

    private boolean isAzureServices(@NonNull String name) {
        if (!StringUtils.hasText(name)) {
            return false;
        }

        return name.contains("azure");
    }

    private void resolveRequestMicroServicesModel(@NonNull SimpleProjectRequest request,
                                                  @NonNull Map<String, Object> model) {
        List<String> microServiceNames = new ArrayList<>();
        List<Map<String, Object>> microServices = new ArrayList<>();
        List<Map<String, Object>> azureServices = new ArrayList<>();

        request.getMicroServices().forEach(s -> microServiceNames.add(s.getName()));
        model.put("microServiceNames", microServiceNames);

        request.getMicroServices().forEach(s -> {
            final Map<String, Object> service = new HashMap<>();

            service.put("port", s.getPort());
            service.put("name", s.getName());
            service.put("modules", s.getModules());

            model.put(s.getName(), service);
            microServices.add(service);

            if (isAzureServices(s.getName())) {
                azureServices.add(service);
            }

            if (s.getName().equalsIgnoreCase("cloud-gateway")) {
                service.put("azureServices", azureServices);
            }
        });

        model.put("microServices", microServices);
        model.put("azureServices", azureServices);
    }

    private void resolveRequestModel(@NonNull SimpleProjectRequest request, @NonNull Map<String, Object> model) {
        model.put("name", request.getName());
        model.put("type", request.getType());
        model.put("groupId", request.getGroupId());
        model.put("artifactId", request.getArtifactId());
        model.put("version", request.getVersion());
        model.put("bootVersion", request.getBootVersion());
        model.put("packageName", request.getPackageName());
        model.put("javaVersion", request.getJavaVersion());
        model.put("baseDir", request.getBaseDir());
        model.put("packaging", request.getPackaging());
        model.put("description", request.getDescription());

        resolveRequestMicroServicesModel(request, model);
    }

    private void resolveRepositoryModel(@NonNull String bootVersion, @NonNull Map<String, Object> model) {
        GeneratorMetadata metadata = this.metadataProvider.get();
        Map<String, Repository> repositories = new LinkedHashMap<>();

        this.getBoms(bootVersion).values().forEach(e -> e.getRepositories().forEach(
                k -> repositories.computeIfAbsent(k, s -> metadata.getConfiguration().getEnv().getRepositories().get(s))
        ));

        model.put("repositoryValues", repositories.entrySet());
        model.put("hasRepositories", true);
        model.put("isRelease", true);
    }

    private Map<String, Object> resolveModel(@NonNull SimpleProjectRequest request) {
        Map<String, Object> model = new LinkedHashMap<>();

        model.put("mavenBuild", true);
        model.put("build", "maven");
        model.put("mavenParentGroupId", "org.springframework.boot");
        model.put("mavenParentArtifactId", "spring-boot-starter-parent");
        model.put("mavenParentVersion", request.getBootVersion());

        resolveRequestModel(request, model);
        resolveRepositoryModel(request.getBootVersion(), model);
        resolveBomsModel(request, model);
        resolveBuildPropertiesModel(request, model);

        return model;
    }

    /**
     * Resolve the specified {@link ProjectRequest} and return the model to use to
     * generate the project.
     *
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

            if (request.getParent() != null) {
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
        } else {
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
        } else {
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

    private void setupModulesModel(ProjectRequest request, Map<String, Object> model) {
        if (!request.getServices().isEmpty()) {
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
        } else {
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
        } else {
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
        } catch (Exception e) {
            throw new IllegalStateException("Cannot write file " + target, e);
        }
    }

    private void writeBinary(File target, byte[] body) {
        try (OutputStream stream = new FileOutputStream(target)) {
            StreamUtils.copy(body, stream);
        } catch (Exception e) {
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
