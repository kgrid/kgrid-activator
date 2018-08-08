#!/bin/bash

 mkdir shelf
 .circleci/download_assets.sh https://api.github.com/repos/kgrid/CPIC-objects/releases/latest
 .circleci/download_assets.sh https://api.github.com/repos/kgrid/mopen-opioid-collection/releases/latest
 .circleci/download_assets.sh https://api.github.com/repos/kgrid/kgrid-activator/releases/latest
 .circleci/download_assets.sh https://api.github.com/repos/kgrid-objects/cancer-risk/releases/latest
 unzip -o opioid-all.zip -d shelf
 unzip -o cpic-all.zip -d shelf
 unzip -o hello-world.zip -d shelf
 unzip -o cancer-risk.zip -d shelf