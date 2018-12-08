package com.gossip

import com.sonyericsson.jenkins.plugins.bfa.model.FailureCause
import com.sonyericsson.jenkins.plugins.bfa.model.FailureCauseBuildAction
import com.sonyericsson.jenkins.plugins.bfa.model.FoundFailureCause
import com.sonyericsson.jenkins.plugins.bfa.model.indication.FoundIndication
import org.jenkinsci.plugins.globalEventsPlugin.Event
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import static org.mockito.Mockito.*

class AnalyzerUnitTest extends AnalyzerTestBase {

    @Before
    void setUp() {
        super.setUp()
        analyzer = new Analyzer(logger, jenkinsId)
    }

    @Test
    void testExtractBuildInfo() {
        causes.add(gerritCause)
        HashMap<String, Object> buildinfo = BuildHelper.extractBuildInfo(build, super.jenkinsId)
        assertEquals(buildinfo.sort().toString(), [
                "build"    : 666,
                "duration" : 100,
                "jenkins"  : "some_jenkins",
                "job"      : "full/job/name",
                "node"     : "MockNode",
                "result"   : "null",
                "timestamp": 456
        ].sort().toString())
    }

    @Test
    void testProcessEvent() {
        FailureCauseBuildAction bfaAction = new FailureCauseBuildAction(
                [new FoundFailureCause(
                        new FailureCause("name", "descr"),
                        [
                                new FoundIndication(null, "Error_1", "Error_1")
                        ])
                ])
        when(build.getAction(FailureCauseBuildAction.class)).thenReturn(bfaAction)

        this.analyzer.processEvent(build, Event.JOB_FINALIZED)

        verify(
                logger,
                times(1)
        ).log(
                Mockito.eq("bfa"),
                Mockito.argThat(new MapMatcher([
                        "build"     : "666",
                        "categories": "none",
                        "cause"     : "name",
                        "duration"  : "100",
                        "jenkins"   : "some_jenkins",
                        "job"       : "full/job/name",
                        "node"      : "MockNode",
                        "result"    : "null",
                        "timestamp" : "456"
                ]))
        )
    }

    @Test
    void testProcessGerritCause() {
        this.analyzer.processStartCause(gerritCause, build, info)

        verify(
                logger,
                times(1)
        ).log(
                Mockito.eq("trigger.gerrit"),
                Mockito.argThat(new MapMatcher([
                        "build"                   : "build",
                        "gerrit_change_id"        : "2345",
                        "gerrit_change_number"    : "3456",
                        "gerrit_description"      : "Triggered by Gerrit: http://some.url",
                        "gerrit_patchset_number"  : "20",
                        "gerrit_patchset_revision": "1234",
                        "gerrit_patchset_url"     : "http://some.url",
                        "gerrit_refspec"          : "refs/changes/1/1/1",
                        "jenkins"                 : "some_jenkins",
                        "job"                     : "job",
                        "upstream_jenkins"        : "upstream_jenkins"
                ]))
        )
    }

    @Test
    void testProcessUpstreamCause() {
        this.analyzer.processStartCause(upstreamCause, build, info)

        verify(
                logger,
                times(1)
        ).log(
                Mockito.eq("trigger.upstream"),
                Mockito.argThat(new MapMatcher([
                        "build"           : "build",
                        "jenkins"         : "some_jenkins",
                        "job"             : "job",
                        "upstream_build"  : "1234",
                        "upstream_jenkins": "upstream_jenkins",
                        "upstream_job"    : "upstreamProject"
                ]))
        )
    }

    @Test
    void testProcessUserIdCause() {
        this.analyzer.processStartCause(userIdCause, build, info)

        verify(
                logger,
                times(1)
        ).log(
                Mockito.eq("trigger.user"),
                Mockito.argThat(new MapMatcher([
                        "build"           : "build",
                        "jenkins"         : "some_jenkins",
                        "job"             : "job",
                        "upstream_jenkins": "upstream_jenkins",
                        "user_id"         : "null",
                        "user_name"       : "anonymous"
                ]))
        )
    }

    @Test
    void testProcessTimerTriggerCause() {
        this.analyzer.processStartCause(timerTriggerCause, build, info)

        verify(
                logger,
                times(1)
        ).log(
                Mockito.eq("trigger.default"),
                Mockito.argThat(new MapMatcher([
                        "build"            : "build",
                        "cause_description": "Started by timer",
                        "cause_type"       : "class hudson.triggers.TimerTrigger\$TimerTriggerCause",
                        "jenkins"          : "some_jenkins",
                        "job"              : "job",
                        "upstream_jenkins" : "upstream_jenkins"
                ]))
        )
    }

    @Test
    void testProcessBFA() {
        FailureCauseBuildAction bfaAction = new FailureCauseBuildAction([
                new FoundFailureCause(
                        new FailureCause("name", "descr"),
                        [
                                new FoundIndication(null, "Error_1", "Error_1")
                        ]
                )
        ])

        when(build.getAction(FailureCauseBuildAction.class)).thenReturn(bfaAction)

        this.analyzer.processFailed(build, info)

        verify(
                logger,
                times(1)
        ).log(
                Mockito.eq("bfa"),
                Mockito.argThat(new MapMatcher([
                        "build"           : "build",
                        "categories"      : "none",
                        "cause"           : "name",
                        "jenkins"         : "some_jenkins",
                        "job"             : "job",
                        "upstream_jenkins": "upstream_jenkins"
                ]))
        )
    }

    @Test
    void testProcessBFAWithNullCategory() {
        FailureCause failureCause = new FailureCause("name", "descr")
        failureCause.setCategories(null)

        FailureCauseBuildAction bfaAction = new FailureCauseBuildAction(
                [new FoundFailureCause(failureCause,
                        [new FoundIndication(null, "Error_1", "Error_1")])])

        when(build.getAction(FailureCauseBuildAction.class)).thenReturn(bfaAction)

        this.analyzer.processFailed(build, info)

        verify(
                logger,
                times(1)
        ).log(
                Mockito.eq("bfa"),
                Mockito.argThat(new MapMatcher([
                        "build"           : "build",
                        "categories"      : "none",
                        "cause"           : "name",
                        "jenkins"         : "some_jenkins",
                        "job"             : "job",
                        "upstream_jenkins": "upstream_jenkins"
                ]))
        )
    }

    @Test
    void testProcessCauses() {
        causes.add(userIdCause)
        this.analyzer.processStartCauses(build, info)

        verify(
                logger,
                times(1)
        ).log(
                Mockito.eq("trigger.user"),
                Mockito.argThat(new MapMatcher([
                        "build"           : "build",
                        "jenkins"         : "some_jenkins",
                        "job"             : "job",
                        "upstream_jenkins": "upstream_jenkins",
                        "user_id"         : "null",
                        "user_name"       : "anonymous"
                ]))
        )
    }
}
