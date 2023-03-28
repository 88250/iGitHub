package org.b3log.github;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * GitHub Issue report client.
 *
 * @author <a href="https://hacpai.com/member/88250">Liang Ding</a>
 * @version 3.0.0.3, Mar 28, 2023
 */
public final class Client {

    private static final String MILESTONE_NUM;
    private static final String REPOS;
    private static final String ISSUE_STATE;
    private static String PAT;

    static {
        final ResourceBundle conf = ResourceBundle.getBundle("issues");
        REPOS = conf.getString("repos");
        MILESTONE_NUM = conf.getString("milestoneNum");
        ISSUE_STATE = conf.getString("issue.state");

        System.out.println("Repository: " + REPOS);
        System.out.println("Version Num: " + MILESTONE_NUM);
        System.out.println("Issue State: " + ISSUE_STATE);

        if (conf.containsKey("proxy.host") && conf.containsKey("proxy.port")) {
            final String proxyHost = conf.getString("proxy.host");
            final String proxyPort = conf.getString("proxy.port");
            if (!"".equals(proxyHost) && !"".equals(proxyPort)) {
                System.getProperties().put("proxySet", "true");
                System.getProperties().put("socksProxyHost", proxyHost);
                System.getProperties().put("socksProxyPort", proxyPort);
                System.out.println("Configured proxy [host=" + proxyHost + ", port=" + proxyPort + "]");
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        PAT = args[0];

        loadLables();

        Runtime.getRuntime().exec("ipconfig /flushdns");
        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        final StringBuilder bugBuilder = new StringBuilder();
        final StringBuilder featureBuilder = new StringBuilder();
        final StringBuilder enhancementBuilder = new StringBuilder();
        final StringBuilder developmentBuilder = new StringBuilder();
        final StringBuilder refactorBuilder = new StringBuilder();
        final StringBuilder docBuilder = new StringBuilder();
        final StringBuilder skinBuilder = new StringBuilder();
        final StringBuilder themeBuilder = new StringBuilder();
        final StringBuilder abolishmentBuilder = new StringBuilder();

        System.out.println("Retrieving issues....");
        System.out.println();
        int page = 1;
        int count = 0;
        while (true) {
            final HttpGet httpGet = new HttpGet("https://api.github.com/repos/" + REPOS + "/issues?"
                    + "milestone=" + MILESTONE_NUM + "&state=" + ISSUE_STATE + "&direction=asc&page=" + page + "&per_page=100");
            page++;
            httpGet.addHeader("Authorization", "Token " + PAT);
            final HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String content = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);

            final JSONArray json = new JSONArray(content);
            if (json.length() < 1) {
                break;
            }

            for (int i = 0; i < json.length(); i++) {
                final JSONObject issue = json.getJSONObject(i);
                final String label = issue.getJSONArray("labels").optJSONObject(0).optString("name");
                final StringBuilder liBuilder = new StringBuilder().append("* [").append(issue.getString("title")).append("](").append(issue.getString("html_url")).append(")\n");
                switch (label) {
                    case "引入特性":
                    case "Feature":
                        featureBuilder.append(liBuilder);
                        count++;
                        break;
                    case "修复缺陷":
                    case "Bug":
                        bugBuilder.append(liBuilder);
                        count++;
                        break;
                    case "改进皮肤":
                        skinBuilder.append(liBuilder);
                        count++;
                        break;
                    case "改进主题":
                        themeBuilder.append(liBuilder);
                        count++;
                        break;
                    case "改进功能":
                    case "Enhancement":
                        enhancementBuilder.append(liBuilder);
                        count++;
                        break;
                    case "开发重构":
                    case "Refactor":
                        refactorBuilder.append(liBuilder);
                        count++;
                        break;
                    case "文档相关":
                    case "Document":
                        docBuilder.append(liBuilder);
                        count++;
                        break;
                    case "移除功能":
                    case "Abolishment":
                        abolishmentBuilder.append(liBuilder);
                        count++;
                        break;
                    case "开发相关":
                    case "Development":
                        developmentBuilder.append(liBuilder);
                        count++;
                        break;
                    default:
                        System.err.println("The label [" + label + ", issue=" + issue.optString("number") + "] is invalid");
                        System.exit(-1);
                }
            }
        }

        System.out.println("## v" + getVersion() + " / " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "\n");

        if (featureBuilder.length() > 0) {
            System.out.println("### 引入特性\n");
            System.out.println(featureBuilder);
        }

        if (skinBuilder.length() > 0) {
            System.out.println("### 改进皮肤\n");
            System.out.println(skinBuilder);
        }

        if (themeBuilder.length() > 0) {
            System.out.println("### 改进主题\n");
            System.out.println(themeBuilder);
        }

        if (enhancementBuilder.length() > 0) {
            System.out.println("### 改进功能\n");
            System.out.println(enhancementBuilder);
        }

        if (abolishmentBuilder.length() > 0) {
            System.out.println("### 移除功能\n");
            System.out.println(abolishmentBuilder);
        }

        if (bugBuilder.length() > 0) {
            System.out.println("### 修复缺陷\n");
            System.out.println(bugBuilder);
        }

        if (docBuilder.length() > 0) {
            System.out.println("### 文档相关\n");
            System.out.println(docBuilder);
        }

        if (refactorBuilder.length() > 0) {
            System.out.println("### 开发重构\n");
            System.out.println(refactorBuilder);
        }

        if (developmentBuilder.length() > 0) {
            System.out.println("### 开发相关\n");
            System.out.println(developmentBuilder);
        }

        System.out.println(count + " issues totally.");
    }

    private static JSONArray LABLES;

    private static void loadLables() throws Exception {
        System.out.println("Loading labels....");
        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        final HttpGet httpGet = new HttpGet("https://api.github.com/repos/" + REPOS + "/labels");
        httpGet.addHeader("Authorization", "Token " + PAT);
        final HttpResponse response = httpClient.execute(httpGet);
        final HttpEntity entity = response.getEntity();
        final String content = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
        LABLES = new JSONArray(content);
    }

    private static String getVersion() throws Exception {
        final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        final HttpGet httpGet = new HttpGet("https://api.github.com/repos/" + REPOS + "/milestones/" + MILESTONE_NUM);
        httpGet.addHeader("Authorization", "Token " + PAT);
        final HttpResponse response = httpClient.execute(httpGet);
        final HttpEntity entity = response.getEntity();
        final String content = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
        final JSONObject milestone = new JSONObject(content);
        return milestone.optString("title");
    }

    private static JSONObject getLable(final String labelName) {
        for (int i = 0; i < LABLES.length(); i++) {
            final JSONObject label = LABLES.optJSONObject(i);
            if (label.optString("name").equals(labelName)) {
                return label;
            }
        }
        return null;
    }
}
