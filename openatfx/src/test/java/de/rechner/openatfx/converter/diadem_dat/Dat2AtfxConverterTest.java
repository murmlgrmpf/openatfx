package de.rechner.openatfx.converter.diadem_dat;

import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import de.rechner.openatfx.converter.ConvertException;
import de.rechner.openatfx.converter.ConverterFactory;
import de.rechner.openatfx.converter.IConverter;


/**
 * Test case for <code>de.rechner.openatfx.basestructure.BaseAttributeImpl</code>.
 * 
 * @author Christian Rechner
 */
public class Dat2AtfxConverterTest {

    private static IConverter dat2AtfxConverter;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        dat2AtfxConverter = ConverterFactory.getInstance().createConverter("diadem_dat2atfx");
    }

    @Test
    public void testConvert() {
        URL url = Dat2AtfxConverterTest.class.getResource("/de/rechner/openatfx/converter/diadem_dat/testdata.DAT");
        File sourceFile = new File(url.getFile());
        File targetFile = new File("D:/PUBLIC/transfer.atfx");
        try {
            dat2AtfxConverter.convert(new File[] { sourceFile }, targetFile, new Properties());
        } catch (ConvertException e) {
            fail(e.getMessage());
        }
    }

    // @Test
    // public void testConvertBulk() {
    // File sourceDir = new File("D:/PUBLIC/TestData/dat/hvb/Batterie1");
    // File[] sourceFiles = sourceDir.listFiles(new FileFilter() {
    //
    // public boolean accept(File pathname) {
    // return pathname.getName().toLowerCase().endsWith(".dat");
    // }
    // });
    //
    // File targetFile = new File("D:/PUBLIC/transfer.atfx");
    // try {
    // dat2AtfxConverter.convert(sourceFiles, targetFile, new Properties());
    // } catch (ConvertException e) {
    // fail(e.getMessage());
    // }
    // }
}
