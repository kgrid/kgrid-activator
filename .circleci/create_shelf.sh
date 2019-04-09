#!/bin/bash

  shelfUrl=$1
  repos=($2)

  mainifest='{"ko":['
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
        mainifest+="\"$asseturl\","
     done

  done

  mainifest=${mainifest%?}
  mainifest+="]}"

  echo $mainifest

  curl -X POST "${shelfUrl}"\
      -H "Content-Type: application/json" \
      -d "$mainifest"

