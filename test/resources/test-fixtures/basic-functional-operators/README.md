# Basic Functional Operators

Basic sanity check for
the basic building blocks
of functional programming:

map, filter, reduce

Result 5 and 6 test operator precidence.
There was an issue with the math operators
having a higher precidence than the functional
operators and causing a crash at runtime.

The equivilant javascript 
which I used to check the value
of result 5 is:

    [[[1, 2], [3, 4]], [[5, 6], [7, 8]]]
        .map((a) => a.map((a) => a.map((a) => a + 1)))
        .reduce((a, b) => 
            a.reduce((a, b) => a.reduce((a, b) => a + b) + b.reduce((a, b) => a + b)) +
            b.reduce((a, b) => a.reduce((a, b) => a + b) + b.reduce((a, b) => a + b)))