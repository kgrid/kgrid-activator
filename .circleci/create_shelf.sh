#!/bin/bash

  shelfUrl=$1
  repos=($2)

  manifest='{"manifest":['
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

     assets=($(curl -s $url | jq -r ".assets[].browser_download_url"))
     echo ${#assets[@]} assests downloading

     for asseturl in "${assets[@]}"
     do
        if [[ $asseturl != *"-all.zip" ]]; then
            manifest+="\"$asseturl\","
        fi
     done

  done

  manifest=${manifest%?}
  manifest+="]}"

  echo "$manifest"
  curl -X POST "${shelfUrl}/manifest"\
      -H "Content-Type: application/json" \
      -d "$manifest"

