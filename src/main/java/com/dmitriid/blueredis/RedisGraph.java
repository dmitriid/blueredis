/*
 * Copyright (c) 2010. Dmitrii Dimandt <dmitrii@dmitriid.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dmitriid.blueredis;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.ri.alphazero.JRedisClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedisGraph implements Graph {

    private JRedis database = null;
    private boolean serializeProps = false; // if true, save type info with prop values

    public RedisGraph() {
        database = new JRedisClient();
    }

    public RedisGraph(String password) {
        database = new JRedisClient(password);
    }

    public RedisGraph(String host, int port) {
        database = new JRedisClient(host, port);
    }

    public RedisGraph(String host, int port, String password, int database) {
        this.database = new JRedisClient(host, port, password, database);
    }

    public void serializeProperties(boolean serialize) {
        serializeProps = serialize;
    }

    public boolean serializeProperties() {
        return serializeProps;
    }

    public long nextVertexId(){
        try {
            return database.incr("globals:next_vertex_id");
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long nextEdgeId(){
        try {
            return database.incr("globals:next_edge_id");
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public JRedis getDatabase(){
        return database;
    }

    @Override
    public Vertex addVertex(Object o) {
        final Vertex vertex = new RedisVertex(this);
        return vertex;
    }

    @Override
    public Vertex getVertex(Object o) {
        try{
            Long id = getLong(o);

            Object v = database.get("vertex:".concat(String.valueOf(id)));

            if(v != null){
                final Vertex vertex = new RedisVertex(id, this);
                return vertex;
            }

            return null;
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public void removeVertex(Vertex vertex) {
        ((RedisElement)vertex).remove();
    }

    @Override
    public Iterable<Vertex> getVertices() {
        List<byte[]> db_vertices = null;

        try {
            long count = database.zcard("globals:vertices");
            db_vertices = database.zrange("globals:vertices", 0, count);
        } catch(RedisException e) {
            e.printStackTrace();
        }
        if(db_vertices == null) return null;

        ArrayList<Vertex> arr = new ArrayList<Vertex>();
        for(byte[] b : db_vertices) {
            arr.add(new RedisVertex(Long.parseLong(new String(b)), this));
        }

        return arr;
    }

    @Override
    public Edge addEdge(Object o, Vertex outVertex, Vertex inVertex, String s) {
        final Edge edge = new RedisEdge((RedisVertex)inVertex, (RedisVertex)outVertex, s, this);
        return edge;
    }

    @Override
    public Edge getEdge(Object o) {
        try{
            Long id = getLong(o);

            Object e = database.get("edge:".concat(String.valueOf(id)));

            if(e != null) {
                final Edge edge = new RedisEdge(id, this);
                return edge;
            }
            return null;
        } catch(Exception e){
            return null;
        }
    }

    @Override
    public void removeEdge(Edge edge) {
        ((RedisEdge) edge).remove();
    }

    @Override
    public Iterable<Edge> getEdges() {
        List<byte[]> db_edges = null;

        try {
            long count = database.zcard("globals:edges");
            db_edges= database.zrange("globals:edges", 0, count);
        } catch(RedisException e) {
            e.printStackTrace();
        }
        if(db_edges == null) return null;

        ArrayList<Edge> arr = new ArrayList<Edge>();
        for(byte[] b : db_edges) {
            arr.add(new RedisEdge(Long.parseLong(new String(b)), this));
        }

        return arr;
    }

    @Override
    public void clear() {
        try {
            database.flushdb();
        } catch(RedisException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Index getIndex() {
        return null;
    }

    @Override
    public void shutdown() {
        database.quit();
    }

    public static String getIdentifier(String prefix, Long id, String suffix) {
        String identifier = prefix.concat(String.valueOf(id));
        if(suffix != null) identifier.concat(":").concat(suffix);

        return identifier;
    }

    // see http://stackoverflow.com/questions/1302605/how-do-i-convert-from-int-to-long-in-java/2904999#2904999

    private final Long getLong(Object obj) throws IllegalArgumentException {
        Long rv;

        if((obj.getClass() == Integer.class) || (obj.getClass() == Long.class) || (obj.getClass() == Double.class)) {
            rv = Long.parseLong(obj.toString());
        } else if((obj.getClass() == int.class) || (obj.getClass() == long.class) || (obj.getClass() == double.class)) {
            rv = (Long) obj;
        } else if(obj.getClass() == String.class) {
            rv = Long.parseLong(obj.toString());
        } else {
            throw new IllegalArgumentException("getLong: type " + obj.getClass() + " = \"" + obj.toString() + "\" unaccounted for");
        }

        return rv;
    }

    public String toString() {
        try {
            Map<String, String> info = this.database.info();
            return "redis[" + info.get("redis_version") + "]";
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return "redis[error retrieving info]";
    }

}
