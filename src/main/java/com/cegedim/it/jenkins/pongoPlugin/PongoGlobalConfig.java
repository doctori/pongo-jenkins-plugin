package com.cegedim.it.jenkins.pongoPlugin;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class PongoGlobalConfig extends GlobalConfiguration {
	private String webhookURL = "";
	private String authToken = "";
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
    	this.webhookURL = webhookURL;
    	save();
    }
    public String getAuthToken() {
    	return this.authToken;
    }
    public void setAuthToken(String authToken) {
    	this.authToken = authToken;
    	save();
    }
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) {
    	this.webhookURL = (String) formData.get("webhookURL");
    	this.authToken  = (String) formData.get("authToken");
    	save();
    	return true;
    			
    }
    
}