package com.microsoft.azure.springcloudplayground.generator;

import com.microsoft.azure.springcloudplayground.dependency.Dependency;
import com.microsoft.azure.springcloudplayground.metadata.*;
import com.microsoft.azure.springcloudplayground.service.Service;
import com.microsoft.azure.springcloudplayground.service.ServiceNames;
import com.microsoft.azure.springcloudplayground.util.TemplateRenderer;
import com.microsoft.azure.springcloudplayground.util.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectGenerator {

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

    @Autowired
    ResourceLoader resourceLoader;

    //private void writeKubernetesFile(File dir, ProjectRequest request){
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

    private List<String> getMicroServiceNames(@NonNull Map<String, Object> model) {
        return (List<String>) model.get("microServiceNames");
    }

    @NonNull
    private Map<String, Object> getMicroServiceByName(@NonNull String name, @NonNull Map<String, Object> model) {
        return (Map<String, Object>) model.get(name);
    }

    @NonNull
    private Map<String, Object> resolveMicroServiceModel(@NonNull Service service, @NonNull Map<String, Object> model) {
        GeneratorMetadata metadata = this.metadataProvider.get();
        Map<String, Object> serviceModel = (Map<String, Object>) model.get(service.getName());

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

        resolveMicroServiceBuildProperties(serviceModel);
        resolveMicroServiceDependency(serviceModel, model.get("bootVersion").toString());

        serviceModel.put("applicationName", metadata.getConfiguration().generateApplicationName(service.getName()));
        serviceModel.put("applicationImports", service.getImports());
        serviceModel.put("applicationAnnotations", service.getAnnotations());

        return serviceModel;
    }

    private void generateBaseDirectory(@NonNull File rootDir, @NonNull ProjectRequest request,
                                       @NonNull Map<String, Object> model) {
        Assert.hasText(request.getBaseDir(), "Basedir should have text.");

        File dir = new File(rootDir, request.getBaseDir());
        dir.mkdir();

        String pom = new String(doGenerateMavenPom(model, "parent-pom.xml"));
        writeText(new File(dir, "pom.xml"), pom);

        writeMavenWrapper(dir);
        write(new File(dir, ".gitignore"), "gitignore.tmpl", model);
    }

    private File generateRootProject(@NonNull ProjectRequest request, @NonNull Map<String, Object> model) {
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

    private void generateCloudConfigSourceCode(@NonNull File serviceDir, @NonNull Map<String, Object> serviceModel,
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

    private void generateCloudGatewaySourceCode(File serviceDir, File src, Map<String, Object> serviceModel,
                                                Map<String, Object> model) {
        write(new File(src, "CustomWebFilter.java"), "CustomWebFilter.java", serviceModel);
        File staticResources = new File(serviceDir, "src/main/resources/static");

        staticResources.mkdirs();

        writeText(new File(staticResources, "index.html"), templateRenderer.process("GatewayIndex.tmpl", model));
        writeText(new File(staticResources, "bulma.min.css"), templateRenderer.process("bulma.min.css", null));
    }

    private void generateMicroServiceSourceCode(@NonNull File serviceDir, @NonNull Map<String, Object> serviceModel,
                                                @NonNull Map<String, Object> model) {
        List<String> dependencies = (List<String>) serviceModel.get("dependencyIdList");
        String appName = serviceModel.get("applicationName").toString();
        String serviceName = serviceModel.get("name").toString();
        File resources = new File(serviceDir, "src/main/resources");
        File src = new File(new File(serviceDir, "src/main/java"),
                serviceModel.get("packageName").toString().replace(".", "/"));
        src.mkdirs();
        resources.mkdirs();

        if (serviceName.equalsIgnoreCase(ServiceNames.CLOUD_HYSTRIX_DASHBOARD)) {
            String prefix = "cloud-hystrix-dashboard/";
            write(new File(src, appName + ".java"), prefix + "CloudHystrixDashboardApplication.java", serviceModel);
            write(new File(src, "MockStreamServlet.java"), prefix + "MockStreamServlet.java", serviceModel);
            writeTextResource(resources, "hystrix.stream", "hystrix.stream");
        } else {
            write(new File(src, appName + ".java"), "Application.java", serviceModel);
        }

        if (dependencies.contains("web")) {
            new File(serviceDir, "src/main/resources/templates").mkdirs();
            new File(serviceDir, "src/main/resources/static").mkdirs();
            write(new File(src, "Controller.java"), "Controller.java", serviceModel);
        }

        if (serviceName.equalsIgnoreCase(ServiceNames.CLOUD_CONFIG_SERVER)) {
            this.generateCloudConfigSourceCode(serviceDir, serviceModel, model);
        } else if (serviceName.equalsIgnoreCase(ServiceNames.CLOUD_GATEWAY)) {
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

    private void generateMicroService(@NonNull String serviceName, @NonNull Map<String, Object> model,
                                      @NonNull File projectDir) {
        Map<String, Service> microServicesMap = (Map<String, Service>) model.get("microServicesMap");
        File serviceDir = new File(projectDir, serviceName);
        Map<String, Object> serviceModel = resolveMicroServiceModel(microServicesMap.get(serviceName), model);

        serviceDir.mkdir();

        generateMicroServiceDockerfile(serviceDir, serviceModel);
        generateMicroServicePom(serviceDir, serviceModel);
        generateMicroServiceCode(serviceDir, serviceModel, model);
    }

    private void generateMicroServicesProject(@NonNull List<MicroService> microServices, @NonNull File projectDir,
                                              @NonNull Map<String, Object> model) {
        microServices.forEach(s -> generateMicroService(s.getName(), model, projectDir));
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

    public File generate(@NonNull ProjectRequest request) {
        Map<String, Object> model = this.resolveModel(request);
        File rootDir = this.generateRootProject(request, model);
        File projectDir = new File(rootDir, request.getBaseDir());

        generateMicroServicesProject(request.getMicroServices(), projectDir, model);
        generateDockerDirectory(projectDir, model);

        return rootDir;
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
            this.temporaryDirectory = new File(this.tmpdir);
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

    private void resolveBomsModel(@NonNull ProjectRequest request, @NonNull Map<String, Object> model) {
        Map<String, BillOfMaterials> boms = getBoms(request.getBootVersion());

        model.put("hasBoms", true);
        model.put("resolvedBoms", boms.values().stream().sorted(Comparator.comparing(BillOfMaterials::getOrder))
                .map(this::getBomModel).collect(Collectors.toList()));
    }

    private void resolveBuildPropertiesModel(@NonNull ProjectRequest request, @NonNull Map<String, Object> model) {
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

    private void resolveRequestMicroServicesModel(@NonNull ProjectRequest request,
                                                  @NonNull Map<String, Object> model) {
        List<String> microServiceNames = new ArrayList<>();
        List<Service> microServices = new ArrayList<>();
        Map<String, Service> microServicesMap = new HashMap<>();
        List<Map<String, Object>> azureServices = new ArrayList<>();

        request.getMicroServices().forEach(s -> microServiceNames.add(s.getName()));

        request.getMicroServices().forEach(s -> {
            final Map<String, Object> serviceModel = new HashMap<>();
            final Service service = ServiceNames.toService(s);

            model.put(s.getName(), serviceModel);
            microServices.add(service);
            microServicesMap.put(s.getName(), service);

            serviceModel.put("port", s.getPort());
            serviceModel.put("name", s.getName());
            serviceModel.put("modules", s.getModules());

            if (isAzureServices(s.getName())) {
                azureServices.add(serviceModel);
            }

            if (s.getName().equalsIgnoreCase(ServiceNames.CLOUD_GATEWAY)) {
                serviceModel.put("azureServices", azureServices);
            }
        });

        model.put("microServices", microServices);
        model.put("microServicesMap", microServicesMap);
        model.put("microServiceNames", microServiceNames);
        model.put("azureServices", azureServices);
    }

    private void resolveRequestModel(@NonNull ProjectRequest request, @NonNull Map<String, Object> model) {
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

    private Map<String, Object> resolveModel(@NonNull ProjectRequest request) {
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

    private byte[] doGenerateMavenPom(Map<String, Object> model, String template) {
        return this.templateRenderer.process(template, model).getBytes();
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
            writeBinary(target, this.projectResourceLocator.getBinaryResource("classpath:project/" + location));
        } else {
            writeText(target, this.projectResourceLocator.getTextResource("classpath:project/" + location));
        }

        return target;
    }

    /**
     * Write a template with model to a target file
     *
     * @param target target file
     */
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
}
