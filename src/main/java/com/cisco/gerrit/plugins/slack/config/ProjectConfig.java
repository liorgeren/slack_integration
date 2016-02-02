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

package com.cisco.gerrit.plugins.slack.config;

import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.project.NoSuchProjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple configuration class to access plugin config values.
 *
 * @author Matthew Montgomery
 */
public class ProjectConfig
{
    /**
     * The class logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            ProjectConfig.class);

    /**
     * The name of the plugin config section to lookup within the gerrit.config
     * file.
     */
    public static final String CONFIG_NAME = "slack-integration";

    private boolean enabled;
    private String webhookUrl;
    private String channel;
    private String username;
    private String ignore;

    /**
     * Creates a new instance of the ProjectConfig class for the given project.
     *
     * @param configFactory The Gerrit PluginConfigFactory instance to use.
     * @param project The project to use when looking up a configuration.
     */
    public ProjectConfig(PluginConfigFactory configFactory, String project)
    {
        enabled = false;

        Project.NameKey projectNameKey;
        projectNameKey = Project.NameKey.parse(project);

        try
        {
            enabled = configFactory.getFromProjectConfigWithInheritance(
                    projectNameKey, CONFIG_NAME).getBoolean(
                    "enabled", false);

            webhookUrl = configFactory.getFromProjectConfigWithInheritance(
                    projectNameKey, CONFIG_NAME).getString(
                    "webhookurl", "");

            channel = configFactory.getFromProjectConfigWithInheritance(
                    projectNameKey, CONFIG_NAME).getString(
                    "channel", "general");

            username = configFactory.getFromProjectConfigWithInheritance(
                    projectNameKey, CONFIG_NAME).getString(
                    "username", "gerrit");

            ignore = configFactory.getFromProjectConfigWithInheritance(
                    projectNameKey, CONFIG_NAME).getString(
                    "ignore", "");
        }
        catch (NoSuchProjectException e)
        {
            LOGGER.warn("The specified project could not be found: " +
                    project);
        }
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public String getWebhookUrl()
    {
        return webhookUrl;
    }

    public String getChannel()
    {
        return channel;
    }

    public String getUsername()
    {
        return username;
    }

    public String getIgnore()
    {
        return ignore;
    }
}
