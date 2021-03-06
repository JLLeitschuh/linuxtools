/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.abstractviewers;

import org.eclipse.linuxtools.dataviewers.listeners.ISpecialDrawerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * Simple implementation of ISTField
 */
public abstract class AbstractSTDataViewersField implements ISTDataViewersField {

    @Override
    public Image getColumnHeaderImage() {
        return null;
    }

    @Override
    public int getDefaultDirection() {
        return STDataViewersComparator.ASCENDING;
    }

    @Override
    public String getDescription() {
        return getColumnHeaderText();
    }

    @Override
    public Image getDescriptionImage() {
        return null;
    }

    @Override
    public Image getImage(Object obj) {
        return null;
    }

    @Override
    public int getPreferredWidth() {
        return 100;
    }

    /**
     * @since 5.0
     */
    @Override
    public boolean isShowingByDefault() {
        return true;
    }

    @Override
    public ISpecialDrawerListener getSpecialDrawer(Object element) {
        return null;
    }

    @Override
    public Color getBackground(Object element) {
        return null;
    }

    @Override
    public Color getForeground(Object element) {
        return null;
    }

    @Override
    public String getToolTipText(Object element) {
        return null;
    }

    @Override
    public String getColumnHeaderTooltip() {
        return getColumnHeaderText();
    }

    @Override
    public String toString() {
        return getColumnHeaderText();
    }

    @Override
    public int getAlignment() {
        return SWT.NONE;
    }
}
