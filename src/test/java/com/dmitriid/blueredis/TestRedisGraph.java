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


import com.tinkerpop.blueprints.pgm.EdgeTestSuite;
import com.tinkerpop.blueprints.pgm.GraphTestSuite;
import com.tinkerpop.blueprints.pgm.SuiteConfiguration;
import com.tinkerpop.blueprints.pgm.VertexTestSuite;
//import com.tinkerpop.blueprints.pgm.parser.GraphMLReaderTestSuite;
import junit.framework.TestCase;

import java.lang.reflect.Method;


public class TestRedisGraph extends TestCase {
    private static final SuiteConfiguration config = new SuiteConfiguration();

    static {
        config.allowsDuplicateEdges = true;
        config.allowsSelfLoops = true;
        config.requiresRDFIds = false;
        config.isRDFModel = false;
        config.supportsVertexIteration = true;
        config.supportsEdgeIteration = true;
        config.supportsVertexIndex = false;
        config.supportsEdgeIndex = false;
        config.ignoresSuppliedIds = true;
    }

    public void testVertexSuite() throws Exception {
        doSuiteTest(new VertexTestSuite(config));
    }

    public void testEdgeSuite() throws Exception {
        doSuiteTest(new EdgeTestSuite(config));
    }

    public void testGraphSuite() throws Exception {
        doSuiteTest(new GraphTestSuite(config));
    }

    public void testIndexSuite() throws Exception {
        doSuiteTest(new com.tinkerpop.blueprints.pgm.IndexTestSuite(config));
    }
          /*
    public void testGraphMLReaderSuite() throws Exception {
        doSuiteTest(new GraphMLReaderTestSuite(config));
    }   */


    private void doSuiteTest(final com.tinkerpop.blueprints.pgm.ModelTestSuite suite) throws Exception {
        String doTest = System.getProperty("testRedisGraph");
        if(doTest == null || doTest.equals("true")) {
            for(Method method : suite.getClass().getDeclaredMethods()) {
                if(method.getName().startsWith("test")) {
                    System.out.println("Testing " + method.getName() + "...");

                    RedisGraph graph = new RedisGraph();
                    graph.serializeProperties(true);
                    method.invoke(suite, graph);
                }
            }
        }
    }
}
