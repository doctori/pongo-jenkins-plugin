package com.cegedim.it.jenkins.pongoPlugin.global;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.listeners.RunListener;
import hudson.util.Secret;
import hudson.model.TaskListener;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cegedim.it.jenkins.pongoPlugin.MessageBuilder;
import com.cegedim.it.jenkins.pongoPlugin.PongoGlobalConfig;
import com.cegedim.it.jenkins.pongoPlugin.PongoNotifier;
import com.cegedim.it.jenkins.pongoPlugin.PongoNotifyClient;

@Extension
public class GlobalRunListener extends RunListener<Run<?,?>> {
    private static final Logger log = LoggerFactory.getLogger(GlobalRunListener.class);
	@Override
	public void onCompleted(Run<?,?>build, @Nonnull TaskListener listener) {
     	 // Gets the full path of the build's project
	    // String path = build.getParent().getRelativeNameFrom(Jenkins.get());
	    // Get the webhook URL and token from configuration
	    String webhookURL = PongoGlobalConfig.getInstance().getWebhookURL();
	    String clientID = PongoGlobalConfig.getInstance().getClientID();
	    Secret clientSecret = PongoGlobalConfig.getInstance().getClientSecret();
        log.info("Perform: {}", build.getDisplayName());
        log.info("---------------------- Perform ----------------------");
        PongoNotifier notifier = new PongoNotifier(webhookURL, clientID, clientSecret );
        MessageBuilder messageBuilder = new MessageBuilder(notifier, build, listener);
        PongoNotifyClient.notify(webhookURL, clientID, clientSecret, messageBuilder.build());
	    		
	}

}
