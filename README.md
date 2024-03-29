# nanoweave

[![CircleCI](https://circleci.com/gh/NoxHarmonium/nanoweave.svg?style=svg)](https://circleci.com/gh/NoxHarmonium/nanoweave)

A JSONesqe language for transforming
the structure and values of documents.

The intended use of this project
is to create a way to
connect different API services,
that use different data formats,
in an easy declarative way.

The ultimate goal of this project
would be to create a simple system service
with nothing but an
OpenAPI or RAML API spec
and a nanoweave file to describe
the transformations between services.

This is an ambitious goal
at the moment is more of a project
to learn Clojure and AST parsing.

## Todo List

As you can see here,
there are lot of features
I would like to implement.

### Input/Output Formats

- [x] JSON Support
- [ ] XML Support
- [ ] CSV Support

### Language Features

- [x] Structural Transforms
- [x] String Interpolation
- [x] Functions (Lambdas)
- [x] Variables
- [x] Logic Operators (or, and, xor)
- [x] Math Operators (+, -, /, \*, %, ...)
- [x] Basic Functional Operators (map, filter, reduce)
- [x] Scopes
- [x] Java Interop
- [x] Array Access
- [x] Ranges
- [x] Flow Control/Pattern Matching
- [x] Concatenation
- [x] Regex
- [x] Type Checking (is?)
- [x] Type Coercion
- [ ] String Manipulation
- [x] Error handling (line number etc.)
- [ ] List comprehensions
- [ ] Set and Map Operators
- [ ] Aggregrate (.\*)

### Tools

- [x] Command Line Interface
- [ ] API Service Wrapper
- [ ] Graphical UI For Editing nanoweave files
- [ ] IDE Autocomplete from Schema

## Usage

    Performs actions on an input file according to a given nanoweave definition file .

    Usage: nanoweave [options] transform

    Options:
      -i, --input PATH   Path to input file
      -o, --output PATH  Path to output file
      -n, --nweave PATH  Path to nanoweave definition file
      -v                 Verbosity level; may be specified multiple times to increase value
      -h, --help

    Actions:
      transform     Transforms the given input file

## Examples

You can run nanoweave using `lein`.

    lein run -i test/resources/test-fixtures/simple-structure-transform/input.json -o output.json -j test/resources/test-fixtures/simple-structure-transform/transform.nweave transform

You can print out the AST tree by using the `dump-ast` command with the same options.
For example to save the the AST tree of the 'map-collection' test fixture to a file called 'ast.png' you would run the following command:

    lein run -i test/resources/test-fixtures/map-collection/input.json -n test/resources/test-fixtures/map-collection/transform.nweave -o ast.png dump-ast

It should output an image file like the following:

![AST Image Example](doc/ast.png)

## REPL

Start the REPL with `lein repl`

This will drop you into the namespace `repl-env` which has
a function called `refresh` in scope that allows you to
safely reload the source files if they change.

```
(require '[clojure.tools.namespace.repl :refer [refresh]])
(reload)
```

See also:

https://stackoverflow.com/a/25979645/1153203

## License

Copyright 2018 Sean Dawson

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
