easy-validate-dans-bag
===========
[![Build Status](https://travis-ci.org/DANS-KNAW/easy-validate-dans-bag.png?branch=master)](https://travis-ci.org/DANS-KNAW/easy-validate-dans-bag)

Determines whether a DANS bag is valid according to the DANS BagIt Profile.

SYNOPSIS
--------

    easy-validate-dans-bag [--aip] [--bag-store <uri>] [--response-format,-f json|text] <bag>
    easy-validate-dans-bag run-service


DESCRIPTION
-----------

Determines whether a DANS bag is valid according to the DANS BagIt Profile v0 or v1. If the bag
does not specify what version of the profile it claims to comply with, v0 is assumed. This module has
both a command line and an HTTP interface. The command line interface is documented in the
[ARGUMENTS](#arguments) section below.


ARGUMENTS
---------

    Options:

          --aip                      Validate as AIP (instead of as SIP)
          --bag-store  <arg>         The bag store to use for deep validation
      -f, --response-format  <arg>   Format for the result report (default = text)
      -h, --help                     Show help message
      -v, --version                  Show version of this program
    
     trailing arguments:
      bag (not required)   The bag to validate
    
    Subcommand: run-service - Starts EASY Validate Dans Bag as a daemon that services HTTP requests
      -h, --help   Show help message
    ---


HTTP
----
#### Requests
The HTTP interface supports the following path patterns and methods:


Path                                                                               | Method | Description
-----------------------------------------------------------------------------------|--------|------------------------------------------------------
`/`                                                                                | `GET`  | Returns a message stating that the server is running.
`/validate?[infoPackageType=AIP\|SIP]&uri=<bag-uri>[&bag-store=<bag-store-uri>]`   | `POST` | Validates the bag at `<bag-uri>` and returns the result as a JSON-document.

* `<bag-uri>` may be a file-URI to a directory (e.g., `file:///some/path/to/bagdir`). In fact, this is the only type of URI
  that will be implemented in the first version.

* By default the validation will only check rules that can be verified independently of any bag store. If the query parameter `bag-store` is provided
  and contains the URL of a specific bag store, the rules will be verified in the context of that bag store. In particular, the virtual validity
  of the bag will be checked by resolving the [local-file-uri]'s in that bag store.

* If the `Accept` header is specified and set to `application/json` the [response message](#response-message) will be formatted as a JSON object, otherwise
  it will be formatted as plain text.

#### Responses
* The `/validate` route will return `200 OK` if the bag was compliant and `400 Bad Request` if the bag was not compliant. In both cases the body of the
  response will contain the [response message](#response-message).
* A `500 Internal Server Error` if some other error occurs.

### Response message
The response message has the following structure:

Field                    | Type   | Description
-------------------------|--------|-----------------------------------
Bag                      | String | The name of the bag.
Bag URI                  | URI    | The URI of the bag to validate.
Profile version          | Number | The version of the profile used in the validation.
Information package type | Enum   | `SIP` or `AIP`: the type of information package the bag was validated as.
Result                   | Enum   | `COMPLIANT` or `NOT_COMPLIANT`.
Rule violations          | Map    | The rules that were violated. The violations are (rule number, violation details) pairs. This field is absent if Result is `COMPLIANT`.

This message can be serialized in one of two formats: plain text or JSON. A general description follows. See the [EXAMPLES](#examples) section for
concrete examples of what the output should look like.

#### Plain text format
The plain text serialization has the following layout for simple value fields (i.e. everything except "Rule violations"):

    <field-name>: <field-value>

And for the "Rule violations" map:

    Rule violations:
        - '['<rule-number>']' <violation-details>

where the pattern on the second line is instantiated for for each violation.

#### JSON format
The JSON serialization is a map from `<field-name>` to `<field-value>`

    {
        "<field-name>": "<field-value>" [, "<field-name>": "<field-value>"...]
    }

The `<field-value>` of "Rule violations" is itself a map from `<rule-number>` to `<violation-details>`:

    {
        "<rule-number>"; "<violation-details>" [, "<rule-number>": "<violation-details>"...]
    }


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
Currently this project is built as an RPM package for RHEL7/CentOS7 and later. The RPM will install the binaries to
`/opt/dans.knaw.nl/easy-validate-dans-bag` and the configuration files to `/etc/opt/dans.knaw.nl/easy-validate-dans-bag`. 

To install the module on systems that do not support RPM, you can copy and unarchive the tarball to the target host.
You will have to take care of placing the files in the correct locations for your system yourself. For instructions
on building the tarball, see next section.


BUILDING FROM SOURCE
--------------------
Prerequisites:

* Java 8 or higher
* Maven 3.3.3 or higher
* RPM

Steps:
    
    git clone https://github.com/DANS-KNAW/easy-validate-dans-bag.git
    cd easy-validate-dans-bag 
    mvn clean install

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM 
packaging will be activated. If `rpm` is available, but at a different path, then activate it by using
Maven's `-P` switch: `mvn -Pprm install`.

Alternatively, to build the tarball execute:

    mvn clean install assembly:single

[local-file-uri]: https://dans-knaw.github.io/easy-bag-store/03_definitions.html#local-file-uri
