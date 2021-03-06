/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.core.ml.inference.trainedmodel;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.test.ESTestCase;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class NlpConfigUpdateTests extends ESTestCase {

    public void testTokenizationFromMap() {

        Map<String, Object> config = new HashMap<>() {
            {
                Map<String, Object> truncate = new HashMap<>();
                truncate.put("truncate", "first");
                Map<String, Object> bert = new HashMap<>();
                bert.put("bert", truncate);
                put("tokenization", bert);
            }
        };
        assertThat(NlpConfigUpdate.tokenizationFromMap(config), equalTo(new BertTokenizationUpdate(Tokenization.Truncate.FIRST)));

        config = new HashMap<>();
        assertThat(NlpConfigUpdate.tokenizationFromMap(config), nullValue());

        config = new HashMap<>() {
            {
                Map<String, Object> truncate = new HashMap<>();
                // only the truncate option is updatable
                truncate.put("do_lower_case", true);
                Map<String, Object> bert = new HashMap<>();
                bert.put("bert", truncate);
                put("tokenization", bert);
            }
        };
        assertThat(NlpConfigUpdate.tokenizationFromMap(config), nullValue());

        Map<String, Object> finalConfig = new HashMap<>() {
            {
                Map<String, Object> truncate = new HashMap<>();
                truncate.put("truncate", "first");
                Map<String, Object> bert = new HashMap<>();
                bert.put("not_bert", truncate);
                put("tokenization", bert);
            }
        };
        ElasticsearchStatusException e = expectThrows(
            ElasticsearchStatusException.class,
            () -> NlpConfigUpdate.tokenizationFromMap(finalConfig)
        );
        assertThat(e.getMessage(), containsString("unknown tokenization type expecting one of [bert] got [not_bert]"));

    }
}
