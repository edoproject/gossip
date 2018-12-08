package com.gossip

import com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.GerritCause
import com.sonyericsson.jenkins.plugins.bfa.model.FailureCauseBuildAction
import com.sonyericsson.jenkins.plugins.bfa.model.FoundFailureCause
import com.sonyericsson.jenkins.plugins.bfa.model.indication.FoundIndication
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Change
import com.sonymobile.tools.gerrit.gerritevents.dto.events.ChangeBasedEvent
import com.sonymobile.tools.gerrit.gerritevents.dto.events.GerritTriggeredEvent
import hudson.model.Cause
import hudson.model.Cause.UpstreamCause
import hudson.model.Cause.UserIdCause
import org.fluentd.logger.FluentLogger
import org.jenkinsci.plugins.globalEventsPlugin.Event

class Analyzer {
    final FluentLogger fluentLogger
    final String jenkinsId

    Analyzer(FluentLogger fluentLogger, String jenkinsId) {
        this.fluentLogger = fluentLogger
        this.jenkinsId = jenkinsId
    }

    void processEvent(Run build, String event) {
        if (event != Event.JOB_FINALIZED) return
        final Map<String, Object> buildInfo = BuildHelper.extractBuildInfo(build, jenkinsId)
        extendBuildInfo(build, buildInfo)
        processStartCauses(build, buildInfo)
        processFailed(build, buildInfo)
        fluentLogger.log("builds", buildInfo.sort())
    }

    void extendBuildInfo(Run build, Map<String, Object> buildInfo) {
    }

    void processStartCauses(Run build, Map<String, Object> buildInfo) {
        build.getCauses().each { cause -> processStartCause(cause, build, buildInfo) }
    }

    void processStartCause(UpstreamCause cause, Run build, Map<String, Object> buildInfo) {
        final Map<String, Object> causeInfo = [
                "upstream_build"  : cause.getUpstreamBuild(),
                "upstream_job"    : cause.getUpstreamProject(),
                "upstream_jenkins": buildInfo["jenkins"]
        ]
        causeInfo << buildInfo
        fluentLogger.log("trigger.upstream", causeInfo.sort())
    }

    void processStartCause(UserIdCause cause, Run build, Map<String, Object> buildInfo) {
        final Map<String, Object> causeInfo = [
                "user_name": cause.getUserName(),
                "user_id"  : cause.getUserId()
        ]
        causeInfo << buildInfo
        fluentLogger.log("trigger.user", causeInfo.sort())
    }

    void processStartCause(GerritCause cause, Run build, Map<String, Object> buildInfo) {
        final Map<String, Object> causeInfo = [
                "gerrit_description" : cause.getShortGerritDescription(),
                "gerrit_patchset_url": cause.getUrl()
        ]
        GerritTriggeredEvent gte = cause.getEvent()
        if (gte instanceof ChangeBasedEvent) {
            ChangeBasedEvent cbe = (ChangeBasedEvent) gte
            Change change = cbe.getChange()
            causeInfo << [
                    "gerrit_change_id"        : change.getId(),
                    "gerrit_change_number"    : change.getNumber(),
                    "gerrit_patchset_number"  : cbe.getPatchSet().getNumber(),
                    "gerrit_patchset_revision": cbe.getPatchSet().getRevision(),
                    "gerrit_refspec"          : cbe.getPatchSet().getRef()
            ]
        }
        causeInfo << buildInfo
        fluentLogger.log("trigger.gerrit", causeInfo.sort())
    }

    void processStartCause(Cause cause, Run build, Map<String, Object> buildInfo) {
        final Map<String, Object> causeInfo = [
                "cause_type"       : cause.getClass().toString(),
                "cause_description": cause.getShortDescription()
        ]
        causeInfo << buildInfo
        fluentLogger.log("trigger.default", causeInfo.sort())
    }

    void processFailed(Run build, Map<String, Object> buildInfo) {
        FailureCauseBuildAction bfa = build.getAction(FailureCauseBuildAction.class)
        if (!bfa) return
        bfa.getFailureCauseDisplayData().getFoundFailureCauses().each { c -> processBfaCause(c, buildInfo) }
    }

    void processBfaCause(FoundFailureCause cause, Map<String, Object> buildInfo) {
        List<FoundIndication> foundIndications = cause.getIndications()
        foundIndications.each { indication -> processBfaCauseIndication(indication, cause, buildInfo) }
    }

    void processBfaCauseIndication(FoundIndication indication, FoundFailureCause cause, Map<String, Object> buildInfo) {
        final List<String> categories = cause.getCategories()
        final Map<String, Object> causeInfo = [
                "cause"     : cause.getName(),
                "categories": categories ? categories.join(',') : "none"
        ]
        causeInfo << buildInfo
        fluentLogger.log("bfa", causeInfo.sort())
    }
}
