package com.github.olloginov;

public interface FilterVisitor {
    void onFilterPortType(FilterPortType filterPortType);

    void onFilterName(FilterName filterName);
}
