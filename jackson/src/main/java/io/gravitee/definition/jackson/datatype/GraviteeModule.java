/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.definition.jackson.datatype;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.gravitee.definition.jackson.datatype.deser.MethodDeserializer;
import io.gravitee.definition.jackson.datatype.deser.PathDeserializer;
import io.gravitee.definition.jackson.datatype.deser.PolicyDeserializer;
import io.gravitee.definition.jackson.datatype.deser.ProxyDeserializer;
import io.gravitee.definition.jackson.datatype.ser.MethodSerializer;
import io.gravitee.definition.jackson.datatype.ser.PathSerializer;
import io.gravitee.definition.jackson.datatype.ser.PolicySerializer;
import io.gravitee.definition.jackson.datatype.ser.ProxySerializer;
import io.gravitee.definition.model.Method;
import io.gravitee.definition.model.Path;
import io.gravitee.definition.model.Policy;
import io.gravitee.definition.model.Proxy;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 * @author Gravitee.io Team
 */
public class GraviteeModule extends SimpleModule {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public GraviteeModule() {
        super(new Version(0, 1, 0, (String)null, (String)null, (String)null));

        // first deserializers
        addDeserializer(Method.class, new MethodDeserializer(Method.class));
        addDeserializer(Path.class, new PathDeserializer(Path.class));
        addDeserializer(Policy.class, new PolicyDeserializer(Policy.class));
        addDeserializer(Proxy.class, new ProxyDeserializer(Proxy.class));

        // then serializers:
        addSerializer(Path.class, new PathSerializer(Path.class));
        addSerializer(Method.class, new MethodSerializer(Method.class));
        addSerializer(Policy.class, new PolicySerializer(Policy.class));
        addSerializer(Proxy.class, new ProxySerializer(Proxy.class));
    }

    @Override
    public String getModuleName() {
        // yes, will try to avoid duplicate registations (if MapperFeature enabled)
        return getClass().getSimpleName();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}
