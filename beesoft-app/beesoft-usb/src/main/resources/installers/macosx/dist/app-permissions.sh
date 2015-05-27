#!/bin/bash

# directory were the .app is located
cd "$1"

chmod -R +x Contents/Resources/pypy/
chmod -R +x Contents/Resources/curaEngine/
