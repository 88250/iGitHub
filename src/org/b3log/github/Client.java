package org.b3log.github;

import java.io.Console;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import net.iharder.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * GitHub client.
 *
 * @author <a href="https://hacpai.com/member/88250">Liang Ding</a>
 * @version 1.1.2.4, Jan 12, 2017
 */
public class Client {

    private static final Map<String, Integer> issueNameSort = new HashMap<>();

    static {
        issueNameSort.put("feature", 0);
        issueNameSort.put("bug", 1);
        issueNameSort.put("skin", 2);
        issueNameSort.put("enhancement", 3);
        issueNameSort.put("development", 4);
        issueNameSort.put("crazy", 5);
        issueNameSort.put("default", 6);
    }

    private static final String MILESTONE_NUM;

    private static final String REPOS;

    private static final String USER_NAME;

    private static final String ISSUE_STATE;
    //private static final String PROXY_HOST;
    //private static final String PROXY_PORT;

    static {
        REPOS = ResourceBundle.getBundle("issues").getString("repos");
        MILESTONE_NUM = ResourceBundle.getBundle("issues").getString("milestoneNum");
        ISSUE_STATE = ResourceBundle.getBundle("issues").getString("issue.state");

        USER_NAME = ResourceBundle.getBundle("issues").getString("username");

        System.out.println("Repository: " + REPOS);
        System.out.println("Version Num: " + MILESTONE_NUM);
        System.out.println("Issue State: " + ISSUE_STATE);

//        PROXY_HOST = ResourceBundle.getBundle("issues").getString("proxy.host");
//        PROXY_PORT = ResourceBundle.getBundle("issues").getString("proxy.port");
//
//        if (null != PROXY_HOST && !"".equals(PROXY_HOST)
//            && null != PROXY_PORT && !"".equals(PROXY_PORT)) {
//            System.out.println("Configured proxy[host=" + PROXY_HOST + ", port=" + PROXY_PORT + "]");
//
//            System.getProperties().put("proxySet", "true");
//            System.getProperties().put("proxyHost", "127.0.0.1");
//            System.getProperties().put("proxyPort", "8087");
//        }
    }

    public static void main(String[] args) throws Exception {
        final Console cons = System.console();
        System.out.print("Please input your password: ");
        final String password = new String(cons.readPassword());
        String authStr = USER_NAME + ':' + password;

        authStr = Base64.encodeBytes(authStr.getBytes());

        //Runtime.getRuntime().exec("ipconfig /flushdns");
        final DefaultHttpClient httpClient = new DefaultHttpClient();

        final StringBuilder bugBuilder = new StringBuilder();
        final StringBuilder featureBuilder = new StringBuilder();
        final StringBuilder enhancementBuilder = new StringBuilder();
        final StringBuilder developmentBuilder = new StringBuilder();
        final StringBuilder skinBuilder = new StringBuilder();

        try {
            System.out.println("Retriving issues....");
            System.out.println();
            int page = 1;
            int count = 0;
            while (true) {
                final HttpGet httpGet = new HttpGet("https://api.github.com/repos/" + REPOS + "/issues?"
                        + "milestone=" + MILESTONE_NUM + "&state=" + ISSUE_STATE + "&direction=asc&page=" + page);
                page++;

                httpGet.addHeader("Authorization", "Basic " + authStr);

                final HttpResponse response = httpClient.execute(httpGet);

                HttpEntity entity = response.getEntity();
                String content = IOUtils.toString(entity.getContent(), "UTF-8");

                final JSONArray json = new JSONArray(content);
                if (json.length() < 1) {
                    break;
                }

                for (int i = 0; i < json.length(); i++) {
                    final JSONObject issue = json.getJSONObject(i);
                    final JSONArray labels = issue.getJSONArray("labels");

                    final StringBuilder liBuilder = new StringBuilder();
                    String startIssueName = "default";

                    final StringBuilder labelBuilder = new StringBuilder();
                    for (int j = 0; j < labels.length(); j++) {
                        labelBuilder.append("<span style='");
                        final JSONObject label = labels.getJSONObject(j);
                        final String color = label.getString("color");
                        final String name = label.getString("name");

                        startIssueName = getStartLabelName(name, startIssueName);

                        labelBuilder.append("background: #").append(color).append(" !important;color:#FFFFFF !important;padding: 1px 4px;'>").
                                append(name).append("</span>");

                        if (j < labels.length() - 1) {
                            labelBuilder.append("&nbsp;");
                        }
                    }

                    liBuilder.append("    <li><a href=\"").append(issue.getString("html_url")).append("\">").
                            append(issue.getString("number")).append(' ').append(issue.getString("title")).
                            append("</a>&nbsp;").append(labelBuilder.toString()).append("</li>").append("\n");

                    switch (startIssueName) {
                        case "feature":
                            featureBuilder.append(liBuilder.toString());
                            count++;
                            break;
                        case "bug":
                            bugBuilder.append(liBuilder.toString());
                            count++;
                            break;
                        case "skin":
                            skinBuilder.append(liBuilder.toString());
                            count++;
                            break;
                        case "enhancement":
                            enhancementBuilder.append(liBuilder.toString());
                            count++;
                            break;
                        case "development":
                            developmentBuilder.append(liBuilder.toString());
                            count++;
                            break;
                        default:
                            System.err.println("The label [" + startIssueName + ", issue=" + issue.getString("number")
                                    + "] invalid");
                    }
                }
            }

            System.out.println("<ul>");
            if (featureBuilder.length() > 0) {
                featureBuilder.deleteCharAt(featureBuilder.length() - 1);
                System.out.println(featureBuilder.toString());
            }

            if (bugBuilder.length() > 0) {
                bugBuilder.deleteCharAt(bugBuilder.length() - 1);
                System.out.println(bugBuilder.toString());
            }

            if (skinBuilder.length() > 0) {
                skinBuilder.deleteCharAt(skinBuilder.length() - 1);
                System.out.printf(skinBuilder.toString());
            }

            if (enhancementBuilder.length() > 0) {
                enhancementBuilder.deleteCharAt(enhancementBuilder.length() - 1);
                System.out.println(enhancementBuilder.toString());
            }

            if (developmentBuilder.length() > 0) {
                developmentBuilder.deleteCharAt(developmentBuilder.length() - 1);
                System.out.println(developmentBuilder.toString());
            }

            System.out.println("</ul>");
            System.out.println(count + " issues totally.");

            // printResponseHeader(response);
            // printResponseContent(entity);
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpClient.getConnectionManager().shutdown();
        }
    }

    private static void printResponseContent(HttpEntity entity) throws JSONException, IOException, IllegalStateException {
        String content = IOUtils.toString(entity.getContent());
        final JSONArray json = new JSONArray(content);
        System.out.println(json.toString(4));

        EntityUtils.consume(entity);
    }

    private static void printResponseHeader(final HttpResponse response) {
        System.out.println(response.getStatusLine());
        final Header[] responseHeaders = response.getAllHeaders();
        for (int i = 0; i < responseHeaders.length; i++) {
            final Header header = responseHeaders[i];
            System.out.println(header.getName() + ": " + header.getValue());
        }
    }

    private static String getStartLabelName(final String labelName, final String existLabelName) {
        Integer labelNum = issueNameSort.get(labelName);
        if (null == labelNum) {
            labelNum = 0;
        }

        return labelNum > issueNameSort.get(existLabelName) ? existLabelName : labelName;
    }
}
