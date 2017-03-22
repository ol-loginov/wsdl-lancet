package com.github.olloginov;

import lombok.Data;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Data
public class Wsdl {
    /**
     * @parameter
     */
    @Parameter(required = true)
    private File source;
    /**
     * @parameter
     */
    @Parameter(required = true)
    private File target;
    @Parameter
    /**
     * @parameter
     */
    private WsdlRule[] includes;
    @Parameter
    /**
     * @parameter
     */
    private WsdlRule[] excludes;
}
