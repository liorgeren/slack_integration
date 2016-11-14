/*
 * Copyright 2016 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package com.cisco.gerrit.plugins.slack.message;

import com.cisco.gerrit.plugins.slack.config.ProjectConfig;
import com.google.common.base.Suppliers;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.data.AccountAttribute;
import com.google.gerrit.server.data.ChangeAttribute;
import com.google.gerrit.server.events.ReviewerAddedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the ReviewerAddedMessageGeneratorTest class. The expected behavior
 * is that the ReviewerAddedMessageGeneratorTest should publish if both the plugin
 * and publish-on-reviewer-added are enabled.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Project.NameKey.class})
public class ReviewerAddedMessageGeneratorTest
{
    private static final String PROJECT_NAME = "test-project";

    private Project.NameKey mockNameKey =
            mock(Project.NameKey.class);

    private PluginConfigFactory mockConfigFactory =
            mock(PluginConfigFactory.class);

    private PluginConfig mockPluginConfig =
            mock(PluginConfig.class);

    private ReviewerAddedEvent mockEvent = mock(ReviewerAddedEvent.class);
    private AccountAttribute mockAccount = mock(AccountAttribute.class);
    private ChangeAttribute mockChange = mock(ChangeAttribute.class);

    @Before
    public void setup() throws Exception
    {
        PowerMockito.mockStatic(Project.NameKey.class);
        when(Project.NameKey.parse(PROJECT_NAME)).thenReturn(mockNameKey);
    }

    private ProjectConfig getConfig(boolean publishOnReviewerAdded) throws Exception
    {
        Project.NameKey projectNameKey;
        projectNameKey = Project.NameKey.parse(PROJECT_NAME);

        // Setup mocks
        when(mockConfigFactory.getFromProjectConfigWithInheritance(
                projectNameKey, ProjectConfig.CONFIG_NAME))
                .thenReturn(mockPluginConfig);

        when(mockPluginConfig.getBoolean("enabled", false))
                .thenReturn(true);
        when(mockPluginConfig.getString("webhookurl", ""))
                .thenReturn("https://webook/");
        when(mockPluginConfig.getString("channel", "general"))
                .thenReturn("testchannel");
        when(mockPluginConfig.getString("username", "gerrit"))
                .thenReturn("testuser");
        when(mockPluginConfig.getString("ignore", ""))
                .thenReturn("^WIP.*");
        when(mockPluginConfig.getBoolean("publish-on-reviewer-added", true))
                .thenReturn(publishOnReviewerAdded);

        return new ProjectConfig(mockConfigFactory, PROJECT_NAME);
    }

    private ProjectConfig getConfig() throws Exception
    {
        return getConfig(true /* publishOnReviewerAdded */);
    }

    @Test
    public void factoryCreatesExpectedType() throws Exception
    {
        ProjectConfig config = getConfig();
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator instanceof ReviewerAddedMessageGenerator,
                is(true));
    }

    @Test
    public void publishesWhenExpected() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig();

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(true));
    }

    @Test
    public void doesNotPublishWhenTurnedOff() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig(false /* publishOnReviewerAdded */);

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(false));
    }

    @Test
    public void handlesInvalidIgnorePatterns() throws Exception
    {
        ProjectConfig config = getConfig();
        when(mockPluginConfig.getString("ignore", ""))
                .thenReturn(null);

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(true));
    }

    @Test
    public void generatesExpectedMessage() throws Exception
    {
        // Setup mocks
        ProjectConfig config = getConfig();
        mockEvent.change = Suppliers.ofInstance(mockChange);
        mockEvent.reviewer = Suppliers.ofInstance(mockAccount);

        mockChange.project = "testproject";
        mockChange.branch = "master";
        mockChange.commitMessage = "This is the first line\nAnd the second line.";
        mockChange.url = "https://change/";

        mockAccount.name = "Unit Tester";

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        String expectedResult;
        expectedResult = "{\"text\": \"Unit Tester was added to review\\n>>>" +
                "testproject (master): This is the first line" +
                " (https://change/)\",\"channel\": \"#testchannel\"," +
                "\"username\": \"testuser\"}\n";

        String actualResult;
        actualResult = messageGenerator.generate();

        assertThat(actualResult, is(equalTo(expectedResult)));
    }
}
