package io.jenkins.plugins.sample;

import java.util.UUID;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class Category extends AbstractDescribableImpl<Category> {

    private String name;
    private final String uuid;

    @DataBoundConstructor
    public Category(String name, String uuid) {
        this.name = name;
        this.uuid = uuid != null && !uuid.isEmpty() ? uuid : UUID.randomUUID().toString();
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Category> {
        @Override public String getDisplayName() {
            return "Category";
        }
    }
}
