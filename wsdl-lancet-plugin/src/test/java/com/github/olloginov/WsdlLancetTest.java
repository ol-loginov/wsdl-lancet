package com.github.olloginov;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class WsdlLancetTest {
    private static <T> T[] arrayOf(T... params) {
        return params;
    }

    @Test
    public void processEbay() throws Exception {
        File wsdlTarget = File.createTempFile("wsdl-lancet-test", ".wsdl");
        File wsdlSource = new File(WsdlLancetTest.class.getResource("/biletix-source.wsdl").getFile());
        WsdlSetup wsdl = WsdlSetup.builder()
                .source(wsdlSource)
                .target(wsdlTarget)
                .include(new WsdlRule(
                                arrayOf(
                                        new BindingRule("TAISSoapBinding",
                                                arrayOf(
                                                        new BindingOperationRule("GetMinFares")
                                                )))
                        )
                )
                .build();

        WsdlLancet lancet = new WsdlLancet(new SystemStreamLog());
        lancet.process(new WsdlSetup[]{wsdl});

        assertContentEquals(new File(WsdlLancetTest.class.getResource("/biletix-target.wsdl").getFile()), wsdlTarget);
    }

    private static String readFile(File file) throws IOException {
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while (null != (line = reader.readLine())) {
                out.append(line);
                out.append("\n");
            }
        }
        return out.toString();
    }

    private void assertContentEquals(File expected, File actual) throws IOException {
        String actualString = readFile(actual);
        String expectedString = readFile(expected);
        assertEquals("" + expectedString, "" + actualString);
    }
}
