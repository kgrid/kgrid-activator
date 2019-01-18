#!/bin/bash

 mkdir application/shelf
 repos=(opioid-collection:1.1.0 cpic-collection:1.5.3-rc1 example-collection:1.1.0 cancer-risk-collection:1.1.0 icon-array:1.0.0 script-numerate:0.3 postpci:1.0.0 labwise:1.0.0)

  for i in "${repos[@]}"
  do
     echo -e "Download release  $i "
     if [[ $i == *:* ]]; then
       theReop=$(echo $i | cut -d':' -f 1)
       theTag=$(echo $i | cut -d':' -f 2)
       url=(https://api.github.com/repos/kgrid-objects/$theReop/releases/tags/$theTag?access_token=$GIT_TOKEN )
     else
       url=(https://api.github.com/repos/kgrid-objects/$i/releases/latest?access_token=$GIT_TOKEN  )
     fi
     .circleci/download_assets.sh "$url"
  done

 find . -name "*.zip" -exec unzip -o -q -d shelf {} \;

