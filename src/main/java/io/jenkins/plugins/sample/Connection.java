package io.jenkins.plugins.sample;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.commons.codec.binary.Base64;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.util.FormValidation;
import hudson.util.Secret;

public class Connection extends AbstractDescribableImpl<Connection> {

    private String url;
    private String username;
    private Secret password;

    public String getUrl() {
        return url;
    }

    public String getUsername(){
        return username;
    }

    public Secret getPassword() {
        return password;
    }

    @DataBoundConstructor
    public Connection(String url, String password) {
        this.url = url;
        this.password = Secret.fromString(password);
    }

    public void setUsername(String username){
        if (isLettersOnly(username)){
            this.username = username;
        }
    }


    private static boolean isLettersOnly(String value) {
        Pattern p = Pattern.compile("^[A-Za-z]+$");
        return p.matcher(value).matches();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Connection> {
        @Override public String getDisplayName() {
            return "Connection";
        }

        public FormValidation doCheckUsername(@QueryParameter String value) {
            if (!isLettersOnly(value)){
                return FormValidation.error("The username must only contain letters.");
            }
            return FormValidation.ok();
        }

        @POST
        public FormValidation doTestConnection(@QueryParameter("url") final String urlString,
                                               @QueryParameter("username") final String username,
                                               @QueryParameter("password") final String password) throws IOException, ServletException {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            String authHeaderValue = createBasicAuthHeader(username, password);
            con.setRequestProperty("Authorization", authHeaderValue);
            int responseCode = con.getResponseCode();
            if (responseCode == 200){
                return FormValidation.ok("Success!");
            } else {
                return FormValidation.warning("Got response status " + responseCode);
            }

        }

        private String createBasicAuthHeader(String username, String password) {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            return "Basic " + new String(encodedAuth);
        }
    }
}
