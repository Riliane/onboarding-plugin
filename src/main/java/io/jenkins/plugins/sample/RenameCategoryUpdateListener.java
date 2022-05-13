package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;

@Extension
public class RenameCategoryUpdateListener extends ItemListener {

    @Override
    public void onLocationChanged(Item item, String oldFullName, String newFullName) {
        if (SampleConfiguration.get().getLastCategorizedJobId().equals(oldFullName)){
            SampleConfiguration.get().setLastCategorizedJobId(newFullName);
        }
        OnboardingBuildStep.DescriptorImpl.get().updateOnJobRename(oldFullName, newFullName);
    }
}
