package com.gossip

import hudson.model.*
import org.jenkinsci.plugins.workflow.job.WorkflowRun

interface Run {
    Action getAction(Class type)

    Node getNode()

    Node fetchNodes(WorkflowRun r)

    Node fetchNodes(AbstractBuild r)

    Node fetchNodes(hudson.model.Run run)

    String getDisplayName()

    long getStartTimeInMillis()

    int getNumber()

    String getUrl()

    long getDuration()

    Result getResult()

    Map<String, String> getEnvVars()

    String toString()

    List<Cause> getCauses()

    String getJobFullName()
}