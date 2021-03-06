/**
 *
 * Copyright (C) 2010 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.jclouds.compute.strategy;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.jclouds.compute.config.CustomizationResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.strategy.impl.EncodeTagIntoNameRunNodesAndAddToSetStrategy;

import com.google.common.collect.Multimap;
import com.google.inject.ImplementedBy;

/**
 * creates futures that correlate to
 * 
 * @author Adrian Cole
 */
@ImplementedBy(EncodeTagIntoNameRunNodesAndAddToSetStrategy.class)
public interface RunNodesAndAddToSetStrategy {

   Map<?, Future<Void>> execute(String tag, int count, Template template, Set<NodeMetadata> goodNodes,
            Map<NodeMetadata, Exception> badNodes, Multimap<NodeMetadata, CustomizationResponse> customizationResponses);
}