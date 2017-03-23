package com.github.olloginov;

import lombok.*;
import org.apache.maven.plugins.annotations.Parameter;

import static com.github.olloginov.support.SmartArray.safeList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FilterName implements FilterNamedElement {
    @Parameter
    private String name;

    public static Array array(FilterName[] array) {
        return new Array(array);
    }

    void visit(FilterVisitor visitor) {
        visitor.onFilterName(this);
    }

    @RequiredArgsConstructor
    public static class Array {
        private final FilterName[] array;

        public boolean contains(String name) {
            for (FilterName el : safeList(array)) {
                if (name.equals(el.getName())) {
                    return true;
                }
            }
            return false;
        }
    }
}
