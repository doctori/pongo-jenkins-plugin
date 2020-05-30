package com.cegedim.it.jenkins.pongoPlugin;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.util.Secret;

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
    final String clientID = "identify-me";
    final Secret clientSecret = Secret.fromString("super-secret");
    
    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
                = "node {\n"
                + "  pongo message: 'test-me'\n"
                + "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        // TODO : better than that ...
        String expectedString = "No commit changes";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

}