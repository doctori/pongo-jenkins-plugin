package com.cegedim.it.jenkins.pongoPlugin;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ModelObject;
import hudson.model.TaskListener;
import hudson.security.Permission;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PongoNotifier extends Notifier implements SimpleBuildStep {
	
	
    private String webhookURL;
	private Secret clientSecret;
	private String clientID;
	private boolean enabled;
    private static final Logger log = LoggerFactory.getLogger(PongoNotifier.class);
    
    
    @DataBoundConstructor
    public PongoNotifier(String webhookUrl, String clientID, Secret clientSecret,boolean enabled) {
    	this.webhookURL         = webhookUrl;
    	this.clientID          = clientID;
    	this.clientSecret      = clientSecret;
    	this.enabled           = enabled;
    }

    public String getWebhookURL() {
    	return this.webhookURL;
    }
    public String getCientID() {
    	return this.clientID;
    }
    public String getCientSecret() {
    	return this.clientID;
    }
    public boolean isEnabled() {
		return enabled;
	}
    
    @DataBoundSetter
    public void setClientID(String clientID) {
    	this.clientID = clientID;
    }
    @DataBoundSetter
    public void setClientSecret(Secret clientSecret) {
    	this.clientSecret = clientSecret;
    }
    @DataBoundSetter
    public void setWebhookUrl(String webhookUrl) {
    	this.webhookURL = webhookUrl;
    }

    @DataBoundSetter
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
    public boolean prebuild(Run<?, ?> build, TaskListener listener) {
        return true;
    }
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener)  throws InterruptedException, IOException{
    	if(this.enabled) {
	        log.info("Perform: {}", build.getDisplayName());
	        listener.getLogger().println("---------------------- Perform ----------------------");
	        MessageBuilder messageBuilder = new MessageBuilder(this, build, listener);
	        PongoNotifyClient.notify(this.webhookURL, this.clientID,this.clientSecret, messageBuilder.build());
    	}

    }

        
    


	@Symbol("pongo")
    @Extension(optional = true)
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder>  {
        
        @Override
        public String getDisplayName() {
            return Messages.PongoNotifier_DescriptorImpl_DisplayName();
        }
        public FormValidation doCheckURL(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.PongoNotifier_DescriptorImpl_errors_missingName());
            if (value.length() < 4)
                return FormValidation.warning(Messages.PongoNotifier_DescriptorImpl_warnings_tooShort());
            return FormValidation.ok();
        }

       
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }


    }

}