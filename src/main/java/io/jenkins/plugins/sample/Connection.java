package io.jenkins.plugins.sample;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.commons.codec.binary.Base64;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import io.jenkins.cli.shaded.org.apache.sshd.common.auth.BasicCredentialsProvider;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.Job;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;

import jenkins.model.Jenkins;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

public class Connection extends AbstractDescribableImpl<Connection> {

    private String url;
    private String username;
    private Secret password;
    private String credentialsId;

    @DataBoundConstructor
    public Connection(String url, String password) {
        this.url = url;
        this.password = Secret.fromString(password);
    }

    public String getUrl() {
        return url;
    }

    public String getUsername(){
        return username;
    }

    public Secret getPassword() {
        return password;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setUsername(String username){
        if (isLettersOnly(username)){
            this.username = username;
        }
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = Util.fixEmptyAndTrim(credentialsId);
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

        @POST
        public FormValidation doTestCredential(@QueryParameter("url") final String urlString,
                                               @QueryParameter("credentialsId") final String credentialsId) throws IOException, ServletException {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            StringCredentials credential = getCredentialsById(credentialsId);
            String secretPlaintext = credential.getSecret().getPlainText();
            assemblePostBody(con, secretPlaintext);
            int responseCode = con.getResponseCode();
            if (responseCode == 200){
                return FormValidation.ok("Success!");
            } else {
                return FormValidation.warning("Got response status " + responseCode);
            }

        }

        private void assemblePostBody(HttpURLConnection con, String content) throws IOException {
            con.addRequestProperty("Content-Type", "application/" + "POST");
            con.setRequestProperty("Content-Length", Integer.toString(content.length()));
            con.setDoOutput(true);
            con.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
        }

        private StringCredentials getCredentialsById(String credentialId) {
            List<StringCredentials> candidates = CredentialsProvider.lookupCredentials(StringCredentials.class,
                                                                                       (Item) null,
                                                                                       ACL.SYSTEM, (DomainRequirement) null);
            return CredentialsMatchers.firstOrNull(candidates, CredentialsMatchers.withId(
                    credentialId));
        }

        private String createBasicAuthHeader(String username, String password) {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            return "Basic " + new String(encodedAuth);
        }

        public ListBoxModel doFillCredentialsIdItems(
                @AncestorInPath Item item,
                @QueryParameter String credentialsId) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(credentialsId);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }
            return result
                          .includeAs(ACL.SYSTEM, item, StringCredentials.class)
                          .includeCurrentValue(credentialsId);
        }
    }
}
