#!/bin/bash

 mkdir shelf
 .circleci/download_assets.sh https://api.github.com/repos/kgrid-objects/cpic-objects/releases/latest
 .circleci/download_assets.sh https://api.github.com/repos/kgrid-objects/mopen-opioid-collection/releases/latest
 .circleci/download_assets.sh https://api.github.com/repos/kgrid-objects/example-projects/releases/latest
 .circleci/download_assets.sh https://api.github.com/repos/kgrid-objects/cancer-risk/releases/latest
 .circleci/download_assets.sh https://api.github.com/repos/kgrid-objects/script-numerate/releases/latest
 unzip -o opioid-all.zip -d shelf
 unzip -o cpic-all.zip -d shelf
 unzip -o hello-world.zip -d shelf
 unzip -o cancer-risk.zip -d shelf
 unzip -o scriptnuemrate-all.zip -d shelf
