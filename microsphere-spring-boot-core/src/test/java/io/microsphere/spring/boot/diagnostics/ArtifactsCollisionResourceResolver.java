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

package io.microsphere.spring.boot.diagnostics;

import io.microsphere.classloading.Artifact;
import io.microsphere.classloading.ArtifactResourceResolver;
import io.microsphere.classloading.MavenArtifactResourceResolver;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.net.URLUtils.resolveArchiveFile;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static io.microsphere.util.ServiceLoaderUtils.loadServices;

/**
 * {@link ArtifactResourceResolver} for artifacts collision
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ArtifactResourceResolver
 * @since 1.0.0
 */
public class ArtifactsCollisionResourceResolver implements ArtifactResourceResolver {

    private static boolean enabled = false;

    private ArtifactResourceResolver delegate = new MavenArtifactResourceResolver();

    private static List<ArtifactResourceResolver> resolvers;

    static {
        resolvers = new ArrayList<>();
        for (ArtifactResourceResolver resolver : loadServices(ArtifactResourceResolver.class)) {
            if (resolver instanceof ArtifactsCollisionResourceResolver) {
                continue;
            }
            resolvers.add(resolver);
        }
    }

    @Override
    public Artifact resolve(URL resourceURL) {
        if (enabled) {
            Artifact artifact = doResolve(resourceURL);
            if (artifact == null) {
                URL classResource = getClassResource(ArtifactResourceResolver.class);
                File artifactFile = resolveArchiveFile(classResource);
                URL artifactResource = execute(artifactFile::toURL);
                return delegate.resolve(artifactResource);
            }
        }
        return null;
    }

    Artifact doResolve(URL resourceURL) {
        for (ArtifactResourceResolver resolver : resolvers) {
            Artifact artifact = resolver.resolve(resourceURL);
            if (artifact != null) {
                return artifact;
            }
        }
        return null;
    }

    static void enable() {
        enabled = true;
    }

    static void disable() {
        enabled = false;
    }
}
