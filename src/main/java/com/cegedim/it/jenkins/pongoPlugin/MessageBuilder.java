package com.cegedim.it.jenkins.pongoPlugin;

import com.cegedim.it.jenkins.pongoPlugin.model.BuildReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.*;
import hudson.model.Messages;
import hudson.util.LogTaskListener;
import hudson.scm.ChangeLogSet;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import lombok.extern.slf4j.Slf4j;

import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageBuilder {

    private static final Pattern aTag = Pattern.compile("(?i)<a([^>]+)>(.+?)</a>|(\\{)");
    private static final Pattern href = Pattern.compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");
    public static final String STATUS_MESSAGE_START = "Start";
    public static final String STATUS_MESSAGE_SUCCESS = "Success";
    public static final String STATUS_MESSAGE_FAILURE = "Failure";
    public static final String STATUS_MESSAGE_WORKFLOW = "Workflow";

    private PongoNotifier notifier;
    private Run run;
    private TaskListener listener;
    private BuildReport report;

    private static final Logger log = LoggerFactory.getLogger(MessageBuilder.class);
    
    public MessageBuilder(PongoNotifier notifier, Run Run, TaskListener listener) {
        this.notifier = notifier;
        this.run = Run;
        this.listener = listener;
        this.report = new BuildReport();
    }

    public String buildPipeMsg(String message){
        appendStatus(STATUS_MESSAGE_WORKFLOW);
        appendFullDisplayName();
        appendDisplayName();
        appendOpenLink();
        report.setSummary(message);
        try {
            return new ObjectMapper().writeValueAsString(report);
        } catch (JsonProcessingException e) {
            log.error("Error build json process", e);
            e.printStackTrace();
        }
        return null;
    }

    public String build(){
        appendFullDisplayName();
        appendDisplayName();
        appendParentName();
        appendOpenLink();
        appendCause();
        appendBranch();
        appendDuration();
        appendStatus(getBuildResult());
        appendBuildSummary();
        appendTestSummary();
        appendChanges();
        appendFailedTests();
        try {
            return new ObjectMapper().writeValueAsString(report);
        } catch (JsonProcessingException e) {
            log.error("Error build json process", e);
            e.printStackTrace();
        }
        return null;
    }

    private void appendParentName() {
    	log.info("parent class is "+run.getParent().getClass());
    	// in case we are on a multibranch pipeline : 
    	if(run.getParent() instanceof WorkflowJob){
    		report.setParentName(this.escape(run.getParent().getParent().getFullDisplayName()));
    	}else {
    		report.setParentName(this.escape(run.getParent().getName()));
    	}
		
		
	}

	private void appendBranch() {
		try {
			report.setScmBranch(run.getEnvironment().get("BRANCH_NAME", "not-on-a-branch"));
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void appendFullDisplayName(){
        report.setName(this.escape(run.getFullDisplayName()));
    }

    private void appendDisplayName(){
        report.setNumber(this.escape(run.getDisplayName()));
    }

    private void appendOpenLink(){
        String url = DisplayURLProvider.get().getRunURL(run);
        report.setFullUrl(this.escape(url));
    }


    private void appendStatus(String status){
        report.setStatus(status);
    }

    private String getBuildResult(){
        ResultTrend trend = ResultTrend.getResultTrend(this.run);
        if(trend == ResultTrend.SUCCESS || trend == ResultTrend.FIXED){
            return STATUS_MESSAGE_SUCCESS;
        }else{
            return STATUS_MESSAGE_FAILURE;
        }
    }

    private void appendBuildSummary(){
        report.setSummary(this.escape(run.getBuildStatusSummary().message));
    }

    private void appendCause(){
        CauseAction causeAction = run.getAction(CauseAction.class);
        if (causeAction != null){
            report.setCause(this.escape(causeAction.getCauses().get(0).getShortDescription()));
        }
    }

    private void appendDuration(){
        report.setDuration(run.getDuration());
    }

    private void appendChanges(){
        report.initChanges();
        log.info("run class is {}",run.getClass());
        if(this.run instanceof WorkflowRun){
        	WorkflowRun build = (WorkflowRun) run;
        	List<hudson.scm.ChangeLogSet<? extends hudson.scm.ChangeLogSet.Entry>> changeSets = build.getChangeSets();
            if (changeSets == null || changeSets.isEmpty()){
                listener.getLogger().println("No commit changes");
                log.info("No commit changes");
                return;
            }
            changeSets.forEach((changeSet)->{
            	changeSet.forEach((entry)->{
            		report.addChange(entry);
            	});
            });
        }
    }

    private void appendTestSummary(){
        report.initTestSummary();
        AbstractTestResultAction<?> action = this.run
                .getAction(AbstractTestResultAction.class);
        if (action != null) {
            report.setTotalTest(action.getTotalCount());
            report.setFailTest(action.getFailCount());
            report.setSkipTest(action.getSkipCount());
        } else {
            listener.getLogger().println("No test action");
            log.info("No test action");
        }
    }

    private void appendFailedTests(){
        report.getTestSummary().initFailedResults();
        AbstractTestResultAction<?> action = this.run
                .getAction(AbstractTestResultAction.class);
        if (action != null && action.getFailCount() > 0) {
            for(TestResult result : action.getFailedTests()){
                report.getTestSummary().addFailedTestResults(result);
            }
        } else {
            listener.getLogger().println("No failed tests");
            log.info("No failed tests");
        }
    }


    private String[] extractReplaceLinks(Matcher aTag, StringBuffer sb) {
        int size = 0;
        List<String> links = new ArrayList<>();
        while (aTag.find()) {
            Matcher url = href.matcher(aTag.group(1));
            if (url.find()) {
                String escapeThis = aTag.group(3);
                if (escapeThis != null) {
                    aTag.appendReplacement(sb,String.format("{%s}", size++));
                    links.add("{");
                } else {
                    aTag.appendReplacement(sb,String.format("{%s}", size++));
                    links.add(String.format("<%s|%s>", url.group(1).replaceAll("\"", ""), aTag.group(2)));
                }
            }
        }
        aTag.appendTail(sb);
        return links.toArray(new String[size]);
    }

    private String escapeCharacters(String string) {
        string = string.replace("&", "&amp;");
        string = string.replace("<", "&lt;");
        string = string.replace(">", "&gt;");
        return string;
    }

    public String escape(String string) {
        StringBuffer pattern = new StringBuffer();
        String[] links = extractReplaceLinks(aTag.matcher(string), pattern);
        return MessageFormat.format(escapeCharacters(pattern.toString()), links);
    }
}