package com.github.olloginov;

import lombok.*;
import org.apache.maven.plugins.annotations.Parameter;

import static com.github.olloginov.support.SmartArray.safeList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FilterPortType implements FilterNamedElement {
    @Parameter
    private String name;
    @Parameter
    private FilterName[] operations = new FilterName[0];

    public boolean hasOperations() {
        return operations != null && operations.length > 0;
    }

    public static Array array(FilterPortType[] array) {
        return new Array(array);
    }

    void visit(FilterVisitor visitor) {
        visitor.onFilterPortType(this);
        for (FilterName child : safeList(operations)) {
            child.visit(visitor);
        }
    }

    @RequiredArgsConstructor
    public static class Array {
        private final FilterPortType[] array;

        public FilterPortType getPortType(String portTypeName) {
            for (FilterPortType portType : safeList(array)) {
                if (portTypeName.equals(portType.getName())) {
                    return portType;
                }
            }
            return null;
        }
    }
}
