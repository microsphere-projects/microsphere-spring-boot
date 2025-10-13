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

import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes.of;
import static io.microsphere.spring.core.env.PropertySourcesUtils.findPropertyNames;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.match;
import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.noMatch;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

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

        AnnotationAttributes annotationAttributes = fromMap(metadata.getAnnotationAttributes(ConditionalOnPropertyPrefix.class.getName()));

        ConfigurableEnvironment environment = (ConfigurableEnvironment) context.getEnvironment();

        ResolvablePlaceholderAnnotationAttributes attributes = of(annotationAttributes, environment);

        String[] prefixValues = attributes.getStringArray("value");

        boolean noMatched = findPropertyNames(environment, propertyName -> {
            for (String prefix : prefixValues) {
                if (propertyName.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }).isEmpty();

        return noMatched ? noMatch("The prefix values " + arrayToString(prefixValues) + " were not found in Environment!") : match();
    }
}
