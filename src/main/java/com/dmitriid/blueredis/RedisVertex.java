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
import com.tinkerpop.blueprints.pgm.Vertex;
import org.jredis.RedisException;

import java.util.*;

public class RedisVertex extends RedisElement implements Vertex {

    public RedisVertex(RedisGraph db) {
        super(db, db.nextVertexId());

        try {
            this.db.getDatabase().zadd("globals:vertices", id, String.valueOf(id));            
            this.db.getDatabase().set("vertex:" + String.valueOf(id), String.valueOf(id));
        } catch(RedisException e) {
            e.printStackTrace();
        }

    }

    public RedisVertex(Long id, RedisGraph db) {
        super(db, id);
    }

    @Override
    public Iterable<Edge> getOutEdges() {
        List<byte[]> db_edges = null;
        try {
            db_edges = db.getDatabase().smembers(getIdentifier("edges:out"));
        } catch(RedisException e) {
            e.printStackTrace();
        }
        if(db_edges == null) return null;

        ArrayList<Edge> arr = new ArrayList<Edge>();
        for(byte[] b : db_edges){
            arr.add(new RedisEdge(Long.parseLong(new String(b)), db));
        }

        return arr;
    }

    @Override
    public Iterable<Edge> getInEdges() {
        List<byte[]> db_edges = null;
        try {
            db_edges = db.getDatabase().smembers(getIdentifier("edges:in"));
        } catch(RedisException e) {
            e.printStackTrace();
        }
        if(db_edges == null) return null;

        ArrayList<Edge> arr = new ArrayList<Edge>();
        for(byte[] b : db_edges) {
            arr.add(new RedisEdge(Long.parseLong(new String(b)), db));
        }

        return arr;
    }

}
