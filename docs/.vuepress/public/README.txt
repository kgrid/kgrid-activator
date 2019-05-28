This documentation repo is built with VuePress.

The /dist directory built by VuePress from the /docs
directory is force-pushed to the gh-pages branch in total.

The /public directory contains a .nojekyll file to keep
Github Pages publishing from processing the files built
by VuePress.

The public directory also contains .circleci/config.yml
file that tells the CircleCI build process not to
(re)build the branch.

This README.txt file is to remind us what's going on.

