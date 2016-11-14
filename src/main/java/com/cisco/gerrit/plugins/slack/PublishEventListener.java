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

package com.cisco.gerrit.plugins.slack;

import com.cisco.gerrit.plugins.slack.client.WebhookClient;
import com.cisco.gerrit.plugins.slack.config.ProjectConfig;
import com.cisco.gerrit.plugins.slack.message.MessageGenerator;
import com.cisco.gerrit.plugins.slack.message.MessageGeneratorFactory;
import com.google.gerrit.common.EventListener;
import com.google.gerrit.extensions.annotations.Listen;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.events.ChangeMergedEvent;
import com.google.gerrit.server.events.CommentAddedEvent;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.PatchSetCreatedEvent;
import com.google.gerrit.server.events.ReviewerAddedEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens for Gerrit change events and publishes messages to Slack.
 */
@Listen
@Singleton
public class PublishEventListener implements EventListener
{
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PublishEventListener.class);

    private static final String ALL_PROJECTS = "All-Projects";

    @Inject
    private PluginConfigFactory configFactory;

    @Override
    public void onEvent(Event event)
    {
        try
        {
            ProjectConfig config;
            MessageGenerator messageGenerator;

            if (event instanceof PatchSetCreatedEvent)
            {
                PatchSetCreatedEvent patchSetCreatedEvent;
                patchSetCreatedEvent = (PatchSetCreatedEvent) event;

                config = new ProjectConfig(configFactory,
                        patchSetCreatedEvent.change.get().project);

                messageGenerator = MessageGeneratorFactory.newInstance(
                        patchSetCreatedEvent, config);
            }
            else if (event instanceof ChangeMergedEvent)
            {
                ChangeMergedEvent changeMergedEvent;
                changeMergedEvent = (ChangeMergedEvent) event;

                config = new ProjectConfig(configFactory,
                        changeMergedEvent.change.get().project);

                messageGenerator = MessageGeneratorFactory.newInstance(
                        changeMergedEvent, config);
            }
            else if (event instanceof CommentAddedEvent)
            {
                CommentAddedEvent commentAddedEvent;
                commentAddedEvent = (CommentAddedEvent) event;

                config = new ProjectConfig(configFactory,
                        commentAddedEvent.change.get().project);

                messageGenerator = MessageGeneratorFactory.newInstance(
                        commentAddedEvent, config);
            }
            else if (event instanceof ReviewerAddedEvent)
            {
                ReviewerAddedEvent reviewerAddedEvent;
                reviewerAddedEvent = (ReviewerAddedEvent) event;

                config = new ProjectConfig(configFactory,
                        reviewerAddedEvent.change.get().project);

                messageGenerator = MessageGeneratorFactory.newInstance(
                        reviewerAddedEvent, config);
            }
            else
            {
                LOGGER.debug("Event " + event + " not currently supported");

                config = new ProjectConfig(configFactory, ALL_PROJECTS);

                messageGenerator = MessageGeneratorFactory.newInstance(
                        event, config);
            }

            if (messageGenerator.shouldPublish())
            {
                WebhookClient client;
                client = new WebhookClient();

                client.publish(messageGenerator.generate(),
                        config.getWebhookUrl());
            }
        }
        catch (Throwable e)
        {
            LOGGER.error("Event " + event + " processing failed", e);
        }
    }
}
