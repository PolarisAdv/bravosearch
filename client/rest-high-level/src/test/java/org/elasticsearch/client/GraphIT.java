/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticsearch.client;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.client.graph.GraphExploreRequest;
import org.elasticsearch.client.graph.GraphExploreResponse;
import org.elasticsearch.client.graph.Hop;
import org.elasticsearch.client.graph.Vertex;
import org.elasticsearch.client.graph.VertexRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.hamcrest.Matchers;
import org.junit.Before;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GraphIT extends ESRestHighLevelClientTestCase {

    @Before
    public void indexDocuments() throws IOException {
        // Create chain of doc IDs across indices 1->2->3
        Request doc1 = new Request(HttpPut.METHOD_NAME, "/index1/_doc/1");
        doc1.setJsonEntity("""
            { "num":[1], "const":"start"}""");
        client().performRequest(doc1);

        Request doc2 = new Request(HttpPut.METHOD_NAME, "/index2/_doc/1");
        doc2.setJsonEntity("""
            {"num":[1,2], "const":"foo"}""");
        client().performRequest(doc2);

        Request doc3 = new Request(HttpPut.METHOD_NAME, "/index2/_doc/2");
        doc3.setJsonEntity("""
            {"num":[2,3], "const":"foo"}""");
        client().performRequest(doc3);

        Request doc4 = new Request(HttpPut.METHOD_NAME, "/index_no_field_data/_doc/2");
        doc4.setJsonEntity("""
            {"num":"string", "const":"foo"}""");
        client().performRequest(doc4);

        Request doc5 = new Request(HttpPut.METHOD_NAME, "/index_no_field_data/_doc/2");
        doc5.setJsonEntity("""
            {"num":[2,4], "const":"foo"}""");
        client().performRequest(doc5);

        client().performRequest(new Request(HttpPost.METHOD_NAME, "/_refresh"));
    }

    public void testCleanExplore() throws Exception {
        GraphExploreRequest graphExploreRequest = new GraphExploreRequest();
        graphExploreRequest.indices("index1", "index2");
        graphExploreRequest.useSignificance(false);
        int numHops = 3;
        for (int i = 0; i < numHops; i++) {
            QueryBuilder guidingQuery = null;
            if (i == 0) {
                guidingQuery = new TermQueryBuilder("const.keyword", "start");
            } else if (randomBoolean()) {
                guidingQuery = new TermQueryBuilder("const.keyword", "foo");
            }
            Hop hop = graphExploreRequest.createNextHop(guidingQuery);
            VertexRequest vr = hop.addVertexRequest("num");
            vr.minDocCount(1);
        }
        Map<String, Integer> expectedTermsAndDepths = new HashMap<>();
        expectedTermsAndDepths.put("1", 0);
        expectedTermsAndDepths.put("2", 1);
        expectedTermsAndDepths.put("3", 2);

        GraphExploreResponse exploreResponse = highLevelClient().graph().explore(graphExploreRequest, RequestOptions.DEFAULT);
        Map<String, Integer> actualTermsAndDepths = new HashMap<>();
        Collection<Vertex> v = exploreResponse.getVertices();
        for (Vertex vertex : v) {
            actualTermsAndDepths.put(vertex.getTerm(), vertex.getHopDepth());
        }
        assertEquals(expectedTermsAndDepths, actualTermsAndDepths);
        assertThat(exploreResponse.isTimedOut(), Matchers.is(false));
        ShardOperationFailedException[] failures = exploreResponse.getShardFailures();
        assertThat(failures.length, Matchers.equalTo(0));

    }

    public void testBadExplore() throws Exception {
        // Explore indices where lack of fielddata=true on one index leads to partial failures
        GraphExploreRequest graphExploreRequest = new GraphExploreRequest();
        graphExploreRequest.indices("index1", "index2", "index_no_field_data");
        graphExploreRequest.useSignificance(false);
        int numHops = 3;
        for (int i = 0; i < numHops; i++) {
            QueryBuilder guidingQuery = null;
            if (i == 0) {
                guidingQuery = new TermQueryBuilder("const.keyword", "start");
            } else if (randomBoolean()) {
                guidingQuery = new TermQueryBuilder("const.keyword", "foo");
            }
            Hop hop = graphExploreRequest.createNextHop(guidingQuery);
            VertexRequest vr = hop.addVertexRequest("num");
            vr.minDocCount(1);
        }
        Map<String, Integer> expectedTermsAndDepths = new HashMap<>();
        expectedTermsAndDepths.put("1", 0);
        expectedTermsAndDepths.put("2", 1);
        expectedTermsAndDepths.put("3", 2);

        GraphExploreResponse exploreResponse = highLevelClient().graph().explore(graphExploreRequest, RequestOptions.DEFAULT);
        Map<String, Integer> actualTermsAndDepths = new HashMap<>();
        Collection<Vertex> v = exploreResponse.getVertices();
        for (Vertex vertex : v) {
            actualTermsAndDepths.put(vertex.getTerm(), vertex.getHopDepth());
        }
        assertEquals(expectedTermsAndDepths, actualTermsAndDepths);
        assertThat(exploreResponse.isTimedOut(), Matchers.is(false));
        ShardOperationFailedException[] failures = exploreResponse.getShardFailures();
        assertThat(failures.length, Matchers.equalTo(1));
        assertTrue(failures[0].reason().contains("Text fields are not optimised for operations that require per-document field data"));

    }

}
