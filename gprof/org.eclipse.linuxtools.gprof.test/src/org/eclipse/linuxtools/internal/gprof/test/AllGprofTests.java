/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ GprofAggregatorTest.class, GprofBinaryTest.class,
        GprofParserTest.class, GprofLaunchTest.class, GprofShortcutTest.class,
        GprofTest.class })
public class AllGprofTests {
}
