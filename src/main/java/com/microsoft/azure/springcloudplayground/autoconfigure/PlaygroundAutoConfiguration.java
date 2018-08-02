package com.microsoft.azure.springcloudplayground.autoconfigure;

import com.microsoft.azure.springcloudplayground.generator.ProjectGenerator;
import com.microsoft.azure.springcloudplayground.generator.ProjectResourceLocator;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadata;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadataBuilder;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorMetadataProvider;
import com.microsoft.azure.springcloudplayground.metadata.GeneratorProperties;
import com.microsoft.azure.springcloudplayground.util.TemplateRenderer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

@Configuration
@EnableConfigurationProperties(GeneratorProperties.class)
@AutoConfigureAfter({CacheAutoConfiguration.class, JacksonAutoConfiguration.class, RestTemplateAutoConfiguration.class})
public class PlaygroundAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProjectGenerator projectGenerator() {
        return new ProjectGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public TemplateRenderer templateRenderer(Environment environment) {
        Binder binder = Binder.get(environment);
        boolean cache = binder.bind("spring.mustache.cache", Boolean.class).orElse(true);
        TemplateRenderer templateRenderer = new TemplateRenderer();
        templateRenderer.setCache(cache);
        return templateRenderer;
    }

    @Bean
    public ProjectResourceLocator projectResourceLocator() {
        return new ProjectResourceLocator();
    }

    @Bean
    @ConditionalOnMissingBean(GeneratorMetadataProvider.class)
    public GeneratorMetadataProvider generatorMetadataProvider(GeneratorProperties properties) {
        GeneratorMetadata metadata = GeneratorMetadataBuilder.fromGeneratorProperties(properties).build();
        return new DefaultGeneratorMetadataProvider(metadata);
    }

    @Configuration
    @ConditionalOnClass(javax.cache.CacheManager.class)
    static class CacheConfiguration {

        @Bean
        public JCacheManagerCustomizer cacheManagerCustomizer() {
            return (cm) -> {
                cm.createCache("generator.metadata", config().setExpiryPolicyFactory(
                        CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES)));
                cm.createCache("playground.dependency-metadata", config());
                cm.createCache("playground.project-resources", config());
            };
        }

        private MutableConfiguration<Object, Object> config() {
            return new MutableConfiguration<>().setStoreByValue(false)
                    .setManagementEnabled(true).setStatisticsEnabled(true);
        }
    }
}
