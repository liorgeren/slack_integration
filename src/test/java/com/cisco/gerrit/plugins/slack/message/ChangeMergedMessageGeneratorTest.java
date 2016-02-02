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
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.data.AccountAttribute;
import com.google.gerrit.server.data.ChangeAttribute;
import com.google.gerrit.server.events.ChangeMergedEvent;
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
 * Tests for the ChangeMergedMessageGeneratorTest class. The expected behavior
 * is that the ChangeMergedMessageGenerator should publish regardless of a
 * configured ignore pattern.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Project.NameKey.class})
public class ChangeMergedMessageGeneratorTest
{
    private static final String PROJECT_NAME = "test-project";

    private Project.NameKey mockNameKey =
            mock(Project.NameKey.class);

    private PluginConfigFactory mockConfigFactory =
            mock(PluginConfigFactory.class);

    private PluginConfig mockPluginConfig =
            mock(PluginConfig.class);

    private ChangeMergedEvent mockEvent = mock(ChangeMergedEvent.class);
    private AccountAttribute mockAccount = mock(AccountAttribute.class);
    private ChangeAttribute mockChange = mock(ChangeAttribute.class);

    private ProjectConfig config;

    @Before
    public void setup() throws Exception
    {
        PowerMockito.mockStatic(Project.NameKey.class);
        when(Project.NameKey.parse(PROJECT_NAME)).thenReturn(mockNameKey);

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

        config = new ProjectConfig(mockConfigFactory, PROJECT_NAME);
    }

    @Test
    public void factoryCreatesExpectedType() throws Exception
    {
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator instanceof ChangeMergedMessageGenerator,
                is(true));
    }

    @Test
    public void publishesWhenExpected() throws Exception
    {
        // Setup mocks
        mockEvent.change = mockChange;
        mockChange.commitMessage = "This is a title\nAnd a the body.";

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(true));
    }

    @Test
    public void doesNotPublishWhenExpected() throws Exception
    {
        // Setup mocks
        mockEvent.change = mockChange;
        mockChange.commitMessage = "WIP:This is a title\nAnd a the body.";

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        assertThat(messageGenerator.shouldPublish(), is(true));
    }

    @Test
    public void handlesInvalidIgnorePatterns() throws Exception
    {
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
        mockEvent.change = mockChange;
        mockEvent.submitter = mockAccount;

        mockChange.project = "testproject";
        mockChange.branch = "master";
        mockChange.url = "https://change/";
        mockChange.commitMessage = "This is a title\nAnd a the body.";

        mockAccount.name = "Unit Tester";

        // Test
        MessageGenerator messageGenerator;
        messageGenerator = MessageGeneratorFactory.newInstance(
                mockEvent, config);

        String expectedResult;
        expectedResult = "{\"text\": \"Unit Tester merged\\n>>>" +
                "testproject (master): This is a title (https://change/)\"," +
                "\"channel\": \"#testchannel\",\"username\": \"testuser\"}\n";

        String actualResult;
        actualResult = messageGenerator.generate();

        assertThat(actualResult, is(equalTo(expectedResult)));
    }
}
