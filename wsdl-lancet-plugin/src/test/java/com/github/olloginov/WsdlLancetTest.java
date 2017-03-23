package com.github.olloginov;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

import java.io.*;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class WsdlLancetTest {
    private static <T> T[] arrayOf(T... params) {
        return params;
    }

    @Test
    public void processBiletix() throws Exception {
        File target = File.createTempFile("wsdl-lancet-test", ".wsdl");
        File source = new File(WsdlLancetTest.class.getResource("/biletix-source.wsdl").getFile());
        LancetConfiguration configuration = new LancetConfiguration(
                source, target,
                new TreeMap<String, String>() {{
                    put("tais", "http://www.tais.ru/");
                }},
                new FilterTree(
                        arrayOf(
                                new FilterPortType(
                                        "tais:TAISSoapPort",
                                        arrayOf(
                                                new FilterName("tais:GetOptimalFares")
                                        )
                                )
                        )
                ),
                new FilterTree()
        );

        LancetWrapper lancet = new LancetWrapper(new SystemStreamLog());
        lancet.process(new LancetConfiguration[]{configuration});

        assertContentEquals(new File(WsdlLancetTest.class.getResource("/biletix-target.wsdl").getFile()), target);
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
