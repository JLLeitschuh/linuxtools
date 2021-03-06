/*******************************************************************************
 * Copyright (c) 2007, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *    Alphonse Van Assche
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileDefine;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.swt.graphics.Point;

public class SpecfileHover implements ITextHover, ITextHoverExtension {

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion == null || hoverRegion.getLength() == 0) {
			return null;
		}

		Specfile spec = new SpecfileParser().parse(textViewer.getDocument());
		String currentSelection;
		try {
			currentSelection = textViewer.getDocument().get(hoverRegion.getOffset() + 1, hoverRegion.getLength() - 1);
		} catch (BadLocationException e) {
			return null;
		}

		// First we try to get a define based on the given name
		SpecfileDefine define = spec.getDefine(currentSelection);

		String value = currentSelection + ": "; //$NON-NLS-1$

		if (define != null) {
			return value + define.getStringValue();
		}

		String macroLower = currentSelection.toLowerCase();

		// If there's no such define we try to see if it corresponds to
		// a Source or Patch declaration
		String retrivedValue = RPMUtils.getSourceOrPatchValue(spec, macroLower);
		if (retrivedValue != null) {
			return value + retrivedValue;
		} else {
			// If it does not correspond to a Patch or Source macro, try to find
			// it
			// in the macro proposals list.
			retrivedValue = RPMUtils.getMacroValueFromMacroList(currentSelection);
			if (retrivedValue != null) {
				return value + retrivedValue;
			} else {
				// If it does not correspond to a macro in the list, try to find
				// it
				// in the RPM list.
				retrivedValue = Activator.getDefault().getRpmPackageList()
						.getValue(currentSelection.replaceFirst(":", "")); //$NON-NLS-1$ //$NON-NLS-2$
				if (retrivedValue != null) {
					return retrivedValue;
				}
			}
		}
		// We return null in other cases, so we don't show hover information
		// for unrecognized macros and RPM packages.
		return null;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {

		if (textViewer != null) {
			/*
			 * If the hover offset falls within the selection range return the region for
			 * the whole selection.
			 */
			Point selectedRange = textViewer.getSelectedRange();
			if (selectedRange.x >= 0 && selectedRange.y > 0 && offset >= selectedRange.x
					&& offset <= selectedRange.x + selectedRange.y) {
				return new Region(selectedRange.x, selectedRange.y);
			} else {
				IRegion region = findWord(textViewer.getDocument(), offset);
				if (region != null && region.equals(new Region(offset, 0))) {
					region = findPackages(textViewer.getDocument(), offset);
				}
				return region;
			}
		}
		return null;
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return parent -> new DefaultInformationControl(parent, false);
	}

	public static IRegion findWord(IDocument document, int offset) {
		int start = -1;
		int end = -1;
		boolean beginsWithBrace = false;

		try {
			int pos = offset;
			char c;

			while (pos >= 0) {
				c = document.getChar(pos);
				if (c == '%') {
					if (document.getChar(pos + 1) == '{') {
						beginsWithBrace = true;
					}
					break;
				} else if (c == '\n' || c == '}') {
					// if we hit the beginning of the line, it's not a macro
					return new Region(offset, 0);
				}
				--pos;
			}

			if (!beginsWithBrace) {
				--pos;
			}

			start = pos;

			pos = offset;
			int length = document.getLength();

			while (pos < length) {
				c = document.getChar(pos);
				if (beginsWithBrace && (c == '}')) {
					break;
				} else if (c == '\n' || c == '%' || c == '(') { // '(' is needed
																// for the
																// %deffatt(
																// case
					break;
					// Do not return empty region here. We have a work.
					// return new Region(offset, 0);
				} else if (!beginsWithBrace && c == ' ') {
					break;
				}
				++pos;
			}

			end = pos;

		} catch (BadLocationException x) {
		}

		if (start >= -1 && end > -1) {
			if (start == offset) {
				return new Region(start, end - start);
			} else {
				return new Region(start + 1, end - start - 1);
			}
		}

		return null;
	}

	public static IRegion findPackages(IDocument document, int offset) {
		int start = -1;
		int end = -1;
		boolean beginsWithSpace = false;
		try {
			int pos = offset;
			char c;
			while (pos >= 0) {
				c = document.getChar(pos);
				if (c == ' ' || c == '\t' || c == ':') {
					if (Character.isLetter(document.getChar(pos + 1))) {
						beginsWithSpace = true;
						break;
					} else if (c == '\n') {
						return new Region(offset, 0);
					}
				}
				--pos;
			}
			--pos;
			start = pos;
			pos = offset;
			int length = document.getLength();
			while (pos < length) {
				c = document.getChar(pos);
				if (beginsWithSpace && (!Character.isLetter(c) && !Character.isDigit(c) && c != '-')) {
					break;
				} else if (c == '\n') {
					return new Region(offset, 0);
				}
				++pos;
			}
			end = pos;

		} catch (BadLocationException x) {
		}

		if (start > -1 && end > -1) {
			return new Region(start, end - start);
		}
		return null;
	}

}
