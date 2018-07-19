# nanoweave

[![Build Status](https://travis-ci.org/NoxHarmonium/nanoweave.svg?branch=master)](https://travis-ci.org/NoxHarmonium/nanoweave)

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

- [X] JSON Support
- [ ] XML Support
- [ ] CSV Support

### Language Features

- [X] Structural Transforms
- [ ] String Interpolation
- [ ] Functions (Lambdas)
- [ ] Variables
- [ ] Logic Operators (or, and, xor)
- [ ] Math Operators (+, -, /, *, %, ...)
- [ ] Functional Operators (map, filter, reduce, sum, ...)
- [ ] Scopes
- [ ] Java Interop
- [ ] Array Access
- [ ] Ranges
- [ ] Flow Control/Pattern Matching
- [ ] Concatenation
- [ ] Type Checking (is?)
- [ ] Type Coercion
- [ ] String Manipulation

### Tools
- [X] Command Line Interface
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

    lein run -i test\resources\test-fixtures\simple-structure-transform\input.json -o output.json -j test\resources\test-fixtures\simple-structure-transform\transform.nweave transform

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