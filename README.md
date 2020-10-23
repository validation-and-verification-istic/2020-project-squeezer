# Squeezer

The [Pied Piper](http://www.piedpiper.com/) company hired you as a Q&A consultant to evaluate and improve a  *Minimum Viable Product* (MVP) they are developing for a client. This MVP consists in a command line utility to compress a given file.
 
You are required to improve the quality of the code, create a full test suite and find as many bugs as possible before the deadline.

This document contains the specification given by the client to guide the development of the application and the concrete actions you are expected to complete.

## Specification

The client wants a fully functional command line application that implements, at least, the three following well-known compression algorithms:
- Run length encoding
- Huffman encoding
- Lempel-Ziv-Welch compression (LZW)

These are classic compression algorithms, so the client provided no specific information about them. You can find plenty of materials online explaining these algorithms and even their implementations.

The application to develop must be a a command line tool. It must provide two main subcommands:
- `compress` which compresses a given file and,
- `decompress` which decompresses a previously compressed file.

### `compress`

To compress a file, the user should use the `compress` subcommand as follows:

```bash
<application> compress <input> <output>
```

Example: `java -jar sequeezer.jar compress file.bin compressed-file.sqz` 

`<application>` is a placeholder for the way the application should be invoked. Both `<input>` and `<output>` are mandatory, the user must always provide them. `<input>` is an input file of any kind. It must exist and be readable, otherwise, the application should notify an error. `<output>` is the path to where the compressed file should be created. The application should not overwrite any existing file. Therefore, if `<output>` points to an existing file, the application should print an error.

The application should provide a way to select the compression algorithm. By default it uses LZW. If the user wishes to select another algorithm she should use the `-u` option:

```bash
<application> compress <input> <output> -u <algorithm>
```

Example: `java -jar sequeezer.jar compress file.bin compressed-file.sqz -u HUFFMAN`

How the application identifies each algorithm is left unspecified.

### `decompress`

To decompress a file, a user should use the `decompress` subcommand as follows:

```bash
<application> decompress <input> <output>
```

Example: `java -jar sequeezer.jar decompress compressed-file.sqz file.bin`

`<application>` is a placeholder for the way the application should be invoked. Both `<input>` and `<output>` are also mandatory, the user must always provide them. `<input>` is a previously compressed file. It must exist and be readable. The application should print an error in case the file does not exist, can not be read or it hasn't been compressed by the application, that is, if it has the wrong format.  `<output>` is the path to where the decompressed file should be created. Again, it can not point to an existing file, otherwise the application should print an error.

In no way the user should specify the algorithm to decompress the file. The compressed file should contain enough information so the application knows which algorithm should be used to decompress it.

### Other subcommands

The application should include other subcommands, such as a `help` subcommand so the user can obtain information about how to use the program. It should also provide a subcommand to list the available compression algorithm implementations. The form of these additional subommands is left unspecified.

## Your tasks

Attached to this document you will find the project containing the code written by the Pied Piper engineers.

Their goal was to create a prototype matching the specifications described above. The application (code name `Squeezer`) has been completely developed in Java using Maven as the build system. However, the code is not documented and it is known to contain bugs.

This application creates compressed files with the following structure:

```
┌─────┐┌──────┐┌───────────────────────┐
│ SQZ || <ID> || <Algorithm Payload>   |
└─────┘└──────┘└───────────────────────┘
```

`SQZ` are the three bytes of the ASCII characters `S`, `Q` and `Z`. This prefix is used as a [magic number](https://en.wikipedia.org/wiki/Magic_number_(programming)). `<ID>` is a byte that identifies the algorithm used to compress the file. `<Algorithm Payload>` depends on each compression algorithm.

To achieve this assignment you are required to complete the following tasks (no precisely in this order):

- Fork the project.

- Create a simple CI/CD infrastructure:
  - Using any CI provider (Github Actions, Gitlab CI, Jenkins, Travis, etc) make the project run the build process after every push or pull request made to the repository.
  - The integration should notify somehow if the build failed and what was the cause.

- Improve the quality of the code. For that you should:
  - Include a static analysis tool in the build process of the project.
  - The integration of the tool should allow developers to run it locally and in the CI server after every push or pull request.
  - Make the build fail if the number of issues detected by the static analysis tool is greater than a configured threshold.
  - You should regularly check the code smells detected by the static analysis tool and fix them if needed.
  - Issues detected by the static analysis tool you think should not be solved must be documented.

- Create a full test suite for the project:
  - Create test cases at all levels: unit, integration, system.
  - Design acceptance test cases for the final product.
  - The test suite must be able to reach, at least, a 70% statement coverage.
  - Put in practice the coverage criteria studied in the course and document how they were used to design new test cases.
  - Incorporate a coverage tool to monitor the statement and branch coverage.
  - Incorporate a mutation testing tool to improve the quality of the test suite. Document mutants that were helpful for the creation of new test cases and the mutants that were useless for that purpose.
  - The test suite must be executed as part of the build process after every push to the repository and after every pull request.
  - Make the build fail if the statement coverage falls below a configured threshold.

- Create an implement a fuzzing strategy to detect bugs:
  - Implement a fuzzing variant that helps finding bugs in the compression algorithms.
  - Configure the CI so that in regular scheduled intervals the algorithms are fuzzed for a given amount of time or until a bug is found.
  - Any bug found should be automatically reported to the developers with a new issue containing all the information.

As said before, these tasks can be completed in any order.

Before the deadline you must deliver a repository with the code. The repository must have a `doc` folder containing a report. The report must be written in Github-compatible Markdown. It should include a description of how you completed each task. It should describe the issues found with the help of the static analysis tool, whether each issue is worth solving and how were they solved. In a similar way, the report should describe which criteria were used to create the test suite. It should also describe the test improvements achieved with the help of the mutation testing tool and it should document those mutants that were not useful for testing. Finally it should also describe the fuzzing implementation and the bugs discovered with it.

Any other extra testing action will be considered as a bonus for the final grade.


Happy testing!