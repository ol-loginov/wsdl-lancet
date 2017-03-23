package com.github.olloginov;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.apache.maven.plugins.annotations.Parameter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WsdlRule {
    @Parameter
    private BindingRule[] binding;

    public BindingRule getBindingByName(String name) {
        for (BindingRule item : binding) {
            if (name != null && name.equals(item.getName())) {
                return item;
            }
        }
        return null;
    }
}
