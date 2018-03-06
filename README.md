easy-validate-dans-bag
===========
[![Build Status](https://travis-ci.org/DANS-KNAW/easy-validate-dans-bag.png?branch=master)](https://travis-ci.org/DANS-KNAW/easy-validate-dans-bag)

Determines whether a DANS bag is valid according to the DANS BagIt Profile.

SYNOPSIS
--------

    easy-validate-dans-bag [--aip] [--response-format,-f json|text] <bag>
    easy-validate-dans-bag run-service


DESCRIPTION
-----------

Determines whether a DANS bag is valid according to the DANS BagIt Profile v0 or v1. If the bag
does not specify what version of the profile it claims to comply with, v0 is assumed. This module has
both a command line and an HTTP interface. The command line interface is documented in the
[ARGUMENTS](#arguments) section below.

The HTTP interface supports the following path patterns and methods:

Path                                                     | Method | Description
---------------------------------------------------------|--------|------------------------------------------------------
`/`                                                      | `GET`  | Returns a message stating that the server is running.
`/validate?[infoPackageType=AIP\|SIP]&uri=<bag-uri>`     | `POST` | Validates the bag at `<bag-uri>` an returns the result as a JSON-document.

`<bag-uri>` may be a file-URI to a directory (e.g., `file:///some/path/to/bagdir`). In fact, this is the only type of URI
that will be implemented in the first version.

TODO: status messages

### Response message
The response message has the following structure:

Field                    | Type   | Description
-------------------------|--------|-----------------------------------
Bag                      | String | The name of the bag.
Bag URI                  | URI    | The URI of the bag to validate.
Profile version          | String | The version of the profile used in the validation.
Information package type | Enum   | `SIP` or `AIP`: the type of information package the bag was validated as.
Result                   | Enum   | `COMPLIANT` or `NOT COMPLIANT`.
Rule violations          | Map    | The rules that were violated. The violations are (rule number, violation details) pairs. This field is absent if Result is `COMPLIANT`.

This message can be serialized in one of two formats: plain text or JSON. A general description follows. See the [EXAMPLES](#examples) section for
concrete examples of what the output should look like.

#### Plain text format
The plain text serialization has the following layout for simple value fields (i.e. everything except "Rule violations"):

    <field-name>: <field-value>

And for the "Rule violations" map:

    Rule violations:
        - [<rule-number>] <violation-details>

where the pattern on the second line is instantiated for for each violation.

#### JSON format
The JSON serialization is a map from `<field-name>` to `<field-value>`

    {
        "<field-name>": "<field-value>" [, "<field-name>": "<field-value>"...]
    }

The `<field-value>` of "Rule violations" is itself a map from `<rule-number>` to `<violation-details>`:

    {
        "<rule-number>"; "<violation-details>" [, "<rule-number>"; "<violation-details>"...]
    }


ARGUMENTS
---------

    Options:

          --aip                      Validate as AIP (instead of as SIP)
      -f, --response-format  <arg>   Format for the result report (default = text)
          --help                     Show help message
          --version                  Show version of this program

    Subcommand: run-service - Starts EASY Validate Dans Bag as a daemon that services HTTP requests
          --help   Show help message
    ---

EXAMPLES
--------
Note that the first line of each response (introduced by `OK:` or `ERROR:`) is returned on the STDERR. It is following by the
response message


    $ easy-validate-dans-bag bag1
    OK: bag1 complies with DANS BagIt Profile v1.
    Bag URI: file:///some/path/to/bag1
    Bag: bag1
    Profile version: 0
    Information package type: SIP
    Result: COMPLIANT

    $ easy-validate-dans-bag bag2
    ERROR: bag2 does NOT comply with DANS BagIt Profile v0.
    Bag URI: file:///some/path/to/bag2
    Bag: bag2
    Profile version: 0
    Information package type: AIP
    Result: NOT COMPLIANT
    Rule violations:
    - [1.2.1] No bag-info.txt found.
    - [2.2] Directory "metadata" contains undocumented file "extra-metadata.xml".

    $ easy-validate-dans-bag --response-format json bag2
    ERROR: bag2 does NOT comply with DANS BagIt Profile v0.
    {
        bag_uri: "file:///some/path/to/bag2",
        bag: "bag2",
        profile_version: 0,
        info_package_type: "AIP",
        result: "NOT COMPLIANT",
        rule_violations: {
           "1.2.1": "No bag-info.txt found.",
           "2.2": "Directory \"metadata\" contains undocumented file \"extra-metadata.xml\"."
        }
    }

    $ curl http://localhost:20180/validate?uri=file:///var/opt/dans.knaw.nl/tmp/\
       easy-ingest-flow-inbox/4a341441-55c3-4a41-8abf-54e8dc73a672/bag
    {
       bag_uri: "file:///var/opt/dans.knaw.nl/tmp/easy-ingest-flow-inbox/4a341441-55c3-4a41-8abf-\
          54e8dc73a672/bag",
       bag: "bag",
       profile_version: 1,
       result: "COMPLIANT"
    }

    $ curl http://localhost:20180/validate?infoPackageType=aip&uri=file:///var/opt/dans.knaw.nl/tmp/\
       easy-ingest-flow-inbox/4a341441-55c3-4a41-8abf-54e8dc73a672/bag
    {
       bag_uri: "file:///var/opt/dans.knaw.nl/tmp/easy-ingest-flow-inbox/4a341441-55c3-4a41-8abf-\
          54e8dc73a672/bag",
       bag: "bag",
       profile_version: 1,
       result: "COMPLIANT"
    }



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