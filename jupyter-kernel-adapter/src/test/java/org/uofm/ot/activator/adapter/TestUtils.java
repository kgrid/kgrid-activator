package org.uofm.ot.activator.adapter;

import java.io.IOException;
import java.util.Scanner;

/**
 * Convenience methods for testing.
 * Created by grosscol on 2017-06-16.
 */
public class TestUtils {
  // Convenience method to avoid typing .json at end of resource names
  public static String jsonFixture(String fixtureName) throws IOException {
    return loadFixture(fixtureName + ".json");
  }

  // Helper function to retrieve string fixtures from test package resources
  public static String loadFixture(String fixtureName) throws IOException {
    String fixture = new Scanner(
        TestUtils.class.getResourceAsStream("/fixtures/" + fixtureName), "UTF-8")
        .useDelimiter("\\A").next();
    return fixture;
  }
}
