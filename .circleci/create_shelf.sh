#!/bin/bash

 mkdir application/shelf
 repos=(opioid-collection cpic-collection example-collection cancer-risk-collection icon-array script-numerate postpci labwise )

 for i in "${repos[@]}"
 do
  url=(https://api.github.com/repos/kgrid-objects/$i/releases/latest)
  .circleci/download_assets.sh "$url"
 done

 unzip -o opioid-all.zip -d shelf
 unzip -o cpic-all.zip -d shelf
 unzip -o hello-world.zip -d shelf
 unzip -o cancer-risk.zip -d shelf
 unzip -o icon-array.zip -d shelf
 unzip -o scriptnumerate-all.zip -d shelf
 unzip -o postpci-all.zip -d shelf
 unzip -o labwise-all.zip -d shelf
