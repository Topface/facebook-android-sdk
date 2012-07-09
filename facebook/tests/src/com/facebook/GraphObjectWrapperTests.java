/**
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.test.AndroidTestCase;

public final class GraphObjectWrapperTests extends AndroidTestCase {

    public void testCreateEmptyGraphObject() {
        GraphObject graphObject = GraphObjectWrapper.createGraphObject();
        assertTrue(graphObject != null);
    }

    public void testCanTreatAsMap() {
        GraphObject graphObject = GraphObjectWrapper.createGraphObject();

        graphObject.put("hello", "world");
        assertEquals("world", (String) graphObject.get("hello"));
    }

    public void testCanTreatAsGraphPlace() {
        GraphPlace graphPlace = GraphObjectWrapper.createGraphObject(GraphPlace.class);

        graphPlace.setName("hello");
        assertEquals("hello", graphPlace.getName());
    }

    public void testCanTreatAsGraphUser() {
        GraphUser graphUser = GraphObjectWrapper.createGraphObject(GraphUser.class);

        graphUser.setFirstName("Michael");
        assertEquals("Michael", graphUser.getFirstName());
        assertEquals("Michael", graphUser.get("first_name"));

        graphUser.put("last_name", "Scott");
        assertEquals("Scott", graphUser.get("last_name"));
        assertEquals("Scott", graphUser.getLastName());
    }

    public void testCanCastBetweenGraphObjectTypes() {
        GraphObject graphObject = GraphObjectWrapper.createGraphObject();

        graphObject.put("first_name", "Mickey");

        GraphUser graphUser = graphObject.cast(GraphUser.class);
        assertTrue(graphUser != null);
        // Should see the name we set earlier as a GraphObject.
        assertEquals("Mickey", graphUser.getFirstName());

        // Changes to GraphUser should be reflected in GraphObject version.
        graphUser.setLastName("Mouse");
        assertEquals("Mouse", graphObject.get("last_name"));
    }

    public void testCanSetComplexTypes() {
        GraphLocation graphLocation = GraphObjectWrapper.createGraphObject(GraphLocation.class);
        graphLocation.setCity("Seattle");

        GraphPlace graphPlace = GraphObjectWrapper.createGraphObject(GraphPlace.class);
        graphPlace.setLocation(graphLocation);

        assertEquals(graphLocation, graphPlace.getLocation());
        assertEquals("Seattle", graphPlace.getLocation().getCity());
    }

    public void testCanConvertFromJSON() throws JSONException {
        JSONObject jsonLocation = new JSONObject();
        jsonLocation.put("city", "Paris");
        jsonLocation.put("country", "France");

        JSONObject jsonPlace = new JSONObject();
        jsonPlace.put("location", jsonLocation);
        jsonPlace.put("name", "Eiffel Tower");

        GraphPlace graphPlace = GraphObjectWrapper.wrapJson(jsonPlace, GraphPlace.class);
        GraphLocation graphLocation = graphPlace.getLocation();
        assertEquals("Paris", graphLocation.getCity());
    }

    public void testCanConvertFromGraphObject() throws JSONException {
        GraphObject graphObject = GraphObjectWrapper.createGraphObject();
        graphObject.put("city", "Paris");
        graphObject.put("country", "France");

        JSONObject jsonPlace = new JSONObject();
        jsonPlace.put("location", graphObject);
        jsonPlace.put("name", "Eiffel Tower");

        GraphPlace graphPlace = GraphObjectWrapper.wrapJson(jsonPlace, GraphPlace.class);
        GraphLocation graphLocation = graphPlace.getLocation();
        assertEquals("Paris", graphLocation.getCity());
    }

    private abstract class GraphObjectClass implements GraphObject {
    }

    public void testCantWrapNonInterface() {
        try {
            GraphObjectWrapper.createGraphObject(GraphObjectClass.class);
            fail("Expected exception");
        } catch (FacebookGraphObjectException exception) {
        }
    }

    private interface BadMethodNameGraphObject extends GraphObject {
        void floppityFlee();
    }

    public void testCantWrapBadMethodName() {
        try {
            GraphObjectWrapper.createGraphObject(BadMethodNameGraphObject.class);
            fail("Expected exception");
        } catch (FacebookGraphObjectException exception) {
        }
    }

    private interface BadGetterNameGraphObject extends GraphObject {
        void get();
    }

    public void testCantWrapBadGetterName() {
        try {
            GraphObjectWrapper.createGraphObject(BadGetterNameGraphObject.class);
            fail("Expected exception");
        } catch (FacebookGraphObjectException exception) {
        }
    }

    private interface BadGetterParamsGraphObject extends GraphObject {
        Object getFoo(Object obj);
    }

    public void testCantWrapBadGetterParams() {
        try {
            GraphObjectWrapper.createGraphObject(BadGetterParamsGraphObject.class);
            fail("Expected exception");
        } catch (FacebookGraphObjectException exception) {
        }
    }

    private interface BadGetterReturnTypeGraphObject extends GraphObject {
        void getFoo();
    }

    public void testCantWrapBadGetterReturnType() {
        try {
            GraphObjectWrapper.createGraphObject(BadGetterReturnTypeGraphObject.class);
            fail("Expected exception");
        } catch (FacebookGraphObjectException exception) {
        }
    }

    private interface BadSetterNameGraphObject extends GraphObject {
        void set();
    }

    public void testCantWrapBadSetterName() {
        try {
            GraphObjectWrapper.createGraphObject(BadSetterNameGraphObject.class);
            fail("Expected exception");
        } catch (FacebookGraphObjectException exception) {
        }
    }

    private interface BadSetterParamsGraphObject extends GraphObject {
        void setFoo();
    }

    public void testCantWrapBadSetterParams() {
        try {
            GraphObjectWrapper.createGraphObject(BadSetterParamsGraphObject.class);
            fail("Expected exception");
        } catch (FacebookGraphObjectException exception) {
        }
    }

    private interface BadSetterReturnTypeGraphObject extends GraphObject {
        Object setFoo(Object obj);
    }

    public void testCantWrapBadSetterReturnType() {
        try {
            GraphObjectWrapper.createGraphObject(BadSetterReturnTypeGraphObject.class);
            fail("Expected exception");
        } catch (FacebookGraphObjectException exception) {
        }
    }

    private interface BadBaseInterfaceGraphObject extends BadSetterReturnTypeGraphObject {
        void setBar(Object obj);

        Object getBar();
    }

    public void testCantWrapBadBaseInterface() {
        try {
            GraphObjectWrapper.createGraphObject(BadBaseInterfaceGraphObject.class);
            fail("Expected exception");
        } catch (FacebookGraphObjectException exception) {
        }
    }

    public void testObjectEquals() {
        GraphObject graphObject = GraphObjectWrapper.createGraphObject();
        graphObject.put("aKey", "aValue");

        assertTrue(graphObject.equals(graphObject));

        GraphPlace graphPlace = graphObject.cast(GraphPlace.class);
        assertTrue(graphObject.equals(graphPlace));
        assertTrue(graphPlace.equals(graphObject));

        GraphObject aDifferentGraphObject = GraphObjectWrapper.createGraphObject();
        aDifferentGraphObject.put("aKey", "aDifferentValue");
        assertFalse(graphObject.equals(aDifferentGraphObject));
    }

    public void testMapClear() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hello", "world");

        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);

        assertEquals(1, jsonObject.length());

        graphObject.clear();

        assertEquals(0, jsonObject.length());
    }

    public void testMapContainsKey() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hello", "world");

        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);

        assertTrue(graphObject.containsKey("hello"));
        assertFalse(graphObject.containsKey("hocus"));
    }

    public void testMapContainsValue() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hello", "world");

        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);

        assertTrue(graphObject.containsValue("world"));
        assertFalse(graphObject.containsValue("pocus"));
    }

    public void testMapEntrySet() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hello", "world");
        jsonObject.put("hocus", "pocus");

        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);

        Set<Entry<String, Object>> entrySet = graphObject.entrySet();
        assertEquals(2, entrySet.size());
    }

    public void testMapGet() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hello", "world");
        jsonObject.put("hocus", "pocus");

        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);
        assertEquals("world", graphObject.get("hello"));
        assertTrue(graphObject.get("fred") == null);
    }

    public void testMapIsEmpty() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);
        assertTrue(graphObject.isEmpty());

        jsonObject.put("hello", "world");
        jsonObject.put("hocus", "pocus");
        assertFalse(graphObject.isEmpty());
    }

    public void testMapKeySet() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hello", "world");
        jsonObject.put("hocus", "pocus");

        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);

        Set<String> keySet = graphObject.keySet();
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains("hello"));
        assertTrue(keySet.contains("hocus"));
        assertFalse(keySet.contains("world"));
    }

    public void testMapPut() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);
        graphObject.put("hello", "world");
        graphObject.put("hocus", "pocus");

        assertEquals("pocus", jsonObject.get("hocus"));
        assertEquals(2, jsonObject.length());
    }

    /*
     * TODO port: putting objects/collections should extract the underlying object and store it.
     * 
    public void testMapPutOfWrapperPutsJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);
        graphObject.put("hello", "world");
        graphObject.put("hocus", "pocus");

        GraphObject parentObject = GraphObjectWrapper.createGraphObject();
        parentObject.put("key", graphObject);
        
    }
    */
    
    public void testMapPutAll() throws JSONException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("hello", "world");
        map.put("hocus", "pocus");

        JSONObject jsonObject = new JSONObject();
        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);

        graphObject.putAll(map);
        assertEquals("pocus", jsonObject.get("hocus"));
        assertEquals(2, jsonObject.length());
    }

    public void testMapRemove() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hello", "world");
        jsonObject.put("hocus", "pocus");

        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);
        graphObject.remove("hello");

        assertEquals(1, jsonObject.length());
    }

    public void testMapSize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hello", "world");
        jsonObject.put("hocus", "pocus");

        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);

        assertEquals(2, graphObject.size());
    }

    public void testMapValues() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hello", "world");
        jsonObject.put("hocus", "pocus");

        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);

        Collection<Object> values = graphObject.values();

        assertEquals(2, values.size());
        assertTrue(values.contains("world"));
    }

    public void testGetInnerJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hello", "world");
        jsonObject.put("hocus", "pocus");

        GraphObject graphObject = GraphObjectWrapper.wrapJson(jsonObject);

        assertEquals(jsonObject, graphObject.getInnerJSONObject());
    }

    public void testSettingGraphObjectProxyStoresJSONObject() throws JSONException {
        GraphPlace graphPlace = GraphObjectWrapper.createGraphObject(GraphPlace.class);
        GraphLocation graphLocation = GraphObjectWrapper.createGraphObject(GraphLocation.class);

        graphPlace.setLocation(graphLocation);

        assertEquals(graphLocation.getInnerJSONObject(), graphPlace.getInnerJSONObject().get("location"));

    }

    public void testCollectionAdd() throws JSONException {
        JSONArray array = new JSONArray();

        Collection<Integer> collection = GraphObjectWrapper.wrapArray(array, Integer.class);
        collection.add(5);

        assertTrue(array.length() == 1);
        assertTrue(array.optInt(0) == 5);
    }

    public void testCollectionAddAll() throws JSONException {
        JSONArray array = new JSONArray();

        Collection<Integer> collectionToAdd = Arrays.asList(5, -1);

        Collection<Integer> collection = GraphObjectWrapper.wrapArray(array, Integer.class);
        collection.addAll(collectionToAdd);

        assertTrue(array.length() == 2);
        assertTrue(array.optInt(0) == 5);
        assertTrue(array.optInt(1) == -1);
    }

    public void testCollectionContains() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(5);

        Collection<Integer> collection = GraphObjectWrapper.wrapArray(array, Integer.class);
        assertTrue(collection.contains(5));
        assertFalse(collection.contains(6));
    }

    public void testCollectionContainsAll() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(5);
        array.put(-1);

        Collection<Integer> collection = GraphObjectWrapper.wrapArray(array, Integer.class);
        assertTrue(collection.containsAll(Arrays.asList(5)));
        assertTrue(collection.containsAll(Arrays.asList(5, -1)));
        assertFalse(collection.containsAll(Arrays.asList(5, -1, 2)));
    }

    public void testCollectionIsEmpty() throws JSONException {
        JSONArray array = new JSONArray();

        Collection<Integer> collection = GraphObjectWrapper.wrapArray(array, Integer.class);
        assertTrue(collection.isEmpty());

        array.put(5);
        assertFalse(collection.isEmpty());
    }

    public void testCollectionIterator() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(5);
        array.put(-1);

        Collection<Integer> collection = GraphObjectWrapper.wrapArray(array, Integer.class);
        Iterator<Integer> iter = collection.iterator();
        assertTrue(iter.hasNext());
        assertTrue(iter.next() == 5);
        assertTrue(iter.hasNext());
        assertTrue(iter.next() == -1);
        assertFalse(iter.hasNext());
        
        for (Integer i : collection) {
            assertNotSame(0, i);
        }
    }

    public void testCollectionIteratorOfGraphObject() throws JSONException {
        Collection<GraphLocation> collection = GraphObjectWrapper.createArray(GraphLocation.class);
        
        GraphLocation seattle = GraphObjectWrapper.createGraphObject(GraphLocation.class);
        seattle.setCity("Seattle");
        collection.add(seattle);
        GraphLocation paris = GraphObjectWrapper.createGraphObject(GraphLocation.class);
        paris.setCity("Paris");
        collection.add(paris);
        
        Iterator<GraphLocation> iter = collection.iterator();
        assertTrue(iter.hasNext());
        assertEquals(seattle, iter.next());
        assertTrue(iter.hasNext());
        assertEquals(paris, iter.next());
        assertFalse(iter.hasNext());
        
        for (GraphLocation location : collection) {
            assertTrue(location != null);
        }
    }

    public void testCollectionSize() throws JSONException {
        JSONArray array = new JSONArray();

        Collection<Integer> collection = GraphObjectWrapper.wrapArray(array, Integer.class);
        assertEquals(0, collection.size());

        array.put(5);
        assertEquals(1, collection.size());
    }

    public void testCollectionClearThrows() throws JSONException {
        try {
            Collection<Integer> collection = GraphObjectWrapper.createArray(Integer.class);
            collection.clear();
            fail("Expected exception");
        } catch (UnsupportedOperationException exception) {
        }
    }

    public void testCollectionRemoveThrows() throws JSONException {
        try {
            Collection<Integer> collection = GraphObjectWrapper.createArray(Integer.class);
            collection.remove(5);
            fail("Expected exception");
        } catch (UnsupportedOperationException exception) {
        }
    }

    public void testCollectionRemoveAllThrows() throws JSONException {
        try {
            Collection<Integer> collection = GraphObjectWrapper.createArray(Integer.class);
            collection.removeAll(Arrays.asList());
            fail("Expected exception");
        } catch (UnsupportedOperationException exception) {
        }
    }

    public void testCollectionRetainAllThrows() throws JSONException {
        try {
            Collection<Integer> collection = GraphObjectWrapper.createArray(Integer.class);
            collection.retainAll(Arrays.asList());
            fail("Expected exception");
        } catch (UnsupportedOperationException exception) {
        }
    }

    private interface Locations extends GraphObject {
        Collection<GraphLocation> getLocations();
    }

    
    public void testObjectWrapsJSONCollection() throws JSONException {
        JSONObject jsonLocation = new JSONObject();
        jsonLocation.put("city", "Seattle");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonLocation);

        JSONObject jsonLocations = new JSONObject();
        jsonLocations.put("locations", jsonArray);

        Locations locations = GraphObjectWrapper.wrapJson(jsonLocations, Locations.class);
        Collection<GraphLocation> locationsGraphObjectCollection = locations.getLocations();
        assertTrue(locationsGraphObjectCollection != null);

        GraphLocation graphLocation = locationsGraphObjectCollection.iterator().next();
        assertTrue(graphLocation != null);
        assertEquals("Seattle", graphLocation.getCity());
    }
     
    public void testCollectionWrapsJSONObject() throws JSONException {
        JSONObject jsonLocation = new JSONObject();
        jsonLocation.put("city", "Seattle");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonLocation);
        Collection<GraphLocation> locationsGraphObjectCollection = GraphObjectWrapper.wrapArray(jsonArray,
                GraphLocation.class);
        assertTrue(locationsGraphObjectCollection != null);

        GraphLocation graphLocation = locationsGraphObjectCollection.iterator().next();
        assertTrue(graphLocation != null);
        assertEquals("Seattle", graphLocation.getCity());
    }
    
    public void testCannotCastCollectionOfNonGraphObjects() throws JSONException {
        try {
            GraphObjectList<Integer> collection = GraphObjectWrapper.createArray(Integer.class);
            collection.castToListOf(GraphLocation.class);
            fail("Expected exception");
        } catch (FacebookGraphObjectException exception) {
        }
    }

    public void testCanCastCollectionOfGraphObjects() throws JSONException {
        JSONObject jsonSeattle = new JSONObject();
        jsonSeattle.put("city", "Seattle");
        
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonSeattle);
        
        GraphObjectList<GraphObject> collection = GraphObjectWrapper.wrapArray(jsonArray, GraphObject.class);
        
        GraphObjectList<GraphLocation> locationCollection = collection.castToListOf(GraphLocation.class);
        assertTrue(locationCollection != null);
        
        GraphLocation seattle = locationCollection.iterator().next();
        assertTrue(seattle != null);
        assertEquals("Seattle", seattle.getCity());
    }

    public void testCanGetInnerJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        
        GraphObjectList<GraphObject> collection = GraphObjectWrapper.wrapArray(jsonArray, GraphObject.class);
        
        assertEquals(jsonArray, collection.getInnerJSONArray());
    }
    
    public void canGetRandomAccess() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("Seattle");
        jsonArray.put("Menlo Park");
        
        GraphObjectList<String> collection = GraphObjectWrapper.wrapArray(jsonArray, String.class);
        
        assertEquals("Menlo Park", collection.get(1));
    }

    public void canSetRandomAccess() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        
        GraphObjectList<String> collection = GraphObjectWrapper.wrapArray(jsonArray, String.class);

        collection.add("Seattle");
        collection.add("Menlo Park");

        collection.set(1, "Ann Arbor");
        assertEquals("Ann Arbor", collection.get(1));
    }
// TODO write tests for putting collections
    
}