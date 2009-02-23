/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */ 
package org.apache.rat.document;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.commons.collections.CollectionUtils;
import org.apache.rat.document.impl.zip.ZipDocumentFactory;
import org.apache.rat.test.utils.Resources;

public class DocumentUtilsTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDocumentsContained() throws Exception {
        IDocumentCollection documents = ZipDocumentFactory.load(Resources.getResourceFile("elements/dummy.jar"));
        Collection contents = DocumentUtils.documentsContained(documents);
        assertEquals("8 documents in jar", 8, contents.size());
        CollectionUtils.transform(contents, DocumentUtils.toNameTransformer());
        String[] names = {"Image.png", "LICENSE", "NOTICE", "Source.java", "Text.txt", "Xml.xml", 
                "MANIFEST.MF", "Empty.txt"};
        assertTrue(CollectionUtils.isEqualCollection(Arrays.asList(names), contents));
    }
}
