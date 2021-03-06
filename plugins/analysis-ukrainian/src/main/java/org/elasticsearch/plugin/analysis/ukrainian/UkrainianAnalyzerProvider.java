/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.plugin.analysis.ukrainian;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.uk.UkrainianMorfologikAnalyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.analysis.Analysis;

public class UkrainianAnalyzerProvider extends AbstractIndexAnalyzerProvider<XUkrainianMorfologikAnalyzer> {

    private final XUkrainianMorfologikAnalyzer analyzer;

    public UkrainianAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);
        analyzer = new XUkrainianMorfologikAnalyzer(
            Analysis.parseStopWords(env, settings, UkrainianMorfologikAnalyzer.getDefaultStopSet()),
            Analysis.parseStemExclusion(settings, CharArraySet.EMPTY_SET)
        );
    }

    @Override
    public XUkrainianMorfologikAnalyzer get() {
        return this.analyzer;
    }

}
