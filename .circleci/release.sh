#!/bin/bash

 git config --global user.email "$GIT_USER_EMAIL"
 git config --global user.name "$GIT_USER_NAME"
 TAG_NAME="kgrid-activator-$RELEASE"
 mvn release:clean release:prepare -B  -DscmCommentPrefix="Release [skip ci] " -DreleaseVersion=$RELEASE -DdevelopmentVersion=$NEXT release:perform -P ci -s .circleci/settings.xml
 curl -X POST "https://api.github.com/repos/kgrid/kgrid-activator/releases?access_token=$GITHUB_TOKEN" \
         -H 'Content-Type: application/json' -d "{\"tag_name\":\"$TAG_NAME\"}"
 RELEASE_ID=$(curl -s https://api.github.com/repos/kgrid/kgrid-activator/releases/latest | jq -r ".id")
         curl -X POST "https://uploads.github.com/repos/kgrid/kgrid-activator/releases/$RELEASE_ID/assets?access_token=$GITHUB_TOKEN&name=$TAG_NAME.jar" \
           -H "Content-Type: application/octet-stream" \
           --data-binary "@target/$TAG_NAME.jar"

