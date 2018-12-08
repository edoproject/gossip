package com.gossip

import hudson.model.Node

class BuildHelper {

    static String getNodeName(Run run) {
        Node node = run.getNode()
        return node ? node.getNodeName() : "UNKNOWN"
    }

    static Map<String, Object> extractBuildInfo(Run build, jenkinsId = "") {
        return [
                "build"    : build.getNumber(),
                "duration" : build.getDuration(),
                "jenkins"  : jenkinsId,
                "job"      : build.getJobFullName(),
                "node"     : getNodeName(build),
                "result"   : build.getResult().toString(),
                "timestamp": build.getStartTimeInMillis()
        ]
    }
}
