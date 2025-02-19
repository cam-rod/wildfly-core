/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.host.controller.resources;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.access.management.SensitiveTargetAccessConstraintDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.management.BaseNativeInterfaceResourceDefinition;
import org.jboss.as.controller.operations.validation.IntRangeValidator;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.host.controller.HostModelUtil;
import org.jboss.as.host.controller.operations.LocalHostControllerInfoImpl;
import org.jboss.as.host.controller.operations.NativeManagementAddHandler;
import org.jboss.as.host.controller.operations.NativeManagementRemoveHandler;
import org.jboss.dmr.ModelType;

/**
 * {@link org.jboss.as.controller.ResourceDefinition} for the native management interface resource.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public class NativeManagementResourceDefinition extends BaseNativeInterfaceResourceDefinition {

    public static final SimpleAttributeDefinition INTERFACE = new SimpleAttributeDefinitionBuilder(ModelDescriptionConstants.INTERFACE, ModelType.STRING, false)
            .setAllowExpression(true).setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, false, true))
            .setRestartAllServices()
            .addAccessConstraint(SensitiveTargetAccessConstraintDefinition.SOCKET_CONFIG)
            .setCapabilityReference("org.wildfly.network.interface", NATIVE_MANAGEMENT_RUNTIME_CAPABILITY)
            .build();

    public static final SimpleAttributeDefinition NATIVE_PORT = new SimpleAttributeDefinitionBuilder(ModelDescriptionConstants.PORT, ModelType.INT, false)
            .setAllowExpression(true).setValidator(new IntRangeValidator(0, 65535, false, true))
            .setRestartAllServices()
            .addAccessConstraint(SensitiveTargetAccessConstraintDefinition.SOCKET_CONFIG)
            .build();

    public static final AttributeDefinition[] ATTRIBUTE_DEFINITIONS = combine(COMMON_ATTRIBUTES, INTERFACE, NATIVE_PORT);

    public NativeManagementResourceDefinition(final LocalHostControllerInfoImpl hostControllerInfo) {
        super(new Parameters(RESOURCE_PATH, HostModelUtil.getResourceDescriptionResolver("core","management","native-interface"))
            .setAddHandler(new NativeManagementAddHandler(hostControllerInfo))
            .setRemoveHandler(NativeManagementRemoveHandler.INSTANCE));
    }

    @Override
    protected AttributeDefinition[] getAttributeDefinitions() {
        return ATTRIBUTE_DEFINITIONS;
    }

}
