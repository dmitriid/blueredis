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

package com.tinkerpop.blueprints;

import junit.framework.TestCase;

import java.util.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class BaseTest extends TestCase {

    double timer = -1.0d;

    public void testTrue() {
        assertTrue(true);
    }

    public static List<String> generateUUIDs(int number) {
        List<String> uuids = new ArrayList<String>();
        for (int i = 0; i < number; i++) {
            uuids.add(UUID.randomUUID().toString());
        }
        return uuids;
    }

    public static List<String> generateUUIDs(String prefix, int number) {
        List<String> uuids = new ArrayList<String>();
        for (int i = 0; i < number; i++) {
            uuids.add(prefix + UUID.randomUUID().toString());
        }
        return uuids;
    }

    public static void printCollection(final Collection collection) {
        for (Object o : collection) {
            System.out.println(o);
        }
    }

    public static void printIterator(final Iterator itty) {
        while (itty.hasNext()) {
            System.out.println(itty.next());
        }
    }

    public static int count(final Iterator iterator) {
        int counter = 0;
        while (iterator.hasNext()) {
            iterator.next();
            counter++;
        }
        return counter;
    }

    public static int count(final Iterable iterable) {
        return count(iterable.iterator());
    }

    public static List asList(final Object x, final int times) {
        List list = new ArrayList();
        for (int i = 0; i < times; i++) {
            list.add(x);
        }
        return list;
    }

    public double stopWatch() {
        if (this.timer == -1.0d) {
            this.timer = System.nanoTime() / 1000000.0d;
            return -1.0d;
        } else {
            double temp = (System.nanoTime() / 1000000.0d) - this.timer;
            this.timer = -1.0d;
            return temp;
        }
    }

    public static void printPerformance(String name, Integer events, String eventName, double timeInMilliseconds) {
        if (null != events)
            System.out.println("\t" + name + ": " + events + " " + eventName + " in " + timeInMilliseconds + "ms");
        else
            System.out.println("\t" + name + ": " + eventName + " in " + timeInMilliseconds + "ms");
    }

    public static void warmUp(int amount) {
        List<String> uuids = new ArrayList<String>();
        for (int i = 0; i < amount; i++) {
            uuids.add(UUID.randomUUID().toString());
        }
        for (String uuid : uuids) {
            uuid.toUpperCase();
        }
    }


}
