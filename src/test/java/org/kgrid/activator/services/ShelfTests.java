package org.kgrid.activator.services;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.shelf.repository.FilesystemCDOStore;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class ShelfTests {

  @Test
  public void findByArkId() throws URISyntaxException {

    final URI shelfUri = this.getClass().getResource("/shelf").toURI();
//    Path path = Paths.get(shelfUri);
    FilesystemCDOStore cdoStore = new FilesystemCDOStore("filesystem:" + shelfUri.toString());

  }
}
