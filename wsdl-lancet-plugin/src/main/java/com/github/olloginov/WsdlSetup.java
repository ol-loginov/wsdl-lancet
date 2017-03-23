package com.github.olloginov;

import lombok.*;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsdlSetup {
    @Parameter(required = true)
    private File source;
    @Parameter(required = true)
    private File target;
    @Parameter
    private WsdlRule include;
    @Parameter
    private WsdlRule exclude;
}
