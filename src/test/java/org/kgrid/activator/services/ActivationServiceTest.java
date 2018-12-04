package org.kgrid.activator.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.activator.KgridActivatorApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KgridActivatorApplication.class})
public class ActivationServiceTest {

  @Autowired
  ActivationService as;

  @Test
  public void loadAndActivateEndPoints() {

    as.loadAndActivateEndPoints();

  }

}