/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.as.host.controller.operations;


import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RUNNING_SERVER;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING_GROUP;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ProxyController;
import org.jboss.as.host.controller.logging.HostControllerLogger;
import org.jboss.dmr.ModelNode;

/**
 * {@code OperationHandler} removing an existing server configuration.
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author Emanuel Muckenhuber
 */
public class ServerRemoveHandler extends AbstractRemoveStepHandler {

    public static final String OPERATION_NAME = REMOVE;

    public static final ServerRemoveHandler INSTANCE = new ServerRemoveHandler();

    /**
     * Create the ServerRemoveHandler
     */
    private ServerRemoveHandler() {
    }

    @Override
    protected void performRemove(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        final String socketBindingGroupOverride = model.hasDefined(SOCKET_BINDING_GROUP) ? model.get(SOCKET_BINDING_GROUP).asString() : null;


        super.performRemove(context, operation, model);

        final PathAddress address = PathAddress.pathAddress(operation.require(OP_ADDR));
        final String serverName = address.getLastElement().getValue();
        final PathAddress running = address.subAddress(0, 1).append(PathElement.pathElement(RUNNING_SERVER, serverName));

        final ModelNode runningServerRemove = new ModelNode();
        runningServerRemove.get(OP).set(REMOVE);
        runningServerRemove.get(OP_ADDR).set(running.toModelNode());

        context.addStep(runningServerRemove, new OperationStepHandler() {
            @Override
            public void execute(final OperationContext context, final ModelNode operation) throws OperationFailedException {
                context.removeResource(PathAddress.EMPTY_ADDRESS);
            }
        }, OperationContext.Stage.MODEL, true);

        // Verify that the server is stopped
        final ModelNode verifyOp = new ModelNode();
        verifyOp.get(OP).set("verify-running-server");
        verifyOp.get(OP_ADDR).add(HOST, address.getElement(0).getValue());
        context.addStep(verifyOp, new OperationStepHandler() {
            @Override
            public void execute(final OperationContext context, final ModelNode operation) throws OperationFailedException {
                final PathAddress serverAddress = PathAddress.EMPTY_ADDRESS.append(PathElement.pathElement(SERVER, serverName));
                final ProxyController controller = context.getResourceRegistration().getProxyController(serverAddress);
                if (controller != null) {
                    context.getFailureDescription().set(HostControllerLogger.ROOT_LOGGER.serverStillRunning(serverName));
                }
            }
        }, OperationContext.Stage.RUNTIME);

    }

    protected boolean requiresRuntime(OperationContext context) {
        return false;
    }
}
