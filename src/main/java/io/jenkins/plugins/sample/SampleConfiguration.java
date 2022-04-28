package io.jenkins.plugins.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import net.sf.json.JSONObject;

/**
 * Example of Jenkins global configuration.
 */
@Extension
public class SampleConfiguration extends GlobalConfiguration {

    /** @return the singleton instance */
    public static SampleConfiguration get() {
        return ExtensionList.lookupSingleton(SampleConfiguration.class);
    }

    private String label;
    private String description;
    private List<Category> categories = Collections.emptyList();

    public SampleConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
    }

    /** @return the currently configured label, if any */
    public String getLabel() {
        return label;
    }

    /**
     * Together with {@link #getLabel}, binds to entry in {@code config.jelly}.
     * @param label the new value of this field
     */
    @DataBoundSetter
    public void setLabel(String label) {
        if (isLettersSpaces(label)) {
            this.label = label;
            save();
        }
    }

    public String getDescription() {
        return description;
    }

    @DataBoundSetter
    public void setDescription(String description) {
        this.description = description;
        save();
    }

    public List<Category> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    @DataBoundSetter
    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? new ArrayList<Category>(categories) : Collections.<Category>emptyList();
    }

    public FormValidation doCheckLabel(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify a name.");
        }
        if (!isLettersSpaces(value)){
            return FormValidation.error("The name must only contain letters and spaces.");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckDescription(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify a description.");
        }
        return FormValidation.ok();
    }

    private boolean isLettersSpaces(String label) {
        Pattern p = Pattern.compile("^[ A-Za-z]+$");
        return p.matcher(label).matches();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        if (!json.containsKey("categories")){
            this.categories = Collections.<Category>emptyList();
        }
        return super.configure(req, json);
    }
}
