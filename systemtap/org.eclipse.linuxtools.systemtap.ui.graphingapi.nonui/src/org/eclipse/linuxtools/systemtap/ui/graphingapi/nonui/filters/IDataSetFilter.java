/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.filters;

import java.util.ArrayList;

import org.eclipse.ui.IMemento;

public interface IDataSetFilter {
	@SuppressWarnings("unchecked")
	public ArrayList[] filter(ArrayList[] data);
	public String getID();
	public void writeXML(IMemento parent);
}
