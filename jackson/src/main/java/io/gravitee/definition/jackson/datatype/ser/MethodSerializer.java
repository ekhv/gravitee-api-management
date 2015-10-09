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
package io.gravitee.definition.jackson.datatype.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import io.gravitee.definition.model.Method;
import io.gravitee.definition.model.Policy;

import java.io.IOException;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 * @author Gravitee.io Team
 */
public class MethodSerializer extends StdScalarSerializer<Method> {

    public MethodSerializer(Class<Method> t) {
        super(t);
    }

    @Override
    public void serialize(Method method, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (! method.getPolicies().isEmpty()) {
            jgen.writeStartObject();

            jgen.writeFieldName("methods");
            jgen.writeObject(method.getMethods());

            for (Policy policy : method.getPolicies()) {
                jgen.writeObject(policy);
            }

            jgen.writeEndObject();
        }
    }
}