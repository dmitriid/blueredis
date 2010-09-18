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

import biz.source_code.base64Coder.Base64Coder;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import org.jredis.RedisException;

import java.io.*;
import java.util.HashSet;
import java.util.List;
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
            byte[] o = (byte[])db.getDatabase().hget(getIdentifier("properties"), s);

            if(o != null){
                if(db.serializeProperties()){
                    return getObject(new String(o));
                } else {
                    return new String(o);
                }
            }
        } catch(RedisException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<String> getPropertyKeys() {
        try {
            List<String> l = db.getDatabase().hkeys(getIdentifier("properties"));
            if(l != null){
                return new HashSet<String>(l);
            }
        } catch(RedisException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setProperty(String s, Object o) {
        try {
            String val;
            if(db.serializeProperties()){
                val = writeObject(o);
            } else {
                val = String.valueOf(o);
            }
            if(db.doIndexing()){
                db.getIndex().remove(s, this.getProperty(s), this);
                db.getIndex().put(s, o, this);
            }
            db.getDatabase().hset(getIdentifier("properties"), s, val);
        } catch(RedisException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object removeProperty(String s) {
        try {
            Object property = getProperty(s);
            if(db.doIndexing()) {
                db.getDatabase().hdel(getIdentifier("properties"), s);
                db.getIndex().remove(s, property, this);
            }
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
        if(db.doIndexing()) {
            Index index = db.getIndex();
            Set<String> props = this.getPropertyKeys();
            for(String p : props){
                Object prop = this.getProperty(p);

                index.remove(p, prop, this);
            }
        }

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
                db.getDatabase().del("vertex:" + String.valueOf(id));
                db.getDatabase().zrem("globals:vertices", String.valueOf(id));

            } catch(RedisException e) {
                e.printStackTrace();
            }
        } else {
            RedisEdge edge = (RedisEdge) this;

            RedisVertex in = (RedisVertex) edge.getInVertex();
            RedisVertex out = (RedisVertex) edge.getOutVertex();

            try {
                db.getDatabase().del(getIdentifier("in"));
                db.getDatabase().del(getIdentifier("out"));
                db.getDatabase().del(getIdentifier("label"));
                db.getDatabase().del(getIdentifier("properties"));
                db.getDatabase().del("edge:" + String.valueOf(id));

                db.getDatabase().srem(out.getIdentifier("edges:out"), String.valueOf(getId()));
                db.getDatabase().srem(in.getIdentifier("edges:in"), String.valueOf(getId()));

                db.getDatabase().zrem("globals:edges", String.valueOf(id));
            } catch(RedisException e) {
                e.printStackTrace();
            }

        }
    }

    protected String getIdentifier(String suffix) {
        String prefix = this instanceof RedisVertex ? "vertex:" : "edge:";
        String identifier = prefix.concat(String.valueOf(id));
        if(suffix != null) identifier = identifier.concat(":").concat(suffix);

        return identifier;

    }
    
    public int hashCode() {
        return this.getId().hashCode();
    }

    public boolean equals(Object object) {
        return (this.getClass().equals(object.getClass()) && this.getId().equals(((Element) object).getId()));
    }

    private String writeObject(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(o);
        oos.close();

        return new String(Base64Coder.encode(baos.toByteArray()));
    }

    private Object getObject(String s) throws IOException, ClassNotFoundException {
        byte[] data = Base64Coder.decode(s);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

}
