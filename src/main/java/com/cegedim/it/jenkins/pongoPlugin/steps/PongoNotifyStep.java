package com.cegedim.it.jenkins.pongoPlugin.steps;

import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

import com.cegedim.it.jenkins.pongoPlugin.PongoNotifier;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.DataBoundConstructor;

public class PongoNotifyStep extends Step {
	private String url = "";
	private String message = "";
	private String authToken = "";

    // NOTE: Bump this number if the class evolves as a breaking change
    // (e.g. serializable fields change)
	private static final long serialVersionUID = 2;
	
	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new PongoNotifyStepExecution(this, context);
	}
	public String getMessage() {
		return message;
	}

	@DataBoundConstructor
	public PongoNotifyStep(String message) {
		this.message = message;
	}
	@DataBoundSetter
	public void setMessage(String message) {
		this.message = message;
	}
	private static class PongoNotifyStepExecution extends SynchronousNonBlockingStepExecution<Void> {
		private transient final PongoNotifyStep step;
        // NOTE: Bump this number if the class evolves as a breaking change
        // (e.g. serializable fields change)
        private static final long serialVersionUID = 2;
		public PongoNotifyStepExecution(@Nonnull PongoNotifyStep step, @Nonnull StepContext context) {
			super(context);
			this.step = step;
		}

		@Override
		protected Void run() throws Exception {
			PongoNotifier notifier = new PongoNotifier(this.step.url,this.step.authToken);
			notifier.perform(
					getContext().get(Run.class),
					getContext().get(FilePath.class),
					getContext().get(Launcher.class),
					getContext().get(TaskListener.class));

			return null;
		}
	}
}
