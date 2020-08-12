[![Build Status](https://travis-ci.com/ptrajdos/MulanMultiSearch.svg?branch=master)](https://travis-ci.com/ptrajdos/MulanMultiSearch)

[![Coverage Status](https://coveralls.io/repos/github/ptrajdos/MulanMultiSearch/badge.svg?branch=devel)](https://coveralls.io/github/ptrajdos/MulanMultiSearch?branch=master)

# MulanMultiSearch

The package implements the multi-search module for the mulan package.


## Building

Before the system is build you must install third party library -- mulan-1.5.0. To your maven local repository.
The 'maven central' repository contains only v1.4.0  library, although the newer version may be downloaded [here](https://sourceforge.net/projects/mulan/files/mulan-1-5/mulan-1.5.0.zip/download). In order to do so execute 'thirPartyJars/installJars.sh'

The build system is Maven. To compile the package use:

```console
mvn package
```


