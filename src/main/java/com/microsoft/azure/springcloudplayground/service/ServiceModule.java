package com.microsoft.azure.springcloudplayground.service;

import com.microsoft.azure.springcloudplayground.metadata.Describable;
import com.microsoft.azure.springcloudplayground.metadata.MetadataElement;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ServiceModule extends MetadataElement implements Describable {

    @Setter
    private String description;

    @Override
    public String getDescription() {
        return this.description;
    }

    public ServiceModule(ServiceModule module) {
        super(module);
        this.description = module.description;
    }

    public ServiceModule(String id, String name, String description) {
        super(id, name);
        this.description = description;
    }

    @Override
    public String toString() {
        return "ServiceModule{" + "id='" + getId() + '\'' + ", name='" + getName()
                + '\'' + ", description='" + this.getDescription() + '\'' + '}';
    }

    public static ServiceModule withId(String id, String name, String description) {
        ServiceModule module = new ServiceModule();

        module.setId(id);
        module.setName(name);
        module.setDescription(description);

        return module;
    }
}
