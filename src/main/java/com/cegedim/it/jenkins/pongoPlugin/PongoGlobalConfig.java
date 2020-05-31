package com.cegedim.it.jenkins.pongoPlugin;

import hudson.Extension;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class PongoGlobalConfig extends GlobalConfiguration {
	private String webhookURL = "";
	private Secret clientSecret;
	private String clientID = "";
	private boolean enabled = false;
	
	public PongoGlobalConfig() {
		load();
	}
    public static PongoGlobalConfig getInstance() {
        return GlobalConfiguration.all().get(PongoGlobalConfig.class);
    }
    public String getWebhookURL() {
    	return this.webhookURL;
    }
    public void setWebhookURL(String webhookURL) {
    	this.webhookURL = webhookURL.trim();
    	save();
    }
    public String getClientID() {
    	return this.clientID;
    }
    public void setClientID(String clientID) {
    	this.clientID = clientID;
    	save();
    }
    public Secret getClientSecret() {
    	return this.clientSecret;
    }
    public void setClientSecret(Secret clientSecret) {
    	this.clientSecret = clientSecret;
    	save();
    }
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) {
        req.bindJSON(this, formData);
        save();
        return true;
    			
    }

    
}