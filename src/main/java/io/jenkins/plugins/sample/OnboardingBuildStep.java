package io.jenkins.plugins.sample;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
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
            ((DescriptorImpl)getDescriptor()).addLastCategory(run, category);
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
        private List<RunWithCategory> lastRuns;
        private Map<Category, String> lastRunPerCategory;

        public DescriptorImpl() {
            load();
        }

        public List<RunWithCategory> getLastRuns() {
            return lastRuns;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Onboarding step";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public void addLastCategory(Run<?, ?> run, Category category){
            RunWithCategory runWithCategory = new RunWithCategory(run.getExternalizableId(), category);
            if (lastRuns == null){
                lastRuns = new LinkedList<>();
            } else if (lastRuns.size() >= 5){
                lastRuns.remove(0);
            }
            lastRuns.add(runWithCategory);
            if (lastRunPerCategory == null){
                lastRunPerCategory = new HashMap<>();
            }
            lastRunPerCategory.put(category, run.getParent().getFullName()); //maybe just use the UUID as key? immutable
            save();
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

    public static class RunWithCategory{
        private String runId;
        private Category category;

        public RunWithCategory(String runId, Category category) {
            this.runId = runId;
            this.category = category;
        }

        public String getRunId() {
            return runId;
        }

        public void setRunId(String runId) {
            this.runId = runId;
        }

        public Category getCategory() {
            return category;
        }

        public void setCategory(Category category) {
            this.category = category;
        }

        public String getRunUrl(){
            Run<?, ?> run = Run.fromExternalizableId(this.getRunId());
            return run == null ? null : run.getAbsoluteUrl();
        }
    }

}
