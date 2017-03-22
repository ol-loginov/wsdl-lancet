package com.github.olloginov;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WsdlLancet {
    private final org.apache.maven.plugin.logging.Log log;

    public void process(Wsdl[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalStateException("no wsdl files");
        }

        for (Wsdl setup : files) {
            process(setup);
        }
    }

    private void process(Wsdl setup) {
        if (setup.getSource() == null) {
            throw new IllegalArgumentException("parameter 'source' is not set");
        }
        if (!setup.getSource().exists()) {
            throw new IllegalArgumentException("source file '" + setup.getSource() + "' is not set");
        }

        if (setup.getTarget() == null) {
            throw new IllegalArgumentException("parameter 'target' is not set");
        }
        if (!setup.getSource().exists()) {
            throw new IllegalArgumentException("target file '" + setup.getSource() + "' is not set");
        }

        if (!setup.getTarget().getParentFile().exists()) {
            if (!setup.getTarget().getParentFile().mkdirs()) {
                throw new IllegalStateException("cannot create folder " + setup.getTarget().getParent());
            }
        }


          /*
        File touch = new File(f, "touch.txt");

        FileWriter w = null;
        try {
            w = new FileWriter(touch);

            w.write("touch.txt");
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + touch, e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }   */
    }
}
