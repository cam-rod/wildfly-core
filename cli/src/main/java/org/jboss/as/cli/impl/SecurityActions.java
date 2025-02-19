/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.as.cli.impl;

import static java.lang.Runtime.getRuntime;
import static java.security.AccessController.doPrivileged;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.login.Configuration;

import org.wildfly.security.manager.WildFlySecurityManager;
import org.wildfly.security.manager.action.AddShutdownHookAction;

/**
 * Package privileged actions
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author Scott.Stark@jboss.org
 * @author Alexey Loubyansky
 */
class SecurityActions {
    static void addShutdownHook(Thread hook) {
        if (! WildFlySecurityManager.isChecking()) {
            getRuntime().addShutdownHook(hook);
        } else {
            doPrivileged(new AddShutdownHookAction(hook));
        }
    }

    static Configuration getGlobalJaasConfiguration() throws SecurityException {
        if (WildFlySecurityManager.isChecking() == false) {
            return internalGetGlobalJaasConfiguration();
        } else {

            try {
                return doPrivileged(new PrivilegedExceptionAction<Configuration>() {

                    @Override
                    public Configuration run() throws Exception {
                        return internalGetGlobalJaasConfiguration();
                    }

                });
            } catch (PrivilegedActionException e) {
                throw (SecurityException) e.getCause();
            }

        }
    }

    private static Configuration internalGetGlobalJaasConfiguration() throws SecurityException {
        return Configuration.getConfiguration();
    }

    static void setGlobalJaasConfiguration(final Configuration configuration) throws SecurityException {
        if (WildFlySecurityManager.isChecking() == false) {
            internalSetGlobalJaasConfiguration(configuration);
        } else {

            try {
                doPrivileged(new PrivilegedExceptionAction<Void>() {

                    @Override
                    public Void run() throws Exception {
                        internalSetGlobalJaasConfiguration(configuration);

                        return null;
                    }

                });
            } catch (PrivilegedActionException e) {
                throw (SecurityException) e.getCause();
            }

        }
    }

    private static void internalSetGlobalJaasConfiguration(final Configuration configuration) throws SecurityException {
        Configuration.setConfiguration(configuration);
    }
}
