#!/bin/bash

 repos=(opioid-collection cpic-collection example-collection cancer-risk-collection icon-array script-numerate postpci labwise )

 for i in "${repos[@]}"
 do
  url=(https://api.github.com/repos/kgrid-objects/$i/releases/latest)
  .circleci/download_assets.sh "$url"
 done

 find . -name "*.koio.zip" -exec unzip -o -q -d shelf {} \;

