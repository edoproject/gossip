package com.gossip

import org.mockito.ArgumentMatcher

class MapMatcher implements ArgumentMatcher<Map> {
    private Map map

    MapMatcher(Map map) {
        this.map = map
    }

    @Override
    boolean matches(Map argument) {
        this.map.sort().toString().equals(argument.sort().toString())
    }
}
