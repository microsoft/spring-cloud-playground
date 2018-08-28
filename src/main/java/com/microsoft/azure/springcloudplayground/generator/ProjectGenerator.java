package com.microsoft.azure.springcloudplayground.generator;

import com.microsoft.azure.springcloudplayground.dependency.Dependency;
import com.microsoft.azure.springcloudplayground.dependency.DependencyNames;
import com.microsoft.azure.springcloudplayground.metadata.*;
import com.microsoft.azure.springcloudplayground.service.Service;
import com.microsoft.azure.springcloudplayground.service.ServiceNames;
import com.microsoft.azure.springcloudplayground.util.TemplateRenderer;
import com.microsoft.azure.springcloudplayground.util.Version;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class ProjectGenerator {

    @Autowired
    private GeneratorMetadataProvider metadataProvider;

    @Autowired
    private TemplateRenderer templateRenderer;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    private ProjectResourceLocator projectResourceLocator;

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
    private void resolveMicroServiceDependency(@NonNull Service service, @NonNull Map<String, Object> serviceModel,
                                               @NonNull String bootVersion) {
        Version version = Version.parse(bootVersion);
        GeneratorMetadata metadata = this.metadataProvider.get();
        List<String> dependencyIdList = new ArrayList<>(service.getDependencies());
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
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMicroServiceModelByName(@NonNull String name, @NonNull Map<String, Object> model) {
        Assert.isInstanceOf(Map.class, model.get(name));

        return (Map<String, Object>) model.get(name);
    }

    @NonNull
    private Service getService(@NonNull Map<String, Object> serviceModel) {
        Assert.isInstanceOf(Service.class, serviceModel.get("service"));

        return (Service) serviceModel.get("service");
    }

    @NonNull
    private String getServiceName(@NonNull Map<String, Object> serviceModel) {
        Assert.isInstanceOf(String.class, serviceModel.get("name"));

        return serviceModel.get("name").toString();
    }

    @NonNull
    private String getServiceApplicationName(@NonNull Map<String, Object> serviceModel) {
        Assert.isInstanceOf(String.class, serviceModel.get("applicationName"));

        return serviceModel.get("applicationName").toString();
    }

    private void resolveMicroServiceModel(@NonNull Map<String, Object> serviceModel, @NonNull Map<String, Object> model) {
        GeneratorMetadata metadata = this.metadataProvider.get();
        Service service = getService(serviceModel);

        log.info("Resolving micro service {} model.", service.getName());

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
        resolveMicroServiceDependency(service, serviceModel, model.get("bootVersion").toString());

        serviceModel.put("applicationName", metadata.getConfiguration().generateApplicationName(service.getName()));
        serviceModel.put("applicationImports", service.getImports());
        serviceModel.put("applicationAnnotations", service.getAnnotations());
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

        log.info("Generate root directory {}.", request.getBaseDir());

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
        String dockerTemplate = Paths.get(dockerFile).toString();

        writeText(new File(serviceDir, dockerFile), templateRenderer.process(dockerTemplate, serviceModel));
    }

    private void generateMicroServicePom(@NonNull File serviceDir, @NonNull Map<String, Object> serviceModel) {
        String pom = new String(doGenerateMavenPom(serviceModel, "module-pom.xml"));
        writeText(new File(serviceDir, "pom.xml"), pom);
    }

    private void generateCloudConfigSourceCode(@NonNull File resourceDir, @NonNull Map<String, Object> serviceModel) {
        File shared = new File(resourceDir, "shared");

        shared.mkdir();

        String template = ServicePropsResolver.generateApplicationProps(getService(serviceModel));
        writeText(new File(shared, "application.properties"), templateRenderer.processFromString(template, null));

        List<Map<String, Object>> models = (List<Map<String, Object>>) serviceModel.get("configuredServiceModels");

        Assert.notNull(models, "models should not be null");
        Assert.isTrue(!models.contains(serviceModel), "should not contain config model");

        models.forEach(m -> {
            final Service service = getService(m);
            final String propTemplate = ServicePropsResolver.generateApplicationProps(service);
            writeText(new File(shared, service.getName() + ".properties"), templateRenderer.processFromString(propTemplate, m));
        });
    }

    private void generateCloudGatewaySourceCode(@NonNull File resourceDir, @NonNull File src,
                                                @NonNull Map<String, Object> serviceModel) {
        write(new File(src, "CustomWebFilter.java"), "CustomWebFilter.java", serviceModel);
        File staticResources = new File(resourceDir, "static");

        staticResources.mkdirs();

        writeText(new File(staticResources, "index.html"), templateRenderer.process("GatewayIndex.tmpl", serviceModel));
        writeText(new File(staticResources, "bulma.min.css"), templateRenderer.process("bulma.min.css", null));
    }

    private void generateHystrixDashboardSourceCode(@NonNull File srcDir, @NonNull File resourcesDir,
                                                    @NonNull Map<String, Object> serviceModel) {
        String prefix = "cloud-hystrix-dashboard";
        String applicationName = "CloudHystrixDashboardApplication.java";
        String mockStreamName = "MockStreamServlet.java";

        write(new File(srcDir, applicationName), prefix + "/" + applicationName, serviceModel);
        write(new File(srcDir, mockStreamName), prefix + "/" + mockStreamName, serviceModel);
        writeTextResource(resourcesDir, "hystrix.stream", "hystrix.stream");
    }

    private void generateAzureServiceSourceCode(@NonNull File srcDir, @NonNull Map<String, Object> serviceModel) {
        Service service = getService(serviceModel);
        String appName = serviceModel.get("applicationName").toString();

        service.getModules().forEach(m -> writeAllJavaFilesToDirectory(srcDir, m.getName(), serviceModel));

        write(new File(srcDir, appName + ".java"), "Application.java", serviceModel);
    }

    private void generateFrontEndFiles(@NonNull File resourceDir, @NonNull Map<String, Object> serviceModel) {
        // Write all files under templates/${module_name}/front to generated src/main/resources
        Service service = getService(serviceModel);

        service.getModules().stream().filter(m -> m.hasFront()).forEach(m ->
                writeAllFilesToDirectory(resourceDir, m.getName() + "/front", serviceModel));
    }

    private void generateInfrastructureServiceSourceCode(@NonNull File srcDir, @NonNull File resourceDir,
                                                         @NonNull Map<String, Object> serviceModel) {
        String serviceName = getServiceName(serviceModel);
        String appName = getServiceApplicationName(serviceModel);

        if (serviceName.equalsIgnoreCase(ServiceNames.CLOUD_HYSTRIX_DASHBOARD)) {
            generateHystrixDashboardSourceCode(srcDir, resourceDir, serviceModel);
        } else if (serviceName.equalsIgnoreCase(ServiceNames.CLOUD_CONFIG_SERVER)) {
            generateCloudConfigSourceCode(resourceDir, serviceModel);
        } else if (serviceName.equalsIgnoreCase(ServiceNames.CLOUD_GATEWAY)) {
            generateCloudGatewaySourceCode(resourceDir, srcDir, serviceModel);
        }

        write(new File(srcDir, appName + ".java"), "Application.java", serviceModel);
    }

    private void generateMicroServiceBootstrapPropsFile(@NonNull File resourcesDir, @NonNull Map<String, Object> serviceModel) {
        String template = ServicePropsResolver.generateBootstrapProps(getService(serviceModel));
        writeText(new File(resourcesDir, "bootstrap.properties"), templateRenderer.processFromString(template, serviceModel));
    }

    private void generateMicroServiceWebResource(@NonNull File serviceDir, @NonNull File srcDir,
                                                 @NonNull Map<String, Object> serviceModel) {
        if (getService(serviceModel).getDependencies().contains(DependencyNames.WEB)) {
            new File(serviceDir, "src/main/resources/templates").mkdirs();
            new File(serviceDir, "src/main/resources/static").mkdirs();
            write(new File(srcDir, "Controller.java"), "Controller.java", serviceModel);
        }
    }

    private void generateMicroServiceSourceCode(@NonNull File serviceDir, @NonNull Map<String, Object> serviceModel) {
        String serviceName = getServiceName(serviceModel);
        File resourcesDir = new File(serviceDir, "src/main/resources");
        File srcDir = new File(new File(serviceDir, "src/main/java"),
                serviceModel.get("packageName").toString().replace(".", "/"));

        srcDir.mkdirs();
        resourcesDir.mkdirs();

        if (ServiceNames.isAzureService(serviceName)) {
            generateAzureServiceSourceCode(srcDir, serviceModel);
            generateFrontEndFiles(resourcesDir, serviceModel);
        } else {
            generateInfrastructureServiceSourceCode(srcDir, resourcesDir, serviceModel);
        }

        generateMicroServiceWebResource(serviceDir, srcDir, serviceModel);
        generateMicroServiceBootstrapPropsFile(resourcesDir, serviceModel);
    }

    private void generateMicroServiceTestCode(@NonNull File serviceDir, @NonNull Map<String, Object> serviceModel) {
        String applicationName = serviceModel.get("applicationName").toString();
        File test = new File(new File(serviceDir, "src/test/java"),
                serviceModel.get("packageName").toString().replace(".", "/"));
        test.mkdirs();

        write(new File(test, applicationName + "Tests.java"), "ApplicationTests.java", serviceModel);
    }

    private void generateMicroServiceCode(@NonNull File serviceDir, @NonNull Map<String, Object> serviceModel) {
        generateMicroServiceSourceCode(serviceDir, serviceModel);
        generateMicroServiceTestCode(serviceDir, serviceModel);
    }

    private void generateMicroService(@NonNull String serviceName, @NonNull Map<String, Object> model,
                                      @NonNull File projectDir) {
        File serviceDir = new File(projectDir, serviceName);
        Map<String, Object> serviceModel = getMicroServiceModelByName(serviceName, model);

        log.info("Generate micro service {} project.", serviceName);

        serviceDir.mkdir();

        resolveMicroServiceModel(serviceModel, model);

        generateMicroServiceDockerfile(serviceDir, serviceModel);
        generateMicroServicePom(serviceDir, serviceModel);
        generateMicroServiceCode(serviceDir, serviceModel);
    }

    private void generateMicroServicesProject(@NonNull List<MicroService> microServices, @NonNull File projectDir,
                                              @NonNull Map<String, Object> model) {
        microServices.forEach(s -> generateMicroService(s.getName(), model, projectDir));
    }

    private void generateDockerDirectory(@NonNull File projectDir, @NonNull Map<String, Object> model) {
        String docker = "docker";
        File dockerDir = new File(projectDir, docker);

        log.info("Generate docker directory {}.", docker);

        dockerDir.mkdirs();

        writeAllFilesToDirectory(dockerDir, docker, model);
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

    private Map<String, Object> createMicroServiceModel(@NonNull MicroService service) {
        Map<String, Object> serviceModel = new HashMap<>();

        serviceModel.put("name", service.getName());
        serviceModel.put("port", service.getPort());
        serviceModel.put("modules", service.getModules());

        return serviceModel;
    }

    private void resolveRequestMicroServicesModel(@NonNull ProjectRequest request, @NonNull Map<String, Object> model) {
        List<Service> microServices = new ArrayList<>();
        List<Service> azureServices = new ArrayList<>();
        List<Map<String, Object>> configuredServiceModels = new ArrayList<>();
        AtomicInteger atomicInteger = new AtomicInteger(0);

        model.put("microServices", microServices);

        request.getMicroServices().forEach(s -> {
            final Map<String, Object> serviceModel = createMicroServiceModel(s);
            final Service service = ServiceNames.toService(s);

            serviceModel.put("service", service);
            model.put(s.getName(), serviceModel);
            microServices.add(service);

            if (ServiceNames.isAzureService(s.getName())) {
                // serviceIndex is used to generate spring.cloud.gateway.routes index for spring cloud gateway properties
                service.setIndex(atomicInteger.getAndIncrement());
                azureServices.add(service);
            }

            if (s.getName().equalsIgnoreCase(ServiceNames.CLOUD_GATEWAY)) {
                serviceModel.put("azureServices", azureServices);
                serviceModel.put("microServices", microServices);
            }

            if (s.getName().equalsIgnoreCase(ServiceNames.CLOUD_CONFIG_SERVER)) {
                serviceModel.put("configuredServiceModels", configuredServiceModels);
            } else {
                configuredServiceModels.add(serviceModel);
            }
        });
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

        log.info("Resolving project {}.", request.getName());

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

        writeTextResource(wrapperDir, "maven-wrapper.properties", "maven/wrapper/maven-wrapper.properties");
        writeBinaryResource(wrapperDir, "maven-wrapper.jar", "maven/wrapper/maven-wrapper.jar");
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

    private void writeAllJavaFilesToDirectory(@NonNull File targetDir, @NonNull String templateDir,
                                              @NonNull Map<String, Object> model) {
        this.writeTemplateDirectory(targetDir, templateDir, model, ".java");
    }

    private void writeAllFilesToDirectory(@NonNull File targetDir, @NonNull String templateDir,
                                          @NonNull Map<String, Object> model) {
        this.writeTemplateDirectory(targetDir, templateDir, model, "");
    }

    /**
     * Write all templates under template directory with model to target directory
     *
     * @param targetDir   target directory
     * @param templateDir template directory
     */
    private void writeTemplateDirectory(File targetDir, String templateDir, Map<String, Object> model,
                                        @NonNull String suffix) {
        String pattern = "classpath:templates/" + templateDir + "/*" + suffix;

        try {
            Resource[] templates = loadResources(pattern);
            Arrays.stream(templates).forEach(t -> write(new File(targetDir, t.getFilename()),
                    templateDir + "/" + t.getFilename(), model));
        } catch (IOException e) {
            log.warn(String.format("Failed to find resources match pattern '%s'", pattern), e);
        }
    }

    private Resource[] loadResources(String pattern) throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern);
    }

    private void addTempFile(String group, File file) {
        this.temporaryFiles.computeIfAbsent(group, (key) -> new ArrayList<>()).add(file);
    }

    private static List<Dependency> filterDependencies(List<Dependency> dependencies, String scope) {
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
