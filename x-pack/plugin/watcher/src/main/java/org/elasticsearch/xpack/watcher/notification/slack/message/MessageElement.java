/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.watcher.notification.slack.message;

import org.elasticsearch.xcontent.ParseField;
import org.elasticsearch.xcontent.ToXContentObject;

public interface MessageElement extends ToXContentObject {

    interface XField {
        ParseField TITLE = new ParseField("title");
        ParseField TEXT = new ParseField("text");
    }
}
