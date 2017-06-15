package org.uofm.ot.activator.adapter;

import java.io.IOException;
import java.util.Scanner;

/**
 * Convenience methods for testing.
 * Created by grosscol on 2017-06-16.
 */
public class TestUtils {
  // Helper function to retrieve json fixtures from test package resources
  public static String jsonFixture(String fixtureName) throws IOException {
    String json = new Scanner(
        TestUtils.class.getResourceAsStream("/fixtures/" + fixtureName + ".json"), "UTF-8")
        .useDelimiter("\\A").next();
    return json;
  }

}
