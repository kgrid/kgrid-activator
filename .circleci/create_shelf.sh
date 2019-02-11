#!/bin/bash

 mkdir application/shelf
 repos=(opioid-collection cpic-collection example-collection cancer-risk-collection icon-array script-numerate postpci labwise ipp-collection)

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

