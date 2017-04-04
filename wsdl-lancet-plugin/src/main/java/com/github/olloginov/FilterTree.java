package com.github.olloginov;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.maven.plugins.annotations.Parameter;

import static com.github.olloginov.support.SmartArray.safeList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FilterTree {
    @Parameter
    private FilterPortType[] portTypes = new FilterPortType[0];

    public void visit(FilterVisitor visitor) {
        for (FilterPortType portType : safeList(portTypes)) {
            portType.visit(visitor);
        }
    }
}
