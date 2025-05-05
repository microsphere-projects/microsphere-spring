/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.spring.net;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * The {@link URLConnection} adapter class is based on the Spring {@link Resource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class SpringResourceURLConnectionAdapter extends AbstractSpringResourceURLConnection {

    SpringResourceURLConnectionAdapter(URL url, Resource resource) {
        super(url, resource);
    }

    @Override
    public long getContentLengthLong() {
        try {
            return this.resource.contentLength();
        } catch (IOException e) {
            return super.getContentLengthLong();
        }
    }

    @Override
    public String getContentType() {
        String fileName = this.resource.getFilename();
        String contentType = guessContentTypeFromName(fileName);
        return contentType == null ? super.getContentType() : contentType;
    }

    @Override
    public long getLastModified() {
        try {
            return this.resource.lastModified();
        } catch (IOException e) {
            return super.getLastModified();
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return resource.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (writableResource == null) {
            throw new IOException("The resource does not support output!");
        }
        return writableResource.getOutputStream();
    }

}
