This is a straightforward and rather naïve implementation of a [Blueprints](http://blueprints.tinkerpop.com/)-enabled graph over [Redis](http://code.google.com/p/redis/).

Blueprints is a database-agnostic library for handling graphs. Blueredis allows you to run Blueprints on top of redis. Also see [Pipes](http://pipes.tinkerpop.com/) and [Gremlin](http://gremlin.tinkerpop.com/) to see what this integration will also allow you to do.

Running
===

    Graph db = new RedisGraph();
    
    String password = "pass";
    Graph db1 = new RedisGraph(password);

    String host = "127.0.0.1";
    int port = 6379;
    Graph db2 = new RedisGraph(host, port);

    int database = 10;
    Graph db3 = new RedisGraph(host, port, pass, database);

After this you work with the database as with any Blueprints-enabled graph.

Implementation
===

Vertices
---

Each vertex is represented by the following keys:

* `vertex:ID:properties`, a hash of all vertex properties
* `vertex:ID:edges:in`, a set of all incoming edges
* `vertex:ID:edges:out`, a set of all outgoing edges

Edges
---

Each edge is represented by the following keys:

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

Naïveté
===

In order to implement `getVertices()` and `getEdges` RedisGraph stores an ordered set of vertices and edges in `global:edges` and `global:vertices`. Because of this calling `getVertices()` or `getEdges` on a large set of either of these may be quite slow. This is the reason this implementation is both straightforward an naïve :)

