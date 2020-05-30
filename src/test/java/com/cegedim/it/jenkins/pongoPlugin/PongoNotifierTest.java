package com.cegedim.it.jenkins.pongoPlugin;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class PongoNotifierTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String url = "http://test.me";
    final String authToken = "identify-me";
    

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new PongoNotifier(url,authToken, false, false, false));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(new PongoNotifier(url,authToken, false, false, false), project.getBuildersList().get(0));
    }

    @Test
    public void testConfigRoundtripIncludeCommit() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        PongoNotifier builder = new PongoNotifier(url,authToken, false, false, false);
        builder.setIncludeCommitInfo(true);
        project.getBuildersList().add(builder);
        project = jenkins.configRoundtrip(project);

        PongoNotifier lhs = new PongoNotifier(url,authToken, false, false, false);
        lhs.setIncludeCommitInfo(true);
        jenkins.assertEqualDataBoundBeans(lhs, project.getBuildersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        PongoNotifier builder = new PongoNotifier(url,authToken, false, false, false);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("sending to, " + url, build);
    }

    @Test
    public void testBuildFrench() throws Exception {

        FreeStyleProject project = jenkins.createFreeStyleProject();
        PongoNotifier builder = new PongoNotifier(url,authToken, false, false, false);
        builder.setincludeFailedTests(true);
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("sending test failures to, " + url, build);
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
                = "node {\n"
                + "  pongo '" + url + "','"+authToken+"'\n"
                + "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "sending notification to , " + url + "!";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

}