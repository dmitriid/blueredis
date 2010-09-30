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

package com.dmitriid.blueredis.utils;

import com.dmitriid.blueredis.RedisEdge;
import com.dmitriid.blueredis.RedisGraph;
import com.dmitriid.blueredis.RedisVertex;
import com.tinkerpop.blueprints.pgm.Element;
import org.jredis.JRedis;
import org.jredis.RedisException;

import java.util.Iterator;
import java.util.List;

public class RedisElementIterator implements Iterator {

    protected RedisGraph graph;
    private JRedis db;
    protected RedisElementType type;
    protected long count = 0, current = 0;

    private String elementCollectionKey = "globals:";

    public RedisElementIterator(RedisElementType type, RedisGraph graph, long count) {
        this.type = type;
        this.graph = graph;
        this.db = graph.getDatabase();
        this.count = count;

        elementCollectionKey += type.equals(RedisElementType.REDIS_ELEMENT_VERTEX) ? "vertices" : "edges";
    }

    @Override
    public boolean hasNext() {
        return current < count;
    }

    @Override
    public Object next() {
        List<byte[]> db_vertices = null;
        Element el = null;

        try {
            db_vertices = db.zrange("globals:vertices", current, 1);

            long id = Long.parseLong(new String(db_vertices.get(0)));

            if(type.equals(RedisElementType.REDIS_ELEMENT_VERTEX)) el = new RedisVertex(id, graph);
            else el = new RedisEdge(id, graph);
        } catch(RedisException e) {
            e.printStackTrace();
        }

        current++;

        return el;
    }

    @Override
    public void remove() {
    }
}
