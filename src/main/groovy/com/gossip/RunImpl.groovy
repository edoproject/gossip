package com.gossip

import hudson.model.*
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.actions.WorkspaceAction
import org.jenkinsci.plugins.workflow.flow.FlowExecution
import org.jenkinsci.plugins.workflow.graph.FlowGraphWalker
import org.jenkinsci.plugins.workflow.graph.FlowNode
import org.jenkinsci.plugins.workflow.job.WorkflowRun

class RunImpl implements Run {
    hudson.model.Run run
    Node node

    RunImpl(hudson.model.Run run) {
        this.run = run
        this.node = null
    }

    Action getAction(Class type) {
        return run.getAction(type)
    }

    Node getNode() {
        return node ? node : fetchNodes(run)
    }

    Node fetchNodes(WorkflowRun r) {
        List<Node> nodes = new ArrayList<Node>()

        FlowExecution execution = r.getExecution()
        if (execution == null) return null

        FlowGraphWalker walker = new FlowGraphWalker(execution)
        for (FlowNode step : walker) {
            WorkspaceAction action = step.getAction(WorkspaceAction.class)
            if (action != null) nodes.add(action.getNode())
        }

        return nodes.isEmpty() ? null : Jenkins.getInstance().getNode(nodes[0])
    }

    Node fetchNodes(AbstractBuild r) {
        return r.getBuiltOn()
    }

    Node fetchNodes(hudson.model.Run run) {
        try {
            return run.getExecutor().getOwner().getNode()
        } catch (NullPointerException e) {
        }
        return null
    }

    String getDisplayName() {
        return run.getDisplayName()
    }

    long getStartTimeInMillis() {
        return run.getStartTimeInMillis()
    }

    int getNumber() {
        run.getNumber()
    }

    String getUrl() {
        run.getUrl()
    }

    long getDuration() {
        return run.getDuration()
    }

    Result getResult() {
        return run.getResult()
    }

    Map<String, String> getEnvVars() {
        return run.getEnvVars()
    }

    String toString() {
        run.toString()
    }

    List<Cause> getCauses() {
        return run.getCauses()
    }

    String getJobFullName() {
        return run.getParent().getFullName()
    }
}
