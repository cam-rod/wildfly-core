/*
 * Copyright (C) 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file
 * in the distribution for a full listing of individual contributors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.jboss.as.host.controller;

import static org.hamcrest.CoreMatchers.is;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * @author <a href="mailto:ehugonne@redhat.com">Emmanuel Hugonnet</a> (c) 2013 Red Hat, inc.
 */
public class ServerInventoryImplTestCase {
    @Test
    public void testEncodingAndDecoding() throws UnsupportedEncodingException {
        byte[] array = new byte[]{0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x00, 0x57, 0x6f, 0x72, 0x6c, 0x64};
        byte[] expected = new byte[]{0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x00, 0x57, 0x6f, 0x72, 0x6c, 0x64};
        MatcherAssert.assertThat(Arrays.equals(Base64.getDecoder().decode(Base64.getEncoder().encode(array)), expected), is(true));
    }
}
