#!/bin/bash

# This script builds and packages art generated in this repository so that it
# can be inserted into arbitraty html.  To insert, use (for example):
# 
# <div id="double-helix"></div>
# <script src="/double_helix/cljs-out/dev-main.js"></script>
#
# You'll need the built_works/double_helix directory available as a static
# resource (e.g. in the static/ directory for Hugo) to make this work.

# [[ -z "$1" ]] && { echo "Must specify name to save to!" ; exit 1; }

# echo "Saving as $1"
clojure -m figwheel.main -bo all
rsync -av target/public/cljs-out built_works/
# rm -r built_works/$1/
# mkdir built_works/$1/
# mv target/public/cljs-out built_works/$1/
# sed -i "s:cljs-out:$1/cljs-out:g" built_works/$1/cljs-out/dev-main.js
