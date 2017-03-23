package com.github.olloginov;

import lombok.*;
import org.apache.maven.plugins.annotations.Parameter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BindingOperationRule {
    @Parameter
    private String name;
}
