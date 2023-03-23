/*
 * Copyright 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.extension.elytron;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.registry.ImmutableManagementResourceRegistration;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;

public abstract class AbstractElytronExtension implements Extension {

    protected static final String ELYTRON_SUBSYSTEM_NAME = "elytron";

    protected static <T> String getResourceName(Class<T> extensionClass) {
        return extensionClass.getPackage().getName() + ".LocalDescriptions";
    }

    /**
     *
     * @param subsystemName Name of the subsystem the resource should be registered to. Most common resources will use "elytron".
     * @param extensionClass Class loader for the resource. This usually matches the current subsystem.
     * @return A resolver for the resource description
     */
    static <T> StandardResourceDescriptionResolver getResourceDescriptionResolver(final String subsystemName, final Class<T> extensionClass, final String... keyPrefixes) {
        StringBuilder sb = new StringBuilder(subsystemName);
        if (keyPrefixes != null) {
            for (String current : keyPrefixes) {
                sb.append(".").append(current);
            }
        }

        return new StandardResourceDescriptionResolver(sb.toString(), getResourceName(extensionClass), extensionClass.getClassLoader(), true, false);
    }


    /**
     * Gets whether the given {@code resourceRegistration} is for a server, or if not,
     * is not for a resource in the {@code profile} resource tree.
     */
    static boolean isServerOrHostController(ImmutableManagementResourceRegistration resourceRegistration) {
        return resourceRegistration.getProcessType().isServer() || !ModelDescriptionConstants.PROFILE.equals(resourceRegistration.getPathAddress().getElement(0).getKey());
    }

    /**
     * @return The controller for the required service, from the registry
     * @throws org.jboss.msc.service.ServiceNotFoundException if the {@link org.jboss.msc.Service} cannot be found within the registry
     */
    @SuppressWarnings("unchecked")
    static <T> ServiceController<T> getRequiredService(ServiceRegistry serviceRegistry, ServiceName serviceName, Class<T> serviceType) {
        ServiceController<?> controller = serviceRegistry.getRequiredService(serviceName);
        return (ServiceController<T>) controller;
    }
}
