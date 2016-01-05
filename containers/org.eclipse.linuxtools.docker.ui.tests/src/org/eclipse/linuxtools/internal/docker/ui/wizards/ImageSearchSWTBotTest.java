/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.linuxtools.docker.ui.wizards.ImageSearch;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageSearchResultFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.ClearConnectionManagerRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWelcomePageRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.CloseWizardRule;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.DockerConnectionManagerUtils;
import org.eclipse.linuxtools.internal.docker.ui.testutils.swt.SWTUtils;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;

/**
 * Testing the {@link ImageSearch} wizard.
 */
public class ImageSearchSWTBotTest {

	private SWTWorkbenchBot bot = new SWTWorkbenchBot();
	private DockerExplorerView dockerExplorerView;
	private SWTBotView dockerExplorerViewBot;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule();

	@Rule 
	public ClearConnectionManagerRule clearConnectionManager = new ClearConnectionManagerRule();

	@Rule 
	public CloseWizardRule closeWizard = new CloseWizardRule();

	@Before
	public void lookupDockerExplorerView() {
		dockerExplorerViewBot = bot.viewById("org.eclipse.linuxtools.docker.ui.dockerExplorerView");
		dockerExplorerView = (DockerExplorerView) (dockerExplorerViewBot.getViewReference().getView(true));
		dockerExplorerViewBot.show();
		dockerExplorerViewBot.setFocus();
	}

	@Test
	public void shouldTriggerSearchIfTermWasGiven() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory.onSearch("foo", MockImageSearchResultFactory.name("foo").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// expand the 'Images' node
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images");
		
		// when opening the "Pull..." wizard
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(imagesTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Pull...").click();

		// when specify a term
		bot.textWithLabel(WizardMessages.getString("ImagePull.name.label")).setText("foo");
		
		// when clicking on the "Search..." button
		bot.button(WizardMessages.getString("ImagePull.search.label")).click();
		// then the search should have been triggered and results should be available
		assertThat(bot.table().rowCount()).isEqualTo(1);
	}

	@Test
	public void shouldNotTriggerSearchIfNoTermWasGiven() throws InterruptedException {
		// given
		final DockerClient client = MockDockerClientFactory.onSearch("foo", MockImageSearchResultFactory.name("foo").build())
				.build();
		final DockerConnection dockerConnection = MockDockerConnectionFactory.from("Test", client).get();
		DockerConnectionManagerUtils.configureConnectionManager(dockerConnection);
		// expand the 'Images' node
		SWTUtils.syncExec(() -> dockerExplorerView.getCommonViewer().expandAll());
		final SWTBotTreeItem imagesTreeItem = SWTUtils.getTreeItem(dockerExplorerViewBot, "Test", "Images");
		
		// when opening the "Pull..." wizard
		final SWTBotTree dockerExplorerViewTreeBot = dockerExplorerViewBot.bot().tree();
		dockerExplorerViewTreeBot.select(imagesTreeItem);
		dockerExplorerViewTreeBot.contextMenu("Pull...").click();

		// when clicking on the "Search..." button
		bot.button(WizardMessages.getString("ImagePull.search.label")).click();
		// then the search should have been triggered and results should be available
		assertThat(bot.table().rowCount()).isEqualTo(0);
	}

}