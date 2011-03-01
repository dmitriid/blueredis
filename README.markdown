This is a straightforward and rather naïve implementation of a [Blueprints](http://blueprints.tinkerpop.com/)-enabled
graph over [Redis](http://code.google.com/p/redis/).

Blueprints is a database-agnostic library for handling graphs. Blueredis allows you to run Blueprints on top of redis.
Also see [Pipes](http://pipes.tinkerpop.com/) and [Gremlin](http://gremlin.tinkerpop.com/) to see what this integration
will also allow you to do.

Running
===

Requires Redis 2.x compatible version of [JRedis](http://github.com/alphazero/jredis) (git clone this repo, and you'll get a jar)


    Graph db = new RedisGraph();
    
    String password = "pass";
    Graph db1 = new RedisGraph(password);

    String host = "127.0.0.1";
    int port = 6379;
    Graph db2 = new RedisGraph(host, port);

    int database = 10;
    Graph db3 = new RedisGraph(host, port, pass, database);

After this you work with the database as with any Blueprints-enabled graph.

Peculiarities
===

ID creation
---

RedisGraph **handles id creation for you**. Any id parameter passed to addVertex/addEdge will be ignored. All ids are
of type long

Serializing properties (off by default)
---

**By default all properties are saved and retrieved as strings**, regardless of the type of object you pass to
`addVertex`. If you want to save actual objects/values, call `graph.serializeProperties(true)`. Note though, that it
uses Base64 encoding on top of Java serialization so it may take up a lot of space.

Transactions
---

Currently **transactions are not supported**

Indexing (off by default)
===

**WARNING!** Indexing support that's bundled with Blueredis is rather resource intensive, manipulates quite a few
key-value pair in redis and negatively affects performance. If speed is your primary concern, don't use the default
indexing implementation

Blueredis implements an index for both vertex and edge properties. This implementation is based on
[A fast, fuzzy, full-text index using Redis](http://playnice.ly/blog/2010/05/05/a-fast-fuzzy-full-text-index-using-redis/).

Turning indexing on
---

**By default indexing is off**. To turn it on, call `graph.setIndexing(true)`. To turn it back on, call `graph.setIndexing(false)`.

Implementing your own
---

You may wish to implement your own indexing service. To do this:

* implement Blueprints' `Index` and `AutomaticIndex` interfaces
* override Blueredis' `RedisIndexManager` class
* call `graph.setIndexing(true, yourManagerInstance)` and pass an instance of your indexManager to it

Implementation
===

Vertices
---

Each vertex is represented by the following keys:

* `vertex:ID`, vertex id. This one is used to test if a vertex exists
* `vertex:ID:properties`, a hash of all vertex properties
* `vertex:ID:edges:in`, a set of all incoming edges
* `vertex:ID:edges:out`, a set of all outgoing edges

Edges
---

Each edge is represented by the following keys:

* `edge:ID`, edge id. This one is used to test if an edge exists
* `edge:ID:label`, a string containing the edge's label
* `edge:ID:in`, index of "in" vertex
* `edge:ID:out`, index of "out" vertex
* `edge:ID:properties`, a hash of all edge properties

Globals
---

This are just some keys that hold values necessary for Blueredis to work:

* `globals:next_vertex_id`, a counter that's incremented each time a new vertex is added
* `globals:next_edge_id`, a counter that's incremented each time a new edge is added
* `globals:vertices`, a sorted list of all vertex ids
* `globals:edges`, a sorted list of all edge ids

Indices
---
**This section describes default indexing support that's bundled with Blueredis**. You're free to implement your own.

 Indices are stored in keys that all start with an `index:...`

* `index:auto` - prefix for all automatic indices
* `index:manual` - prefix for all manual indices
   For all of the above:
    * `index:type:index_name`, where `index_name` is a user-supplied index name - prefix for index data for `index_name`
    * `index:type:index_name:key_name`, where `key_name` is a user-supplied key name - prefix for index data for `key_name` key
    * `index:type:index_name:key_name:metaphone`, where `metaphone` is a calculated metaphone for a value - contains
       a  list of vertex ids that have a key key_name with a value whose metaphone matches metaphone
* `index:meta:indices:auto` - a list of automatic indices
* `index:meta:indices:manual` - a list of manual indices
* `index:meta:auto` - prefix for all meta information on automatic indices
* `index:meta:manual` - prefix for all meta information on manual indices
   For index:meta:(auto|manual) above:
    * `index:meta:type:index_name:class`, where `index_name` is a user-supplied index name - contains what class
      the index works on: vertex, edge, etc.
    * `index:meta:type:index_name:keys`, where `index_name` is a user-supplied index name - contains a list of keys
      for this index

Tests
===

RedisGraph now passes tinkerpop's tests.


Benchmarks
===

An old benchmark can be found here: [benchmarks page in wiki](http://github.com/dmitriid/blueredis/wiki). Note that reeds
seems to perform better under load than during singular requests. A newer benchmark will be published as soon as there's one :)