package com.github.olloginov;

import lombok.*;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LancetConfiguration {
    @Parameter(required = true)
    private File source;
    @Parameter(required = true)
    private File target;
    @Parameter
    private Map<String, String> namespaces = new HashMap<>();
    @Parameter
    private FilterTree include = new FilterTree();
    @Parameter
    private FilterTree exclude = new FilterTree();
}
