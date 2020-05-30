package com.cegedim.it.jenkins.pongoPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;


public class PongoNotifyClient{

	private static final Logger log = LoggerFactory.getLogger(PongoNotifyClient.class);
	
    private static CloseableHttpClient httpclient = HttpClients.createDefault();
    
    public static String getAccessToken(String clientID, hudson.util.Secret clientSecret) {
    	URI issuerURI;
        URL providerConfigurationURL;
        InputStream configStream;
        OIDCProviderMetadata providerMetadata;
        // Oauth Config
        try {
        	issuerURI = new URI("https://accounts.cegedim.cloud");
        }catch(URISyntaxException e) {
        	log.error("Could not parse the issuer URL ");
        	return "";
        }
        try {
            providerConfigurationURL = issuerURI.resolve("/auth/realms/cloud/.well-known/openid-configuration").toURL();
        }catch(java.net.MalformedURLException e) {
        	log.error("Could not access the openid-configuration URL");
        	return "";
        }
        
        try {
        	configStream = providerConfigurationURL.openStream();
        }catch(java.io.IOException e) {
        	log.error("Could not access the issuer URL {} because {}",e.getMessage(),e);
        	return "";
        }
        // Read all data from URL
        String providerInfo = null;
        try (java.util.Scanner s = new java.util.Scanner(configStream,"UTF-8")) {
          providerInfo = s.useDelimiter("\\A").hasNext() ? s.next() : "";
        }
        try {
        	providerMetadata = OIDCProviderMetadata.parse(providerInfo);
        }catch(ParseException e) {
        	log.error("Could not parse the oidc configuration");
        	return "";
        }
     // Construct the client credentials grant
        AuthorizationGrant clientGrant = new ClientCredentialsGrant();
        // The credentials to authenticate the client at the token endpoint
        ClientAuthentication clientAuth = new ClientSecretBasic(new ClientID(clientID), new Secret(clientSecret.getPlainText()));
        TokenRequest request = new TokenRequest(providerMetadata.getTokenEndpointURI(),clientAuth,clientGrant);
        TokenResponse tokenResponse;
		try {
			tokenResponse = TokenResponse.parse(request.toHTTPRequest().send());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}

        if (! tokenResponse.indicatesSuccess()) {
            // We got an error response...
            TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
        }

        AccessTokenResponse successResponse = tokenResponse.toSuccessResponse();

        // Get the access token, the server may also return a refresh token
        AccessToken accessToken = successResponse.getTokens().getAccessToken();
        return accessToken.toAuthorizationHeader();
    }
    
    public static boolean notify(String url, String clientID, hudson.util.Secret clientSecret, String message) {
        boolean success = false;
        log.info("Send notification to {}, message: {}", url, message);
        if(url == null || url.isEmpty()){
            log.error("Invalid URL: {}", url);

            return success;
        }
        
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(url);
            httpPost.setHeader("content-type", "application/json;charset=UTF-8");
            // TODO : better error handling
            httpPost.setHeader("Authorization", getAccessToken(clientID,clientSecret));
            
            if(message != null && !message.isEmpty()){
                StringEntity stringEntity = new StringEntity(message, "UTF-8");
                stringEntity.setContentEncoding("UTF-8");
                httpPost.setEntity(stringEntity);
            }
            CloseableHttpResponse response = httpclient.execute(httpPost);
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            log.info("Response code: {}", responseCode);
            if (entity != null) {
                log.info("Response entity: {}", EntityUtils.toString(entity));
            }
            if(responseCode == HttpStatus.SC_OK){
                success = true;
            }
        } catch (IllegalArgumentException e1){
            log.error("Invalid URL: {}", url);
        } catch (IOException e2) {
            log.error("Error posting to Zoom, url: {}, message: {}", url, message);
        } finally {
            if(httpPost != null){
                httpPost.releaseConnection();
            }
        }
        log.info("Notify success? {}", success);
        return success;
    }
}