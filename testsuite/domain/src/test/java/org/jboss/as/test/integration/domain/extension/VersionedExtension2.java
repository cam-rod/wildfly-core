/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.as.test.integration.domain.extension;

import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.COMPOSITE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.STEPS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.controller.transform.ExtensionTransformerRegistration;
import org.jboss.as.controller.transform.OperationResultTransformer;
import org.jboss.as.controller.transform.OperationTransformer;
import org.jboss.as.controller.transform.RejectExpressionValuesTransformer;
import org.jboss.as.controller.transform.ResourceTransformationContext;
import org.jboss.as.controller.transform.ResourceTransformer;
import org.jboss.as.controller.transform.SubsystemTransformerRegistration;
import org.jboss.as.controller.transform.TransformationContext;
import org.jboss.as.controller.transform.description.RejectAttributeChecker;
import org.jboss.as.controller.transform.description.ResourceTransformationDescriptionBuilder;
import org.jboss.as.controller.transform.description.TransformationDescription;
import org.jboss.as.controller.transform.description.TransformationDescriptionBuilder;
import org.jboss.dmr.ModelNode;

/**
 * Version 2 of an extension.
 *
 * @author Emanuel Muckenhuber
 */
public class VersionedExtension2 extends VersionedExtensionCommon {

    // New element which does not exist in v1
    private static final PathElement NEW_ELEMENT = PathElement.pathElement("new-element");
    // Other new element which does not exist in v1
    private static final PathElement OTHER_NEW_ELEMENT = PathElement.pathElement("other-new-element");
    // Element which is element>renamed in v2
    private static final PathElement RENAMED = PathElement.pathElement("renamed", "element");

    private static final SubsystemInitialization TEST_SUBSYSTEM = new SubsystemInitialization(SUBSYSTEM_NAME, true);
    private static final RejectExpressionValuesTransformer rejectExpressions = new RejectExpressionValuesTransformer("int", "string");

    @Override
    public void initialize(final ExtensionContext context) {
        // Normal test subsystem
        final SubsystemInitialization.RegistrationResult result1 = TEST_SUBSYSTEM.initializeSubsystem(context, ModelVersion.create(2, 0, 0));
        final ManagementResourceRegistration registration = result1.getResourceRegistration();

        // Register an update operation, which requires the transformer to create composite operation
        registration.registerOperationHandler(getOperationDefinition("update"), new OperationStepHandler() {
            @Override
            public void execute(final OperationContext context, final ModelNode operation) throws OperationFailedException {
                final Resource resource = context.readResourceForUpdate(PathAddress.EMPTY_ADDRESS);
                final ModelNode model = resource.getModel();
                model.get("test-attribute").set("test");
                context.getResult().set(model);
            }
        });

        // Add a new model, which does not exist in the old model
        registration.registerSubModel(createResourceDefinition(NEW_ELEMENT));
        registration.registerSubModel(createResourceDefinition(OTHER_NEW_ELEMENT));
        // Add the renamed model
        registration.registerSubModel(createResourceDefinition(RENAMED));
        registration.registerOperationHandler(getOperationDefinition("test"), new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
                context.getResult().set(true);
            }
        });
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        TEST_SUBSYSTEM.initializeParsers(context);
    }

    public static final class TransformerRegistration implements ExtensionTransformerRegistration {

        @Override
        public String getSubsystemName() {
            return SUBSYSTEM_NAME;
        }

        @Override
        public void registerTransformers(SubsystemTransformerRegistration subsystemRegistration) {

            final ResourceTransformationDescriptionBuilder subsystemBuilder = TransformationDescriptionBuilder.Factory.createSubsystemInstance();
            subsystemBuilder.getAttributeBuilder()
                    .addRejectCheck(RejectAttributeChecker.SIMPLE_EXPRESSIONS, "int", "string")
                    .end()
                    .addRawOperationTransformationOverride("update", new UpdateTransformer())
                    .addOperationTransformationOverride("test")
                    .inheritResourceAttributeDefinitions()
                    .setCustomOperationTransformer(new OperationTransformer() {
                        @Override
                        public TransformedOperation transformOperation(TransformationContext context, PathAddress address, ModelNode operation) throws OperationFailedException {
                            return new TransformedOperation(operation, new OperationResultTransformer() {
                                @Override
                                public ModelNode transformResult(ModelNode result) {
                                    result.get(RESULT).set(false);
                                    return result;
                                }
                            });
                        }
                    })
            ;

            final ModelVersion version = ModelVersion.create(1, 0, 0);
            // Discard the operations to the new element
            subsystemBuilder.discardChildResource(NEW_ELEMENT);
            // Reject operations to the other new element
            subsystemBuilder.rejectChildResource(OTHER_NEW_ELEMENT);
            // Register an alias operation transformer, transforming renamed>element to element>renamed
            subsystemBuilder.addChildRedirection(RENAMED, PathElement.pathElement("element", "renamed"));
            // Register
            TransformationDescription.Tools.register(subsystemBuilder.build(), subsystemRegistration, version);
        }
    }

    static ResourceTransformer RESOURCE_TRANSFORMER = new ResourceTransformer() {
        @Override
        public void transformResource(ResourceTransformationContext context, PathAddress address, Resource resource) throws OperationFailedException {
            final ResourceTransformationContext childContext = context.addTransformedResource(PathAddress.EMPTY_ADDRESS, resource);
            for(final Resource.ResourceEntry entry : resource.getChildren("renamed")) {
                childContext.processChild(PathElement.pathElement("element", "renamed"), entry);
            }
        }

    };

    static class UpdateTransformer implements OperationTransformer {

        @Override
        public TransformedOperation transformOperation(final TransformationContext context, final PathAddress address, final ModelNode operation) {

            // TODO does the operation transformer have to deal w/ profile in the address ?
            // final ModelNode addr = PathAddress.pathAddress(SUBSYSTEM_PATH).toModelNode();
            final ModelNode addr = address.toModelNode();

            final ModelNode write = new ModelNode();
            write.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
            write.get(OP_ADDR).set(addr);
            write.get(NAME).set(TEST_ATTRIBUTE.getName());
            write.get(VALUE).set("test");

            final ModelNode read = new ModelNode();
            read.get(OP).set(READ_RESOURCE_OPERATION);
            read.get(OP_ADDR).set(addr);

            final ModelNode composite = new ModelNode();
            composite.get(OP).set(COMPOSITE);
            composite.get(OP_ADDR).setEmptyList();
            composite.get(STEPS).add(write);
            composite.get(STEPS).add(read);

            return new TransformedOperation(composite, new OperationResultTransformer() {
                @Override
                public ModelNode transformResult(final ModelNode result) {
                    final ModelNode transformed = result.clone();
                    transformed.get(RESULT).set(result.get(RESULT, "step-2", RESULT));
                    return transformed;
                }
            });
        }
    }

}
