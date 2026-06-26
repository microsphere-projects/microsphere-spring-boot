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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanProperty;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

import java.util.function.Supplier;

import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.MethodUtils.invokeStaticMethod;
import static io.microsphere.util.ClassLoaderUtils.loadClass;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.StringUtils.substringBeforeLast;
import static org.springframework.beans.BeanUtils.copyProperties;
import static org.springframework.util.ReflectionUtils.doWithFields;

/**
 * The utilities class of {@link ConfigurationProperty}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperty
 * @since 1.0.0
 */
public abstract class ConfigurationPropertyUtils {

    private static final Logger logger = getLogger(ConfigurationPropertyUtils.class);

    private static final ClassLoader classLoader = ConfigurationPropertyUtils.class.getClassLoader();

    /**
     * The {@link Class#getName() class name} of {@link org.springframework.boot.context.properties.bind.JavaBeanBinder.BeanProperty}
     */
    private static final String BEAN_PROPERTY_CLASS_NAME = "org.springframework.boot.context.properties.bind.JavaBeanBinder$BeanProperty";

    /**
     * The class name as a constant since Spring Boot 2.2.3
     */
    private static final String DATA_OBJECT_PROPERTY_NAME_CLASS_NAME = "org.springframework.boot.context.properties.bind.DataObjectPropertyName";

    private static final Class<?> DATA_OBJECT_PROPERTY_NAME_CLASS = resolveClass(DATA_OBJECT_PROPERTY_NAME_CLASS_NAME, classLoader);

    /**
     * The {@link Class} of {@link org.springframework.boot.context.properties.bind.JavaBeanBinder.BeanProperty}
     */
    private static final Class<?> BEAN_PROPERTY_CLASS = loadClass(classLoader, BEAN_PROPERTY_CLASS_NAME);

    /**
     * Get the prefix of the specified {@link ConfigurationPropertyName}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ConfigurationPropertyName name = ConfigurationPropertyName.of("server.port");
     * BindContext context = ...; // assume depth = 0
     * String prefix = getPrefix(name, context); // returns "server.port"
     *
     * ConfigurationPropertyName name2 = ConfigurationPropertyName.of("spring.datasource.url");
     * BindContext context2 = ...; // assume depth = 1
     * String prefix2 = getPrefix(name2, context2); // returns "spring.datasource"
     * }</pre>
     *
     * @param name    the {@link ConfigurationPropertyName}
     * @param context the {@link BindContext}
     * @return the prefix of the specified {@link ConfigurationPropertyName}
     * @throws IllegalArgumentException if name or context is null
     */
    @Nonnull
    public static String getPrefix(ConfigurationPropertyName name, BindContext context) {
        int depth = context.getDepth();
        if (name.isLastElementIndexed()) {
            name = name.getParent();
            depth--;
        }
        String propertyName = name.toString();
        if (depth == 0) {
            return propertyName;
        }
        String prefix = propertyName;
        for (int i = 0; i < depth; i++) {
            prefix = substringBeforeLast(prefix, DOT);
        }
        if (propertyName.equalsIgnoreCase(prefix)) {
            prefix = getSource(name);
        }
        return prefix;
    }

    /**
     * Get the source of the specified {@link ConfigurationPropertyName}
     *
     * @param name the {@link ConfigurationPropertyName}
     * @return the prefix of the specified {@link ConfigurationPropertyName}
     * @throws IllegalArgumentException if name is null
     */
    @Nonnull
    public static String getSource(ConfigurationPropertyName name) {
        Object elements = getFieldValue(name, "elements");
        return getFieldValue(elements, "source");
    }

    /**
     * Return the specified Java Bean property name in dashed form.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String result1 = toDashedForm("userName");     // returns "user-name"
     * String result2 = toDashedForm("firstName");    // returns "first-name"
     * String result3 = toDashedForm("URL");          // returns "url"
     * String result4 = toDashedForm("myURL");        // returns "my-url"
     * String result5 = toDashedForm("user_name");    // returns "user-name"
     * }</pre>
     *
     * @param name the source name
     * @return the dashed form
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
     * (Source from org.springframework.boot.context.properties.bind.BeanPropertyName before Spring Boot 2.2.3)
     *
     * @param name  the source name
     * @param start the starting char
     * @return the dashed from
     * @see org.springframework.boot.context.properties.bind.BeanPropertyName
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

    /**
     * Create a new {@link ConfigurationPropertiesBeanProperty} from the specified {@link Bindable}
     *
     * @param bindable the specified {@link Bindable}
     * @return the new {@link ConfigurationPropertiesBeanProperty} , may be {@code null}
     */
    @Nullable
    public static ConfigurationPropertiesBeanProperty newConfigurationPropertiesBeanProperty(Bindable<?> bindable) {
        Supplier<?> value = bindable.getValue();
        if (value == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("The value from Bindable[{}] is null", bindable);
            }
            return null;
        }
        Class<?> valueClass = value.getClass();
        ConfigurationPropertiesBeanProperty property = new ConfigurationPropertiesBeanProperty();

        doWithFields(valueClass, valueField -> {
            Object beanProperty = getFieldValue(value, valueField);
            if (beanProperty != null) {
                copyProperties(beanProperty, property);
            }
        }, f -> BEAN_PROPERTY_CLASS.equals(f.getType()));
        return property;
    }

    private ConfigurationPropertyUtils() {
    }
}
