package org.b3log.github;

import org.kohsuke.github.GHDirection;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueQueryBuilder;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedIterable;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * GitHub Issue report client.
 *
 * @author <a href="https://hacpai.com/member/88250">Liang Ding</a>
 * @version 5.0.0.0, Aug 7, 2026
 */
public final class Client {

    private static final String MILESTONE_NUM;
    private static final String REPOS;
    private static final String ISSUE_STATE;

    static {
        final ResourceBundle conf = ResourceBundle.getBundle("issues");
        REPOS = conf.getString("repos");
        MILESTONE_NUM = conf.getString("milestoneNum");
        ISSUE_STATE = conf.getString("issue.state");

        System.out.println("Repository: " + REPOS);
        System.out.println("Version: " + MILESTONE_NUM);
        System.out.println("Issue state: " + ISSUE_STATE);

        if (conf.containsKey("proxy.host") && conf.containsKey("proxy.port")) {
            final String proxyHost = conf.getString("proxy.host");
            final String proxyPort = conf.getString("proxy.port");
            if (!"".equals(proxyHost) && !"".equals(proxyPort)) {
                System.out.println("The proxy is configured in issues.properties [host=" + proxyHost + ", port=" + proxyPort + "], "
                        + "please configure it in the environment variables HTTPS_PROXY/HTTP_PROXY");
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        final GitHub github = GitHubBuilder.fromEnvironment().build();
        final GHRepository repo = github.getRepository(REPOS);
        final GHMilestone milestone = repo.getMilestone(Integer.parseInt(MILESTONE_NUM));

        final StringBuilder bugBuilder = new StringBuilder();
        final StringBuilder featureBuilder = new StringBuilder();
        final StringBuilder enhancementBuilder = new StringBuilder();
        final StringBuilder developmentBuilder = new StringBuilder();
        final StringBuilder refactorBuilder = new StringBuilder();
        final StringBuilder docBuilder = new StringBuilder();
        final StringBuilder skinBuilder = new StringBuilder();
        final StringBuilder themeBuilder = new StringBuilder();
        final StringBuilder abolishmentBuilder = new StringBuilder();
        final StringBuilder breakingBuilder = new StringBuilder();

        System.out.println("Retrieving issues....");
        System.out.println();
        final GHIssueQueryBuilder.ForRepository issueBuilder = repo.queryIssues();
        issueBuilder.milestone(MILESTONE_NUM);
        issueBuilder.state(GHIssueState.CLOSED);
        issueBuilder.direction(GHDirection.ASC);
        issueBuilder.pageSize(100);
        final PagedIterable<GHIssue> issues = issueBuilder.list();
        int count = 0;
        for (final GHIssue issue : issues) {
            final Collection<GHLabel> labels = issue.getLabels();
            if (labels.isEmpty()) {
                System.err.println("The issue [" + issue.getHtmlUrl() + "] has no label");
                System.exit(-1);
            }
            final String label = labels.iterator().next().getName();
            final StringBuilder liBuilder = new StringBuilder().append("* [").append(issue.getTitle()).append("](").append(issue.getHtmlUrl()).append(")\n");
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
                case "破坏性变更":
                case "Breaking":
                    breakingBuilder.append(liBuilder);
                    count++;
                    break;
                case "开发相关":
                case "Development":
                    developmentBuilder.append(liBuilder);
                    count++;
                    break;
                default:
                    System.err.println("The label [" + label + ", issue=" + issue.getNumber() + "] is invalid");
                    System.exit(-1);
            }
        }

        System.out.println("## v" + milestone.getTitle() + " / " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "\n");

        if (featureBuilder.length() > 0) {
            System.out.println("### Feature\n");
            System.out.println(featureBuilder);
        }

        if (skinBuilder.length() > 0) {
            System.out.println("### Theme\n");
            System.out.println(skinBuilder);
        }

        if (themeBuilder.length() > 0) {
            System.out.println("### Theme\n");
            System.out.println(themeBuilder);
        }

        if (enhancementBuilder.length() > 0) {
            System.out.println("### Enhancement\n");
            System.out.println(enhancementBuilder);
        }

        if (abolishmentBuilder.length() > 0) {
            System.out.println("### Abolishment\n");
            System.out.println(abolishmentBuilder);
        }

        if (breakingBuilder.length() > 0) {
            System.out.println("### Breaking\n");
            System.out.println(breakingBuilder);
        }

        if (bugBuilder.length() > 0) {
            System.out.println("### Bugfix\n");
            System.out.println(bugBuilder);
        }

        if (docBuilder.length() > 0) {
            System.out.println("### Document\n");
            System.out.println(docBuilder);
        }

        if (refactorBuilder.length() > 0) {
            System.out.println("### Refactor\n");
            System.out.println(refactorBuilder);
        }

        if (developmentBuilder.length() > 0) {
            System.out.println("### Development\n");
            System.out.println(developmentBuilder);
        }

        System.out.println(count + " issues totally.");
    }
}
