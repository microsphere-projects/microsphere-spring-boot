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
package io.microsphere.spring.boot.condition;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;

/**
 * {@link Condition} that checks if the prefix of properties are found in environment.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringBootCondition
 * @see ConditionalOnPropertyPrefix
 * @since 1.0.0
 */
public class OnPropertyPrefixCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {

        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(ConditionalOnPropertyPrefix.class.getName()));

        String[] prefixValues = annotationAttributes.getStringArray("value");

        ConfigurableEnvironment environment = (ConfigurableEnvironment) context.getEnvironment();

        boolean matched = false;

        for (String prefix : prefixValues) {

            if (startsWith(environment, prefix)) {
                matched = true;
                break;
            }

        }

        return matched ? ConditionOutcome.match() : ConditionOutcome.noMatch("The prefix values " +
                Arrays.asList(prefixValues) + " were not found in Environment!");

    }

    private boolean startsWith(ConfigurableEnvironment environment, String prefix) {

        final String actualPrefix = prefix.endsWith(".") ? prefix : prefix + ".";

        boolean started = false;

        MutablePropertySources mutablePropertySources = environment.getPropertySources();

        for (PropertySource propertySource : mutablePropertySources) {

            if (propertySource instanceof EnumerablePropertySource) {

                EnumerablePropertySource source = EnumerablePropertySource.class.cast(propertySource);

                String[] propertyNames = source.getPropertyNames();

                for (String propertyName : propertyNames) {

                    if (propertyName.startsWith(actualPrefix)) {
                        started = true;
                        break;
                    }

                }

            }

        }

        return started;

    }

}
