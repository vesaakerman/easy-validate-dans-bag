easy-validate-dans-bag
===========
[![Build Status](https://travis-ci.org/DANS-KNAW/easy-validate-dans-bag.png?branch=master)](https://travis-ci.org/DANS-KNAW/easy-validate-dans-bag)

Determines whether a DANS bag is valid according to the DANS BagIt Profile.


SYNOPSIS
--------

    easy-validate-dans-bag [--aip] [--response-format json|text] <bag>
    easy-validate-dans-bag run-service

TODO: ability to skip non-local validation??

DESCRIPTION
-----------

Determines whether a DANS bag is valid according to the DANS BagIt Profile v0 or v1. If the bag
does not specify what version of the profile it claims to comply with, v0 is assumed. This module has
both a command line and an HTTP interface.


ARGUMENTS
---------

    Options:

        --help      Show help message
        --version   Show version of this program

    Subcommand: run-service - Starts EASY Validate Dans Bag as a daemon that services HTTP requests
        --help   Show help message
    ---

EXAMPLES
--------

    easy-validate-dans-bag -o value


INSTALLATION AND CONFIGURATION
------------------------------
The preferred way of install this module is using the RPM package. This will install the binaries to
`/opt/dans.knaw.nl/easy-validate-dans-bag`, the configuration files to `/etc/opt/dans.knaw.nl/easy-validate-dans-bag`,
and will install the service script for `initd` or `systemd`.

If you are on a system that does not support RPM, you can use the tarball. You will need to copy the
service scripts to the appropiate locations yourself.

BUILDING FROM SOURCE
--------------------

Prerequisites:

* Java 8 or higher
* Maven 3.3.3 or higher
* RPM (if you want to build the RPM package).

Steps:

    git clone https://github.com/DANS-KNAW/easy-validate-dans-bag.git
    cd easy-validate-dans-bag
    mvn install

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM
packaging will be activated. If `rpm` is available, but at a different path, then activate it by using
Maven's `-P` switch: `mvn -Pprm install`.