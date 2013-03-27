# peruse

Peruse is a RSS web aggregator.
It collects feed data in the background and shows updated feeds as they come in.



## Usage

Peruse runs two different scripts:
- The main web application, peruse.web.
- The peruse fetcher, peruse.worker, that fetches updated feeds.

To run the web application:

    lein run
    
To run the worker:

    lein run -m peruse.worker

## License

Copyright © 2013 Frederik De Bleser

Distributed under the Eclipse Public License, the same as Clojure.
