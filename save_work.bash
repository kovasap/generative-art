#!/bin/bash

[[ -z "$1" ]] && { echo "Must specify name to save to!" ; exit 1; }

echo "Saving as $1"
clojure -m figwheel.main -bo dev
rm -r built_works/$1/
mkdir built_works/$1/
mv target/public/cljs-out built_works/$1/
