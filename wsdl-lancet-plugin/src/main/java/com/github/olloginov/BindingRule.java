package com.github.olloginov;

import lombok.*;
import org.apache.maven.plugins.annotations.Parameter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BindingRule {
    @Parameter
    private String name;
    @Parameter
    private BindingOperationRule[] operations;
}
