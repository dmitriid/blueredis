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
import com.tinkerpop.blueprints.pgm.Element;
import org.jredis.RedisException;

import java.util.HashSet;
import java.util.Set;

public class RedisElement implements Element {
    protected Long id = null;
    protected RedisGraph db = null;

    RedisElement(RedisGraph db, Long id) {
        this.db = db;
        this.id = id;
    }

    @Override
    public Object getProperty(String s) {
        try {
            return db.getDatabase().hget(getIdentifier("properties"), s);
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<String> getPropertyKeys() {
        try {
            return new HashSet<String>(db.getDatabase().hkeys(getIdentifier("properties")));
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setProperty(String s, Object o) {
        try {
            db.getDatabase().hset(getIdentifier("properties"), s, String.valueOf(o));
        } catch(RedisException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object removeProperty(String s) {
        try {
            Object property = getProperty(s);
            db.getDatabase().hdel(getIdentifier("properties"), s);
            return property;
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getId() {
        return id;
    }

    public void remove() {
        if(this instanceof RedisVertex) {
            RedisVertex vertex = (RedisVertex) this;

            Iterable<Edge> edges;

            edges = vertex.getInEdges();
            for(Edge edge : edges) {
                ((RedisEdge) edge).remove();
            }

            edges = vertex.getOutEdges();
            for(Edge edge : edges) {
                ((RedisEdge) edge).remove();
            }

            try {
                db.getDatabase().del(getIdentifier(null));
                db.getDatabase().del(getIdentifier("properties"));
                db.getDatabase().del(getIdentifier("edges:in"));
                db.getDatabase().del(getIdentifier("edges:out"));
                db.getDatabase().zrem("vertices", String.valueOf(id));
            } catch(RedisException e) {
                e.printStackTrace();
            }
        } else {
            RedisEdge edge = (RedisEdge) this;

            RedisVertex in = (RedisVertex) edge.getInVertex();
            RedisVertex out = (RedisVertex) edge.getInVertex();

            try {
                db.getDatabase().del(getIdentifier("in"));
                db.getDatabase().del(getIdentifier("out"));
                db.getDatabase().del(getIdentifier("label"));
                db.getDatabase().del(getIdentifier("properties"));
                db.getDatabase().zrem("edges", String.valueOf(id));

                db.getDatabase().srem(in.getIdentifier("edges:out"), String.valueOf(getId()));
                db.getDatabase().srem(out.getIdentifier("edges:in"), String.valueOf(getId()));
            } catch(RedisException e) {
                e.printStackTrace();
            }

        }
    }

    protected String getIdentifier(String suffix) {
        String prefix = this instanceof RedisVertex ? "vertex:" : "edge:";
        String identifier = prefix.concat(String.valueOf(id));
        if(suffix != null) identifier.concat(":").concat(suffix);

        return identifier;

    }
    
    public int hashCode() {
        return this.getId().hashCode();
    }

    public boolean equals(Object object) {
        return (this.getClass().equals(object.getClass()) && this.getId().equals(((Element) object).getId()));
    }

}
