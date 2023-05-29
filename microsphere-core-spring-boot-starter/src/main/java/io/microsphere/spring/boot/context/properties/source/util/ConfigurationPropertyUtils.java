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
package io.microsphere.spring.boot.context.properties.source.util;

import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

import static io.microsphere.reflect.MethodUtils.invokeStaticMethod;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;

/**
 * The utilities class of {@link ConfigurationProperty}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class ConfigurationPropertyUtils {

    private static final ClassLoader classLoader = ConfigurationPropertyUtils.class.getClassLoader();

    /**
     * The class name before Spring Boot 2.2.3
     */
    private static final String BEAN_PROPERTY_NAME_CLASS_NAME = "org.springframework.boot.context.properties.bind.BeanPropertyName";

    /**
     * The class name as a constant since Spring Boot 2.2.3
     */
    private static final String DATA_OBJECT_PROPERTY_NAME_CLASS_NAME = "org.springframework.boot.context.properties.bind.DataObjectPropertyName";

    private static final Class<?> BEAN_PROPERTY_NAME_CLASS = resolveClass(BEAN_PROPERTY_NAME_CLASS_NAME, classLoader);

    private static final Class<?> DATA_OBJECT_PROPERTY_NAME_CLASS = resolveClass(DATA_OBJECT_PROPERTY_NAME_CLASS_NAME, classLoader);

    private ConfigurationPropertyUtils() throws InstantiationException {
        throw new InstantiationException();
    }

    public static final String getPrefix(ConfigurationPropertyName name, BindContext context) {
        int depth = context.getDepth();
        String propertyName = name.toString();
        String prefix = propertyName;
        for (int i = 0; i < depth; i++) {
            int lastIndex = prefix.lastIndexOf('.');
            if (lastIndex > -1) {
                prefix = prefix.substring(0, lastIndex);
            }
        }
        return prefix;
    }

    /**
     * Return the specified Java Bean property name in dashed form.
     *
     * @param name the source name
     * @return the dashed from
     * @see org.springframework.boot.context.properties.bind.BeanPropertyName
     * @see org.springframework.boot.context.properties.bind.DataObjectPropertyName
     */
    public static String toDashedForm(String name) {
        if (DATA_OBJECT_PROPERTY_NAME_CLASS != null) {
            return invokeStaticMethod(DATA_OBJECT_PROPERTY_NAME_CLASS, "toDashedForm", name);
        }
        return toDashedForm(name, 0);
    }

    /**
     * Return the specified Java Bean property name in dashed form.
     * (Source from org.springframework.boot.context.properties.bind.BeanPropertyName)
     *
     * @param name  the source name
     * @param start the starting char
     * @return the dashed from
     */
    protected static String toDashedForm(String name, int start) {
        StringBuilder result = new StringBuilder();
        char[] chars = name.replace("_", "-").toCharArray();
        for (int i = start; i < chars.length; i++) {
            char ch = chars[i];
            if (Character.isUpperCase(ch) && result.length() > 0 && result.charAt(result.length() - 1) != '-') {
                result.append("-");
            }
            result.append(Character.toLowerCase(ch));
        }
        return result.toString();
    }


}
