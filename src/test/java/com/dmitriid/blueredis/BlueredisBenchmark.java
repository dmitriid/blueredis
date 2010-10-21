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

import com.tinkerpop.blueprints.pgm.ModelTestSuite;
import com.tinkerpop.blueprints.pgm.Vertex;
import junit.framework.TestResult;

public class BlueredisBenchmark extends ModelTestSuite {
    RedisGraph graph;

    public BlueredisBenchmark() {
    }

    public BlueredisBenchmark(RedisGraph graph) {
        this.graph = graph;
    }

    @Override
    public TestResult run() {

        int[] calls = {1, 10, 100, 1000/*, 10000, 100000, 1000000*/};
        int[] runs = {1, 10, 100, 1000};

        double[][] results = new double[4][4];
        graph.setIndexing(true);
        graph.serializeProperties(true);

        int r = 0, c = 0;

        System.gc();
        System.gc();
        System.gc();

        Runtime r = Runtime.getRuntime();

        r.gc();
        
        for(int noOfRuns : runs) {
            c = 0;
            for(int noOfCalls : calls) {
                if(noOfCalls * noOfRuns > 1000000) continue;
                double milliseconds = 0;
                for(int i = 0; i < noOfRuns; i++) {
                    graph.clear();
                    graph.addVertex(null);                    
                    this.stopWatch();
                    Vertex v;
                    for(int j = 0; j < noOfCalls; j++) {
                        v = graph.addVertex(null);
                        v.setProperty("name", noOfRuns + noOfCalls);
                    }
                    milliseconds += this.stopWatch();
                }
                //double milliseconds = this.stopWatch();
                double seconds = milliseconds / (double) 1000;
                double callsPerSecond = noOfCalls * noOfRuns / seconds;
                System.out.println(noOfRuns + " runs of " + noOfCalls + " calls averaging " + seconds / (double) noOfRuns + " s per run and " + seconds / (double) (noOfRuns * noOfCalls) + " s per call");
                System.out.println(callsPerSecond + " calls per second\n");
                System.out.println("Test took " + seconds + " seconds\n");

                results[r][c] = callsPerSecond;
                c++;
            }
            r++;
        }

        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                System.out.println(Math.round(results[i][j]) + "\t");                         
            }
            System.out.println("\n");
        }

        return null;
    }

    public static void printPerformance(String name, Integer events, String eventName, double timeInMilliseconds) {
        System.out.println("\t" + name + ": " + events + " " + eventName + " in " + timeInMilliseconds / 1000.0 + "s " + ": " + events / (double) (timeInMilliseconds / 1000) + " reqs per second");
    }

}
