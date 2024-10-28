package au.org.ala.parser;

import static org.junit.Assert.assertEquals;

import au.org.ala.pipelines.parser.LicenseParser;
import org.junit.Before;
import org.junit.Test;

public class LicenseParserTest {
  private LicenseParser lp;

  @Before
  public void init() {
    lp = LicenseParser.getInstance();
  }

  @Test
  public void randomTest() {
    assertEquals("CC BY-NC-SA 3.0", lp.matchLicense("creativecommons  by  nc sa 3.0"));
    assertEquals("CC BY-SA 3.0", lp.matchLicense("creativecommons  by   sa 3.0"));
    assertEquals("CC BY-NC 3.0", lp.matchLicense("cc  by   NC 3.0"));
    assertEquals("CC0 1.0", lp.matchLicense("cc0"));
    assertEquals("Custom", lp.matchLicense("test"));
  }

  @Test
  public void fullTest() {
    assertEquals("CC BY-NC-SA 3.0", lp.matchLicense(" creativecommons by nc sa 3.0 Int))? "));
    assertEquals("CC BY-NC-ND 3.0", lp.matchLicense(" creativecommons by nc nd 3.0 Int))? "));
    assertEquals("CC BY-NC-SA 2.5", lp.matchLicense(" creativecommons by nc sa 2.5 Int))? "));
    assertEquals("CC BY-NC-ND 2.5", lp.matchLicense(" creativecommons by nc nd 2.5 Int))? "));
    assertEquals("CC BY-NC-SA 2.0", lp.matchLicense(" creativecommons by nc sa 2.0 Int))? "));
    assertEquals("CC BY-NC-ND 2.0", lp.matchLicense(" creativecommons by nc nd 2.0 Int))? "));
    assertEquals("CC BY-NC-SA 1.0", lp.matchLicense(" creativecommons by nc sa 1.0 Int))? "));
    assertEquals("CC BY-ND-NC 1.0", lp.matchLicense(" creativecommons by nd nc 1.0 Int))? "));
    assertEquals("CC BY-NC-SA 4.0", lp.matchLicense(" creativecommons by nc sa 4.0)? Int))? "));
    assertEquals("CC BY-NC-ND 4.0", lp.matchLicense(" creativecommons by nc nd 4.0)? Int))? "));
    assertEquals("CC BY-SA 3.0", lp.matchLicense(" creativecommons by sa 3.0 Int))? "));
    assertEquals("CC BY-NC 3.0", lp.matchLicense(" creativecommons by nc 3.0 Int))? "));
    assertEquals("CC BY-ND 3.0", lp.matchLicense(" creativecommons by nd 3.0 Int))? "));
    assertEquals("CC BY-SA 2.5", lp.matchLicense(" creativecommons by sa 2.5 Int))? "));
    assertEquals("CC BY-NC 2.5", lp.matchLicense(" creativecommons by nc 2.5 Int))? "));
    assertEquals("CC BY-ND 2.5", lp.matchLicense(" creativecommons by nd 2.5 Int))? "));
    assertEquals("CC BY-SA 2.0", lp.matchLicense(" creativecommons by sa 2.0 Int))? "));
    assertEquals("CC BY-NC 2.0", lp.matchLicense(" creativecommons by nc 2.0 Int))? "));
    assertEquals("CC BY-ND 2.0", lp.matchLicense(" creativecommons by nd 2.0 Int))? "));
    assertEquals("CC BY-SA 1.0", lp.matchLicense(" creativecommons by sa 1.0 Int))? "));
    assertEquals("CC BY-NC 1.0", lp.matchLicense(" creativecommons by nc 1.0 Int))? "));
    assertEquals("CC BY-ND 1.0", lp.matchLicense(" creativecommons by nd 1.0 Int))? v"));
    assertEquals("CC BY-SA 4.0", lp.matchLicense(" creativecommons by sa 4.0)? Int))? "));
    assertEquals("CC BY-NC 4.0", lp.matchLicense(" creativecommons by nc 4.0)? Int))? "));
    assertEquals("CC BY-ND 4.0", lp.matchLicense(" creativecommons by nd 4.0)? Int))? "));
    assertEquals("CC BY 3.0", lp.matchLicense(" creativecommons by 3.0 Int))? "));
    assertEquals("CC BY 2.5", lp.matchLicense(" creativecommons by 2.5 Int))? "));
    assertEquals("CC BY 2.0", lp.matchLicense(" creativecommons by 2.0 Int))? "));
    assertEquals("CC BY 1.0", lp.matchLicense(" creativecommons by 1.0 Int))? "));
    assertEquals("CC BY 4.0", lp.matchLicense(" creativecommons by 4.0)? Int))? "));
    assertEquals("CC BY-NC-SA 4.0", lp.matchLicense(" attribution non-commercial sa"));
    assertEquals("CC BY-NC-ND 4.0", lp.matchLicense("attribution nc no-deriv "));
    assertEquals("CC BY-NC 4.0", lp.matchLicense("attribution nc"));
    assertEquals("CC BY-ND 4.0", lp.matchLicense("	 attribution nd"));
    assertEquals("CC BY-SA 4.0", lp.matchLicense("	 attribution sa"));
    assertEquals("CC BY-NC 4.0", lp.matchLicense("	 attribution non-commercial "));
    assertEquals("CC BY-ND 4.0", lp.matchLicense("	 attribution nd "));
    assertEquals("CC BY 4.0", lp.matchLicense(" attribution "));
    assertEquals("CC0 1.0", lp.matchLicense("creativecommons zero "));
    assertEquals("CC0 1.0", lp.matchLicense("creativecommons-0 "));
    assertEquals("CC0 1.0", lp.matchLicense("creativecommons0 "));
    assertEquals("PDM", lp.matchLicense("pdm "));
    assertEquals("PDM", lp.matchLicense("public domain mark "));
    assertEquals("Custom", lp.matchLicense("	 "));
  }
}
