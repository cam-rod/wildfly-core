/*
* JBoss, Home of Professional Open Source.
* Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.wildfly.core.test.standalone.mgmt.api;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CHILDREN;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INHERITED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MODEL_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.inject.Inject;
import org.jboss.as.core.model.test.KnownIssuesValidationConfiguration;
import org.jboss.as.core.model.test.TestModelType;
import org.jboss.as.model.test.ModelTestModelDescriptionValidator;
import org.jboss.as.model.test.ModelTestModelDescriptionValidator.ValidationConfiguration;
import org.jboss.as.model.test.ModelTestModelDescriptionValidator.ValidationFailure;
import org.jboss.as.remoting.RemotingExtension;
import org.jboss.as.threads.ThreadsExtension;


import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.core.testrunner.ManagementClient;
import org.wildfly.core.testrunner.WildFlyRunner;

/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
@RunWith(WildFlyRunner.class)
public class ValidateModelTestCase {

    @Inject
    private ManagementClient managementClient;

    @Test
    public void testValidateModel() throws Exception {

        ModelNode description = getDescription();

        removeSubsystems(description);

        ValidationConfiguration config = KnownIssuesValidationConfiguration.createAndFixupModel(TestModelType.STANDALONE, description);
        System.out.println(description);

        ModelTestModelDescriptionValidator validator = new ModelTestModelDescriptionValidator(new ModelNode().setEmptyList(), description, config);
        List<ValidationFailure> failures = validator.validateResources();

        if (failures.size() > 0) {
            System.out.println("==== VALIDATION FAILURES: " + failures.size());
            for (ValidationFailure failure : failures) {
                System.out.println(failure);
                System.out.println();
            }
            Assert.fail("The model failed validation");
        }
    }

    private void removeSubsystems(ModelNode description) {
        //TODO should remove all subsystems since they are tested in unit tests
        //but for now leave threads and remoting in since unit tests could not be created
        //for them due to circular maven dependencies

        ModelNode subsystemDescriptions = description.get(CHILDREN, SUBSYSTEM, MODEL_DESCRIPTION);
        Set<String> removes = new HashSet<String>();
        for (String key : subsystemDescriptions.keys()) {
            if (!key.equals(RemotingExtension.SUBSYSTEM_NAME) && !key.equals(ThreadsExtension.SUBSYSTEM_NAME)) {
                removes.add(key);
            }
        }

        for (String remove : removes) {
            subsystemDescriptions.remove(remove);
        }
    }

    protected ModelNode getDescription() throws Exception {
        ModelNode op = new ModelNode();
        op.get(OP).set(READ_RESOURCE_DESCRIPTION_OPERATION);
        op.get(OP_ADDR).setEmptyList();
        op.get(RECURSIVE).set(true);
        op.get(INHERITED).set(false);
        op.get(OPERATIONS).set(true);
        ModelNode result = managementClient.getControllerClient().execute(op);
        if (result.hasDefined(FAILURE_DESCRIPTION)) {
            throw new RuntimeException(result.get(FAILURE_DESCRIPTION).asString());
        }
        return result.require(RESULT);
    }
}
