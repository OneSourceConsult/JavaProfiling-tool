# JavaProfiling-tool

Tool to perform profiling of Java applications and the system they're running on.

## Objective

This project started as a way to facilitate the profiling of the Java applications used within a project. Existing tools fail to provide a single, simple and lightweight method to perform measurements and display these results in a human-readable format.

## Features

- Integration within any Java application
- No need for instatiation, nor having variables passing through your code. Just a **single** code call into a static method
- Secondary tool to do the heavy work of processing the results and creating human-readable graphs

## Requirements

To run both the measurement tool and the processing tool, the following requirements must be met (although earlier versions of the presented software, might work):

- Java Virtual Machine ≥ 1.6
- Python ≥ 2.7.5
- Gnuplot ≥ 4.6.5

**Note:** Since the processing tool is a command line tool, it is assumed that a little bash knowledge has already been acquired.

## Usage

Assuming that all the above requirements are met:

1. The measurement tool should be included in any Java project by importing the file ``cf-javalogger-0.4.5.jar``.
2. According to your system, place in the classpath directory either ``libsigar-amd64-linux.so``, ``libsigar-x86-linux.so`` or ``libsigar-universal64-macosx.dylib``.
3. Import the following into your Java source file.

```java
import static cf.os.javalogger.core.Log.*;
```

4. Perform measurements by performing calls in your code. Bear in mind that you should have a key suffixed by either ***_start*** or ***_stop***, depending wether it's the begin or the termination of a given measurement key.

```java
Boolean stuff = true;
measure("some_stuff_start", "Stuff", "Test 1");
measure("some_stuff_stop", "Stuff", "Test 2", stuff);
closeLogger(); // Should close logger to assert everything was properly flushed. This is only needed ONCE.
```

5. Edit the top of the file ``lp.py``, changing the following variables.

```python
# Where the log files can be found
DIR_PRE = "/home/user/log_files/"
# Subdirectories, in case you produced several measurements
DIR = ["m1", "m2", "m3"]
# Another branch of subdirectories inside which separate different tests (You can leave this list empty)
VPS_COMPS = ["LOGS"]
# Keys to find within log files, without the _start and _stop suffixes
KEYS = ["some_stuff"]
```

6. Run the parser

```bash
$ python lp.py
```

## Source

If not doing so already, you can download the latest version of the JavaProfiling-tool by cloning the [github](https://github.com/OneSourceConsult/JavaProfiling-tool) repository.

## Copyright

Copyright (c) 2014 OneSource Consultoria Informatica, Lda. [🔗](http://www.onesource.pt)

This project has been developed in the scope of the CityFlow project[🔗](http://www.cityflow.eu/) by João Gonçalves

## License

Distributed under the MIT license. See ``LICENSE`` for more information.

##About

OneSource is the core institution that supported this work. Regarding queries about further development of custom solutions and consultancy services please contact us by email: **_geral✉️onesource.pt_** or through our website: <http://www.onesource.pt>

OneSource is a Portuguese SME specialised in the fields of data communications, security, networking and systems management, including the consultancy, auditing, design, development and lifetime administration of tailored IT solutions for corporate networks, public-sector institutions, utilities and telecommunications operators.

Our company is a start-up and technological spin-off from Instituto Pedro Nunes (IPN), a non-profit private organisation for innovation and technology transfer between the University of Coimbra and the industry and business sectors. Faithful to its origins, OneSource keeps a strong involvement in R&D activities, participating in joint research projects with academic institutions and industrial partners, in order to be able to provide its customers with state-of-art services and solutions.
