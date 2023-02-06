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
package io.github.microsphere.spring.redis.serializer;

import io.github.microsphere.spring.redis.event.RedisCommandEvent;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static io.github.microsphere.spring.redis.metadata.RedisMetadataRepository.findMethodIndex;
import static io.github.microsphere.spring.redis.metadata.RedisMetadataRepository.findRedisCommandMethod;
import static io.github.microsphere.spring.redis.metadata.RedisMetadataRepository.findWriteCommandMethod;
import static io.github.microsphere.spring.redis.serializer.RedisCommandEventSerializer.VersionedRedisSerializer.valueOf;
import static io.github.microsphere.spring.redis.util.RedisCommandsUtils.resolveInterfaceName;
import static io.github.microsphere.spring.redis.util.RedisCommandsUtils.resolveSimpleInterfaceName;

/**
 * {@link RedisSerializer} for {@link RedisCommandEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class RedisCommandEventSerializer extends AbstractSerializer<RedisCommandEvent> {

    public static final byte VERSION_DEFAULT = 0;

    public static final byte VERSION_V1 = 1;

    private static final RedisSerializer<RedisCommandEvent> delegate = findDelegate();

    private static RedisSerializer<RedisCommandEvent> findDelegate() {
        return findDelegate(VERSION_DEFAULT);
    }

    private static RedisSerializer<RedisCommandEvent> findDelegate(byte version) {
        return valueOf(version);
    }

    @Override
    protected byte[] doSerialize(RedisCommandEvent redisCommandEvent) throws SerializationException {
        byte version = redisCommandEvent.getSerializationVersion();
        RedisSerializer<RedisCommandEvent> delegate = findDelegate(version);
        return delegate.serialize(redisCommandEvent);
    }

    @Override
    protected RedisCommandEvent doDeserialize(byte[] bytes) throws SerializationException {
        byte version = bytes[0];
        RedisSerializer<RedisCommandEvent> delegate = findDelegate(version);
        return delegate.deserialize(bytes);
    }

    enum VersionedRedisSerializer implements RedisSerializer<RedisCommandEvent> {

        DEFAULT(VERSION_DEFAULT) {
            @Override
            protected void writeMethodMetadata(RedisCommandEvent redisCommandEvent, OutputStream outputStream) throws IOException {
                // write interfaceName
                writeInterfaceName(redisCommandEvent, outputStream);
                // write methodName
                writeMethodName(redisCommandEvent, outputStream);
                // write parameter types;
                writeParameterTypes(redisCommandEvent, outputStream);
            }

            private void writeInterfaceName(RedisCommandEvent redisCommandEvent, OutputStream outputStream) throws IOException {
                String simpleName = resolveSimpleInterfaceName(redisCommandEvent.getInterfaceName());
                writeString(simpleName, outputStream);
            }

            private void writeMethodName(RedisCommandEvent redisCommandEvent, OutputStream outputStream) throws IOException {
                writeString(redisCommandEvent.getMethodName(), outputStream);
            }

            private void writeParameterTypes(RedisCommandEvent redisCommandEvent, OutputStream outputStream) throws IOException {
                Class[] parameterTypes = redisCommandEvent.getParameterTypes();
                int parameterCount = redisCommandEvent.getParameterCount();
                // write parameter count
                outputStream.write(parameterCount);
                // write each parameter type
                for (Class parameterType : parameterTypes) {
                    writeString(parameterType.getName(), outputStream);
                }
            }

            @Override
            protected void readMethodMetadata(InputStream inputStream, RedisCommandEvent.Builder builder) throws IOException {
                readMethod(inputStream, builder);
            }

            private void readMethod(InputStream inputStream, RedisCommandEvent.Builder builder) throws IOException {
                String interfaceName = readInterfaceName(inputStream);
                String methodName = readMethodName(inputStream);
                int parameterCount = inputStream.read();
                String[] parameterTypes = readParameterTypes(inputStream, parameterCount);
                Method method = findWriteCommandMethod(interfaceName, methodName, parameterTypes);
                builder.method(method);
            }

            private String readInterfaceName(InputStream inputStream) throws IOException {
                String interfaceName = readString(inputStream);
                return resolveInterfaceName(interfaceName);
            }

            private String readMethodName(InputStream inputStream) throws IOException {
                return readString(inputStream);
            }

            private String[] readParameterTypes(InputStream inputStream, int parameterCount) throws IOException {
                String[] parameterTypes = new String[parameterCount];
                for (int i = 0; i < parameterCount; i++) {
                    parameterTypes[i] = readString(inputStream);
                }
                return parameterTypes;
            }
        },

        V1(RedisCommandEventSerializer.VERSION_V1) {

            private final ShortSerializer serializer = ShortSerializer.INSTANCE;

            @Override
            protected void writeMethodMetadata(RedisCommandEvent redisCommandEvent, OutputStream outputStream) throws IOException {
                Method redisCommandMethod = redisCommandEvent.getMethod();
                short methodIndex = findMethodIndex(redisCommandMethod);
                byte[] bytes = serializer.serialize(methodIndex);
                outputStream.write(bytes);
            }

            @Override
            protected void readMethodMetadata(InputStream inputStream, RedisCommandEvent.Builder builder) throws IOException {
                int bytesLength = serializer.getBytesLength();
                byte[] bytes = new byte[bytesLength];
                inputStream.read(bytes);
                short methodIndex = serializer.deserialize(bytes);
                Method redisCommandMethod = findRedisCommandMethod(methodIndex);
                builder.method(redisCommandMethod);
            }
        };

        private final Charset asciiCharset = StandardCharsets.US_ASCII;

        private final byte version;

        VersionedRedisSerializer(int version) {
            this((byte) version);
        }

        VersionedRedisSerializer(byte version) {
            this.version = version;
        }

        @Override
        public byte[] serialize(RedisCommandEvent redisCommandEvent) throws SerializationException {
            FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
            try {
                // write metadata(version, byte-size, method, and so on)
                writeMetadata(redisCommandEvent, outputStream);
                // write data(parameters)
                writeData(redisCommandEvent, outputStream);
            } catch (IOException e) {
                throw new SerializationException("RedisCommandEvent serialization failed", e);
            } finally {
                outputStream.close();
            }
            return outputStream.toByteArray();
        }

        /**
         * Write metadata:
         * <ol>
         *     <li>{@link #writeVersion(RedisCommandEvent, OutputStream)}</li>
         *     <li>{@link #writeApplicationName(RedisCommandEvent, OutputStream)}</li>
         *     <li>{@link #writeMethodMetadata(RedisCommandEvent, OutputStream)}</li>
         * </ol>
         *
         * @param redisCommandEvent {@link RedisCommandEvent}
         * @param outputStream      {@link OutputStream}
         * @throws IOException
         */
        protected void writeMetadata(RedisCommandEvent redisCommandEvent, OutputStream outputStream) throws IOException {
            // write version
            writeVersion(redisCommandEvent, outputStream);
            // write application name
            writeApplicationName(redisCommandEvent, outputStream);
            // Write method meta-data(e.g declaring class, method name, parameter types)
            writeMethodMetadata(redisCommandEvent, outputStream);
        }

        protected void writeVersion(RedisCommandEvent redisCommandEvent, OutputStream outputStream) throws IOException {
            outputStream.write(version);
        }

        protected void writeApplicationName(RedisCommandEvent redisCommandEvent, OutputStream outputStream) throws IOException {
            writeString(redisCommandEvent.getApplicationName(), outputStream);
        }

        protected void writeMethodMetadata(RedisCommandEvent redisCommandEvent, OutputStream outputStream) throws IOException {
            // Subclass must implement this method
        }

        /**
         * Write data
         *
         * @param redisCommandEvent
         * @param outputStream
         * @throws IOException
         */
        private void writeData(RedisCommandEvent redisCommandEvent, OutputStream outputStream) throws IOException {
            // write arguments
            writeArguments(redisCommandEvent, outputStream);
        }

        protected void writeArguments(RedisCommandEvent redisCommandEvent, OutputStream outputStream) throws IOException {
            Object[] args = redisCommandEvent.getArgs();
            Class[] parameterTypes = redisCommandEvent.getParameterTypes();
            int parameterCount = redisCommandEvent.getParameterCount();
            for (int i = 0; i < parameterCount; i++) {
                Object arg = args[i];
                Class parameterType = parameterTypes[i];
                byte[] parameter = Serializers.serialize(arg, parameterType);
                writeBytes(parameter, outputStream);
            }
        }

        protected void writeString(String value, OutputStream outputStream) throws IOException {
            byte[] bytes = getBytes(value);
            writeBytes(bytes, outputStream);
        }

        protected void writeBytes(byte[] bytes, OutputStream outputStream) throws IOException {
            int bytesLength = bytes.length;
            outputStream.write(bytesLength);
            outputStream.write(bytes);
        }

        @Override
        public RedisCommandEvent deserialize(byte[] bytes) throws SerializationException {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes, 1, bytes.length);
            RedisCommandEvent.Builder builder = RedisCommandEvent.Builder.source("stream");
            try {
                // read metadata(version, byte-size, method, and so on)
                readMetadata(inputStream, builder);
                // read data
                readData(inputStream, builder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return builder.build();
        }

        protected void readMetadata(InputStream inputStream, RedisCommandEvent.Builder builder) throws IOException {
            // read application name
            readApplicationName(inputStream, builder);
            // read method metadata
            readMethodMetadata(inputStream, builder);
        }

        protected void readApplicationName(InputStream inputStream, RedisCommandEvent.Builder builder) throws IOException {
            String applicationName = readApplicationName(inputStream);
            builder.applicationName(applicationName);
        }

        protected void readMethodMetadata(InputStream inputStream, RedisCommandEvent.Builder builder) throws IOException {
            // Subclass must implement this method
        }

        protected void readData(InputStream inputStream, RedisCommandEvent.Builder builder) throws IOException {
            // read arguments
            readArguments(inputStream, builder);
        }

        protected void readArguments(InputStream inputStream, RedisCommandEvent.Builder builder) throws IOException {
            Method method = builder.getMethod();
            int parameterCount = method.getParameterCount();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] args = new Object[parameterCount];
            for (int i = 0; i < parameterCount; i++) {
                byte[] rawArgument = readBytes(inputStream);
                Class<?> parameterType = parameterTypes[i];
                args[i] = Serializers.deserialize(rawArgument, parameterType);
            }
            builder.args(args);
        }

        protected String readApplicationName(InputStream inputStream) throws IOException {
            return readString(inputStream);
        }

        protected byte[] readBytes(InputStream inputStream) throws IOException {
            int length = inputStream.read();
            byte[] bytes = new byte[length];
            inputStream.read(bytes, 0, length);
            return bytes;
        }

        protected String readString(InputStream inputStream) throws IOException {
            int length = inputStream.read();
            byte[] bytes = new byte[length];
            inputStream.read(bytes, 0, length);
            return new String(bytes, asciiCharset);
        }

        protected byte[] getBytes(String value) {
            return value.getBytes(asciiCharset);
        }

        static RedisSerializer<RedisCommandEvent> valueOf(byte version) {
            if (RedisCommandEventSerializer.VERSION_V1 == version) {
                return V1;
            }
            return VersionedRedisSerializer.DEFAULT;
        }
    }
}
