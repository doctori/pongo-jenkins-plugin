package com.cegedim.it.jenkins.pongoPlugin.steps;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.Permission;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;

import com.cegedim.it.jenkins.pongoPlugin.PongoGlobalConfig;
import com.cegedim.it.jenkins.pongoPlugin.PongoNotifier;
import com.cegedim.it.jenkins.pongoPlugin.PongoNotifyClient;
import com.google.common.collect.ImmutableSet;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import org.kohsuke.stapler.DataBoundConstructor;

public class PongoNotifyStep extends Step implements Serializable{
	private String url = "";

	private String message = "";
	private String clientID = "";
	private Secret clientSecret;

    // NOTE: Bump this number if the class evolves as a breaking change
    // (e.g. serializable fields change)
	private static final long serialVersionUID = 2;
	

	public String getClientID() {
		return clientID;
	}
	public Secret getClientSecret() {
		return clientSecret;
	}
	public String getMessage() {
		return message;
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
	public void setMessage(String message) {
		this.message = message;
	}

	@DataBoundConstructor
	public PongoNotifyStep(String message) {
		this.message = message;
	    // Get the webhook URL and token from configuration
	    this.url = PongoGlobalConfig.getInstance().getWebhookURL();
	    this.clientID = PongoGlobalConfig.getInstance().getClientID();
	    this.clientSecret = PongoGlobalConfig.getInstance().getClientSecret();
	}
	
	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new PongoNotifyStepExecution(this, context);
	}
	
	
	private static class PongoNotifyStepExecution extends SynchronousNonBlockingStepExecution<Void> {
		private final PongoNotifyStep step;
        // NOTE: Bump this number if the class evolves as a breaking change
        // (e.g. serializable fields change)
        private static final long serialVersionUID = 2;
		public PongoNotifyStepExecution(@Nonnull PongoNotifyStep step, @Nonnull StepContext context) {
			super(context);
			this.step = step;
		}

		@Override
		protected Void run() throws Exception {
			PongoNotifyClient.notify(this.step.url,this.step.clientID,this.step.clientSecret,this.step.message);
			return null;
		}
	}
	@Extension
    public static class DescriptorImpl extends StepDescriptor implements Serializable {

        /**
		 * 
		 */
		private static final long serialVersionUID = -6597131923883003169L;

		@Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "pongo";
        }

        @Override
        public String getDisplayName() {
            return "pongo";
        }

        @POST
        public FormValidation doTestConnection(@QueryParameter("webhookUrl") final String webhookUrl,
                                               @QueryParameter("clientID") final String clientID,
                                               @QueryParameter("clientSecret") final Secret clientSecret){
            Jenkins.get().checkPermission(Permission.CONFIGURE);
            if(PongoNotifyClient.notify(webhookUrl, clientID, clientSecret,"")){
                return FormValidation.ok("Connection is ok");
            }
            return FormValidation.error("Connect failed");
        }
    }
}
