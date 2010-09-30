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

import com.dmitriid.blueredis.RedisGraph;
import org.jredis.JRedis;
import org.jredis.RedisException;

public class RedisElementIterable {

    protected RedisElementType type;
    protected RedisGraph graph;
    private JRedis db;
    protected long count = 0;

    public RedisElementIterable(RedisElementType type, RedisGraph graph) {
        this.type  = type;
        this.graph = graph;
        this.db    = graph.getDatabase();

        String elementCollectionKey = "globals:";
        elementCollectionKey += type.equals(RedisElementType.REDIS_ELEMENT_VERTEX) ? "vertices" : "edges";

        try {
            count = db.zcard(elementCollectionKey);
        } catch(RedisException e) {
            e.printStackTrace();
        }
    }

    public long count(){
        return count;
    }
}
