codn
====

clojure source code as EDN data

notes:
- this is WIP in both design and implementation
- the current implementation is franksteined off of clojure.tools.reader (Thanks!) and neither elegant or consistent


## Rationale

Clojure's built-in readers both parse source code, and interpret reader macros such as ` (syntax-quote). 

This means the result of read is a) a derivative of the source code, rather than a literal representation, and b) tied to the environment in which the read is performed.

These two characteristics of clojure's readers make them unsuitable for tasks such as source code analysis, and dynamic code distribution in distributed systems. 

Codn (COde as eDN) transliterates clojure source code into a strict EDN subset, allowing the source code to be treated as pure data.

### Objectives

The objectives of codn are:

- represent clojure source code as pure EDN data
- provide a basis for source code analysis
- provide a basis for multipass analysis and compilation
- provide a safe, analyzable basis for code exchange in distributed systems
- cleanly interoperate with term rewriting / tree transformation libraries

It is a non-objective of codn to provide isomorphic representation of textual documents containing source code. While codn is potentially useful for IDEs and editors, the goal is not to represent the document containing the program, but the program itself.

## Usage

Codn provides facilities for parsing source code into the codn format, and for reading codn data into their corresponding runtime structures.

### Parsing

load parser namespace:

    (require '[codn.parser.core :as p]

Primitives return a form {:head primitive-type-keyword :value primitive-value}

    (p/parse-string "a")
    => {:head :symbol, :value a}

Compound structures return a form {:head compound-type-keyword :body [components..]}

    (p/parse-string "[a]")
    => {:head :vector, :body [{:head :symbol, :value a}]}

    (p/parse-string "(deref a)")
    => {:head :list, :body [{:head :symbol, :value deref} {:head :symbol, :value a}]}

Reader macros and other non-EDN syntaxes are parsed into compound structures, differentiated by their :head :

    (p/parse-string "`a")
    => {:head :syntax-quote, :body [{:head :symbol, :value a}]}

    (p/parse-string "@a")
    => {:head :deref, :body [{:head :symbol, :value a}]}

    (p/parse-string "#foo.bar[1]")
    => {:head :constructor, :body [{:head :symbol, :value foo.bar} {:head :vector, :body [{:head :integer, :value 1}]}]}

### Reading

load reader namespace:

    (require '[codn.reader.core :as r]

    (r/read-codn '{:head :vector, :body [{:head :symbol, :value a}]})
    => [a]

















