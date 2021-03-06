/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.integration.tests.container;

import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockDockerConnectionManager;
import org.eclipse.linuxtools.docker.reddeer.condition.ContainerIsDeployedCondition;
import org.eclipse.linuxtools.docker.reddeer.core.ui.wizards.ImageRunSelectionPage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockContainerInfoFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerClientFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockDockerConnectionFactory;
import org.eclipse.linuxtools.internal.docker.ui.testutils.MockImageFactory;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

/**
 *
 * @author jkopriva@redhat.com
 * @contributor adietish@redhat.com
 *
 */
public class BasicSecurityTest extends AbstractImageBotTest {

	private static final String IMAGE_NAME = IMAGE_BUSYBOX;
	private static final String IMAGE_TAG = IMAGE_TAG_LATEST;
	private static final String CONTAINER_NAME = "test_run_busybox";

	@Before
	public void before() throws DockerException, InterruptedException {
		deleteAllConnections();
		getConnection();
		pullImage(IMAGE_NAME, IMAGE_TAG);
	}

	@Test
	public void testBasicSecurity() {
		DockerImagesTab imagesTab = openDockerImagesTab();
		imagesTab.runImage(IMAGE_NAME + ":" + IMAGE_TAG);
		ImageRunSelectionPage firstPage = new ImageRunSelectionPage(imagesTab);
		firstPage.setContainerName(CONTAINER_NAME);
		firstPage.setAllocatePseudoTTY();
		firstPage.setKeepSTDINOpen();
		firstPage.setBasicSecurity();
		firstPage.finish();
		if (mockitoIsUsed()) {
			runContainer();
			// MockDockerClientFactory.addContainer(this.client,
			// this.createdContainer, this.containerInfo);
			getConnection().refresh();
			new WaitUntil(new ContainerIsDeployedCondition(CONTAINER_NAME, getConnection()));
		}
		new WaitWhile(new JobIsRunning());
		PropertySheet propertiesView = openPropertiesTabForContainer("Inspect", CONTAINER_NAME);
		String readonlyProp = propertiesView.getProperty("HostConfig", "ReadonlyRootfs").getPropertyValue();
		assertTrue("Container is not running read-only!", readonlyProp.equals("true"));
		String tmpfsProp = propertiesView.getProperty("HostConfig", "Tmpfs", "/run").getPropertyValue();
		assertTrue("Container /run is not tmpfs rw,exec!", tmpfsProp.equals("rw,exec"));
		tmpfsProp = propertiesView.getProperty("HostConfig", "Tmpfs", "/tmp").getPropertyValue();
		assertTrue("Container /tmp is not tmpfs rw,exec!", tmpfsProp.equals("rw,exec"));
		String capDropProp = propertiesView.getProperty("HostConfig", "CapDrop", "").getPropertyValue();
		assertTrue("Container does not have capDrop all!", capDropProp.equals("all"));
	}

	@Override
	@After
	public void after() {
		deleteContainerIfExists(CONTAINER_NAME);
	}

	private void runContainer() {
		final DockerClient client = MockDockerClientFactory
				.container(MockContainerFactory.name(CONTAINER_NAME).status("Stopped").build(),
						MockContainerInfoFactory.link(IMAGE_NAME + ":" + IMAGE_TAG_LATEST).id("TestTestTestTestTest").ipAddress("127.0.0.1").build())
				.image(MockImageFactory.id("987654321abcde").name(IMAGE_UHTTPD + ":" + IMAGE_TAG_LATEST).build())
				.build();
		final org.eclipse.linuxtools.internal.docker.core.DockerConnection dockerConnection = MockDockerConnectionFactory
				.from(DEFAULT_CONNECTION_NAME, client).withDefaultTCPConnectionSettings();
		MockDockerConnectionManager.configureConnectionManager(dockerConnection);
	}
}