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
	private String clientID;
	
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
    public String getCientID() {
    	return this.clientID;
    }
    public void setClientID(String clientID) {
    	this.clientID = clientID;
    	save();
    }
    public Secret getCientSecret() {
    	return this.clientSecret;
    }
    public void setClientSecret(Secret clientSecret) {
    	this.clientSecret = clientSecret;
    	save();
    }
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) {
        req.bindJSON(this, formData);
        save();
        return true;
    			
    }
    
}