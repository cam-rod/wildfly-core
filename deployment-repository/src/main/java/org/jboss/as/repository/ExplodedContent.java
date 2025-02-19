/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.repository;

import java.io.InputStream;

/**
 * Represent a content in an exploded deployment.
 * @author Emmanuel Hugonnet (c) 2016 Red Hat, inc.
 */
public class ExplodedContent {
    private final String relativePath;
    private final InputStream content;

    public ExplodedContent(String relativePath, InputStream content) {
        this.relativePath = relativePath;
        this.content = content;
    }

    public ExplodedContent(String relativePath) {
        this.relativePath = relativePath;
        this.content = InputStream.nullInputStream();
    }

    public String getRelativePath() {
        return relativePath;
    }

    public InputStream getContent() {
        return content;
    }

}
