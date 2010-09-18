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


/*
 * Based on http://playnice.ly/blog/2010/05/05/a-fast-fuzzy-full-text-index-using-redis/
 */


package com.dmitriid.blueredis;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.jredis.JRedis;
import org.jredis.RedisException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedisIndex implements Index {

    private RedisGraph graph;
    private JRedis database;

    public Set<String> indexKeys;
    boolean indexAll = true;

    private DoubleMetaphone metaphone = new DoubleMetaphone();
    private Pattern word_match = Pattern.compile("(\\w+)");
    private Matcher matcher = null;

    public RedisIndex(RedisGraph graph) {
        this.graph = graph;
        this.database = graph.getDatabase();
        this.indexKeys = new HashSet<String>();
    }

    @Override
    public void put(final String key, final Object value, final Element element) {
        if(!this.indexAll && !this.indexKeys.contains(key)) {
            return;
        }

        indexKeys.add(key);

        //matcher = word_match.matcher(String.valueOf(value));
        //matcher.reset();

        //while(matcher.find()) {
            String val = getMetaphone(value);//matcher.group()
            String node_name = "key:".concat(key).concat(":").concat(val).concat(element instanceof RedisVertex ? ":vertices" : ":edges");

            try {
                database.sadd(node_name, element.getId().toString());
            } catch(RedisException e) {
                e.printStackTrace();
            }
        //}
    }

    @Override
    public Iterable<Element> get(String key, Object value) {
        if(!indexKeys.contains(key)){
            return null;
        }
        ArrayList<Element> arr = new ArrayList<Element>();

        //matcher = word_match.matcher(String.valueOf(value));
        //matcher.reset();

        //while(matcher.find()) {
            String val = getMetaphone(value);//matcher.group()
            String node_name;
            List<byte[]> l;

            try {
                node_name = "key:".concat(key).concat(":").concat(val).concat(":vertices");
                l = database.smembers(node_name);
                if(l != null) {
                    for(byte[] o : l) {
                        arr.add(new RedisVertex(Long.parseLong(new String(o)), graph));
                    }
                }
                node_name = "key:".concat(key).concat(":").concat(val).concat(":edges");
                l = database.smembers(node_name);
                if(l != null) {
                    for(byte[] o : l) {
                        arr.add(new RedisEdge(Long.parseLong(new String(o)), graph));
                    }
                }
            } catch(RedisException e) {
                e.printStackTrace();
            }
        //}

        return arr.size() != 0 ? arr : null;
    }

    @Override
    public void remove(String key, Object value, Element element) {
        if(value == null) return;

        //matcher = word_match.matcher(String.valueOf(value));
        //matcher.reset();

        //while(matcher.find()) {
            String val = getMetaphone(value); //matcher.group()
            String node_name = "key:".concat(key).concat(":").concat(val).concat(element instanceof RedisVertex ? ":vertices" : ":edges");;

            try {
                database.srem(node_name, element.getId().toString());
            } catch(RedisException e) {
                e.printStackTrace();
            }
        //}
    }

    @Override
    public void indexAll(boolean b) {
        this.indexAll = b;
    }

    @Override
    public void addIndexKey(String s) {
        indexKeys.add(s);
    }

    @Override
    public void removeIndexKey(String s) {
        indexKeys.remove(s);
    }

    private String getMetaphone(Object o){
        String val = "";
        try{
            val = (String) metaphone.encode(String.valueOf(o));
        } catch(Exception ignored) {
        }
        
        if(val.equals("")) val = o.toString();

        return val;
    }

}
