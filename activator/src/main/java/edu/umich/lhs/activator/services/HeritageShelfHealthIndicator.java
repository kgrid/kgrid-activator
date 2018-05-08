package edu.umich.lhs.activator.services;

import static org.springframework.boot.actuate.health.Health.unknown;

import edu.umich.lhs.activator.exception.ActivatorException;
import edu.umich.lhs.activator.repository.Shelf;
import java.io.File;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
public class HeritageShelfHealthIndicator extends AbstractHealthIndicator {

  private final Shelf shelf;

  @Autowired
  public HeritageShelfHealthIndicator(Shelf shelf) { this.shelf = shelf; }

  /**
   * Shows the external adapter directory and a list of the valid adapters loaded internally and from the adapter directory.
   * @param builder
   */

  @Override
  protected void doHealthCheck(Health.Builder builder) {

    String shelfPath = shelf.getShelfPath();
    File shelfDir = new File(shelfPath);
    int numberObjectsOnShelf = shelf.getAllObjects().size();

    builder
        .withDetail("Shelf directory", shelfPath)
        .withDetail("Number of objects on the shelf", numberObjectsOnShelf);

    if (shelfPath.isEmpty()) {
      builder.withDetail("External shelf directory is not configured", "").unknown();
    } else if (!shelfDir.exists() ) {
      builder.down(new ActivatorException("Cannot find shelf directory " + shelfPath));
    } else if (!shelfDir.isDirectory() ){
      builder.down(new ActivatorException(shelfPath + " is not a valid directory"));
    } else if (!shelfDir.canWrite()) {
      builder.withDetail("Warning", "Cannot write to the shelf directory " + shelfPath).unknown();
    } else {
      builder.up();
    }
  }
}