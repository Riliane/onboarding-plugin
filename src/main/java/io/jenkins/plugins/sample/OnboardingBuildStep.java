package io.jenkins.plugins.sample;

import java.util.List;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;

public class OnboardingBuildStep extends Builder implements SimpleBuildStep {
    private Category category;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getCategoryUUID() {
        return category.getUuid();
    }

    @DataBoundConstructor
    public OnboardingBuildStep(String categoryUUID) {
        List<Category> categories = SampleConfiguration.get().getCategories();
        categories.stream()
                  .filter(category1 -> category1.getUuid().equalsIgnoreCase(categoryUUID))
                  .findFirst()
                  .ifPresent(value -> this.category = value);
    }

//    @DataBoundSetter
//    public void setCategoryUUID(String categoryUUID) {
//        List<Category> categories = SampleConfiguration.get().getCategories();
//        categories.stream()
//                  .filter(category1 -> category1.getUuid().equalsIgnoreCase(categoryUUID))
//                  .findFirst()
//                  .ifPresent(value -> this.category = value);
//    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env,
                        @NonNull Launcher launcher, @NonNull TaskListener listener) throws AbortException {
        try{
            listener.getLogger().println("Onboarding step: category " + category.getName());
        } catch (Exception e) {
            setFailed(run, listener, e);
        }
    }

    private void setFailed(@NonNull Run<?, ?> run, TaskListener listener, Exception e) throws AbortException {
        listener.getLogger().println("Problem executing onboarding step: " + e.getMessage());
        e.printStackTrace();
        throw new AbortException("Failed to execute onboarding step");
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Onboarding step";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return FreeStyleProject.class.isAssignableFrom(jobType);
        }

        public ListBoxModel doFillCategoryUUIDItems(){
            ListBoxModel model = new ListBoxModel();
            List<Category> categories = SampleConfiguration.get().getCategories();
            for (Category value : categories) {
                model.add(value.getName(), value.getUuid()); //can add UUID as value and then search by UUID
                // but nothing except name is useful in this case
            }
            return model;

        }
    }

}
