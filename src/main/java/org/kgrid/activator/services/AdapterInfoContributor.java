package org.kgrid.activator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AdapterInfoContributor implements InfoContributor {
    @Autowired
    Environment environment;

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("kgrid.activator.adapter-locations", environment.getProperty("kgrid.activator.adapter-locations"));
    }
}
