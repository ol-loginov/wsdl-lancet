package com.github.olloginov;

import java.util.Map;

public interface Lancet {
    void process(FilterTree include, FilterTree exclude);
}
