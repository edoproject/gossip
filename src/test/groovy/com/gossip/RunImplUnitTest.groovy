package com.gossip

import hudson.model.AbstractBuild
import hudson.model.Computer
import hudson.model.Executor
import hudson.model.Node
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.actions.WorkspaceAction
import org.jenkinsci.plugins.workflow.flow.FlowExecution
import org.jenkinsci.plugins.workflow.graph.FlowNode
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner.class)
@PrepareForTest(Jenkins.class)
class RunImplUnitTest extends GroovyTestCase {
    Node node
    FlowNode flowNode
    List<Node> flowNodes
    WorkspaceAction action

    @Mock
    private Jenkins jenkins

    @Mock
    private Node powerNode

    void setUp() {
        PowerMockito.mockStatic(Jenkins.class)
        PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins)
        PowerMockito.when(jenkins.getNode("node_name")).thenReturn(powerNode)

        node = PowerMockito.mock(Node)
        PowerMockito.when(node.getNodeName()).thenReturn("MockNode")
        PowerMockito.when(node.getLabelString()).thenReturn("MockLabel")

        action = PowerMockito.mock(WorkspaceAction.class)
        PowerMockito.when(action.getNode()).thenReturn("node_name")

        flowNode = PowerMockito.mock(FlowNode)
        PowerMockito.when(flowNode.getAction(WorkspaceAction.class)).thenReturn(action)
        flowNodes = new ArrayList<Node>()
        flowNodes.add(flowNode)
    }

    @Test
    void testWorkflowRun() {
        WorkflowRun run = PowerMockito.mock(WorkflowRun.class)
        FlowExecution execution = PowerMockito.mock(FlowExecution.class)
        PowerMockito.when(execution.getCurrentHeads()).thenReturn(flowNodes)
        PowerMockito.when(run.getExecution()).thenReturn(execution)
        RunImpl rp = new RunImpl(run)

        Node n = rp.fetchNodes(run)

        assertEquals(powerNode, n)
    }

    @Test
    void testAbstractBuild() {
        AbstractBuild run = PowerMockito.mock(AbstractBuild.class)
        PowerMockito.when(run.getBuiltOn()).thenReturn(node)
        RunImpl rp = new RunImpl(run)

        Node n = rp.fetchNodes(run)

        assertEquals(node, n)
    }

    @Test
    void testRun() {
        hudson.model.Run run = PowerMockito.mock(hudson.model.Run.class)
        Executor executor = PowerMockito.mock(Executor.class)
        Computer computer = PowerMockito.mock(Computer.class)
        PowerMockito.when(computer.getNode()).thenReturn(node)
        PowerMockito.when(executor.getOwner()).thenReturn(computer)
        PowerMockito.when(run.getExecutor()).thenReturn(executor)
        RunImpl rp = new RunImpl(run)

        Node n = rp.fetchNodes(run)

        assertEquals(node, n)
    }
}
