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

package org.jboss.as.server.deployment;

import static org.jboss.as.server.controller.resources.DeploymentAttributes.ENABLED;
import static org.jboss.as.server.controller.resources.DeploymentAttributes.RUNTIME_NAME;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

/**
 * @author Jason T. Greene
 */
public class DeploymentStatusHandler implements OperationStepHandler {

    public static final OperationStepHandler INSTANCE = new DeploymentStatusHandler();
    private static final ModelNode NO_METRICS = new ModelNode("no metrics available");

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        final ModelNode deployment = context.readResource(PathAddress.EMPTY_ADDRESS).getModel();
        final boolean isEnabled = ENABLED.resolveModelAttribute(context, deployment).asBoolean();
        final String runtimeName = RUNTIME_NAME.resolveModelAttribute(context, deployment).asString();
        context.addStep(new OperationStepHandler() {
            @Override
            public void execute(final OperationContext context, final ModelNode operation) {
                final ModelNode result = context.getResult();
                if (!isEnabled) {
                    result.set(AbstractDeploymentUnitService.DeploymentStatus.STOPPED.toString());
                } else {
                    final ServiceController<?> controller = context.getServiceRegistry(false).getService(Services.deploymentUnitName(runtimeName));
                    if (controller != null) {
                        if (controller.getState() == ServiceController.State.DOWN && controller.missing().isEmpty()) {
                            result.set(AbstractDeploymentUnitService.DeploymentStatus.STOPPED.toString());
                        } else {
                            result.set(((AbstractDeploymentUnitService) controller.getService()).getStatus().toString());
                        }
                    } else {
                        result.set(NO_METRICS);
                    }
                }
            }
        }, OperationContext.Stage.RUNTIME);
    }
}
