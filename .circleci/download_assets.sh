#!/bin/bash

assets=($(curl -s $1 | jq -r ".assets[].browser_download_url"))

for url in "${assets[@]}"
do
	curl -L -O $url
done