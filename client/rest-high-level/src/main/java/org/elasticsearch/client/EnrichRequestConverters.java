/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticsearch.client;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.elasticsearch.client.enrich.DeletePolicyRequest;
import org.elasticsearch.client.enrich.ExecutePolicyRequest;
import org.elasticsearch.client.enrich.GetPolicyRequest;
import org.elasticsearch.client.enrich.PutPolicyRequest;
import org.elasticsearch.client.enrich.StatsRequest;

import java.io.IOException;

import static org.elasticsearch.client.RequestConverters.REQUEST_BODY_CONTENT_TYPE;
import static org.elasticsearch.client.RequestConverters.createEntity;

final class EnrichRequestConverters {

    static Request putPolicy(PutPolicyRequest putPolicyRequest) throws IOException {
        String endpoint = new RequestConverters.EndpointBuilder().addPathPartAsIs("_enrich", "policy")
            .addPathPart(putPolicyRequest.getName())
            .build();
        Request request = new Request(HttpPut.METHOD_NAME, endpoint);
        request.setEntity(createEntity(putPolicyRequest, REQUEST_BODY_CONTENT_TYPE));
        return request;
    }

    static Request deletePolicy(DeletePolicyRequest deletePolicyRequest) {
        String endpoint = new RequestConverters.EndpointBuilder().addPathPartAsIs("_enrich", "policy")
            .addPathPart(deletePolicyRequest.getName())
            .build();
        return new Request(HttpDelete.METHOD_NAME, endpoint);
    }

    static Request getPolicy(GetPolicyRequest getPolicyRequest) {
        String endpoint = new RequestConverters.EndpointBuilder().addPathPartAsIs("_enrich", "policy")
            .addCommaSeparatedPathParts(getPolicyRequest.getNames())
            .build();
        return new Request(HttpGet.METHOD_NAME, endpoint);
    }

    static Request stats(StatsRequest statsRequest) {
        String endpoint = new RequestConverters.EndpointBuilder().addPathPartAsIs("_enrich", "_stats").build();
        return new Request(HttpGet.METHOD_NAME, endpoint);
    }

    static Request executePolicy(ExecutePolicyRequest executePolicyRequest) {
        String endpoint = new RequestConverters.EndpointBuilder().addPathPartAsIs("_enrich", "policy")
            .addPathPart(executePolicyRequest.getName())
            .addPathPartAsIs("_execute")
            .build();
        Request request = new Request(HttpPost.METHOD_NAME, endpoint);
        if (executePolicyRequest.getWaitForCompletion() != null) {
            request.addParameter("wait_for_completion", executePolicyRequest.getWaitForCompletion().toString());
        }
        return request;
    }

}
