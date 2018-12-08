package com.gossip

import com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.GerritCause
import com.sonyericsson.jenkins.plugins.bfa.model.FailureCause
import com.sonyericsson.jenkins.plugins.bfa.model.FailureCauseBuildAction
import com.sonyericsson.jenkins.plugins.bfa.model.FoundFailureCause
import com.sonyericsson.jenkins.plugins.bfa.model.indication.FoundIndication
import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventType
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Change
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.PatchSet
import com.sonymobile.tools.gerrit.gerritevents.dto.events.ChangeBasedEvent
import hudson.model.Cause
import hudson.model.Cause.UpstreamCause
import hudson.model.Cause.UserIdCause
import hudson.model.Node
import hudson.triggers.TimerTrigger
import org.fluentd.logger.FluentLogger
import org.mockito.Mock

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class AnalyzerTestBase extends GroovyTestCase {

    Analyzer analyzer
    FailureCauseBuildAction bfaAction
    FluentLogger logger
    GerritCause gerritCause
    HashMap<String, Object> info
    List<Cause> causes

    @Mock
    Node node

    @Mock
    Run build
    String jenkinsId
    TimerTrigger.TimerTriggerCause timerTriggerCause
    UpstreamCause upstreamCause
    UserIdCause userIdCause

    private Cause prepareGerritCause() {
        Cause cause = new GerritCause()
        cause.url = "http://some.url"
        cause.setEvent(new ChangeBasedEvent() {
            @Override
            GerritEventType getEventType() {
                return null
            }

            @Override
            boolean isScorable() {
                return false
            }
        })
        PatchSet patchset = new PatchSet()
        patchset.setNumber("20")
        patchset.setRef("refs/changes/1/1/1")
        patchset.setRevision("1234")
        Change change = new Change()
        change.setId("2345")
        change.setNumber("3456")
        ((ChangeBasedEvent) cause.getEvent()).setChange(change)
        ((ChangeBasedEvent) cause.getEvent()).setPatchset(patchset)
        return cause
    }

    private Run prepareBuild(List<Cause> causes, Node node) {
        Run build = mock(Run.class)
        when(build.getCauses()).thenReturn(causes)
        when(build.getDisplayName()).thenReturn("DisplayName")
        when(build.getDuration()).thenReturn(100L)
        when(build.getJobFullName()).thenReturn("full/job/name")
        when(build.getNode()).thenReturn(node)
        when(build.getNumber()).thenReturn(666)
        when(build.getStartTimeInMillis()).thenReturn(456L)
        when(build.getUrl()).thenReturn("relativeUrl")
        when(build.getResult()).thenReturn(null)
        return build
    }

    private Node prepareNode() {
        Node node = mock(Node)
        when(node.getNodeName()).thenReturn("MockNode")
        when(node.getLabelString()).thenReturn("MockLabel")
        return node
    }

    void setUp() {
        jenkinsId = "some_jenkins"
        logger = mock(FluentLogger.class)
        node = prepareNode()

        upstreamCause = new UpstreamCause("upstreamProject", 1234, "http://upstream.url", causes)
        userIdCause = new UserIdCause()
        gerritCause = prepareGerritCause()
        timerTriggerCause = new TimerTrigger.TimerTriggerCause()
        causes = new ArrayList<Cause>()

        build = prepareBuild(causes, node)

        info = [
                "jenkins"         : "some_jenkins",
                "upstream_jenkins": "upstream_jenkins",
                "build"           : "build",
                "job"             : "job"
        ]

        bfaAction = new FailureCauseBuildAction(
                [new FoundFailureCause(
                        new FailureCause("name", "descr"),
                        [new FoundIndication(null, "Error_1", "Error_1")])
                ])
    }

    // Workaround to be able to run the whole suite without the warning "No tests found..."
    void testEmpty() {

    }
}