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
import com.cisco.gerrit.plugins.slack.util.ResourceHelper;
import com.google.common.base.Ascii;
import com.google.gerrit.server.events.CommentAddedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A specific MessageGenerator implementation that can generate a message for
 * a commend added event.
 *
 * @author Kenneth Pedersen
 */
public class CommentAddedMessageGenerator extends MessageGenerator
{
    /**
     * The class logger instance.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(CommentAddedMessageGenerator.class);

    private ProjectConfig config;
    private CommentAddedEvent event;

    /**
     * Creates a new CommentAddedMessageGenerator instance using the provided
     * CommentAddedEvent instance.
     *
     * @param event The CommentAddedEvent instance to generate a message for.
     */
    protected CommentAddedMessageGenerator(CommentAddedEvent event,
                                           ProjectConfig config)
    {
        if (event == null)
        {
            throw new NullPointerException("event cannot be null");
        }

        this.event = event;
        this.config = config;
    }

    @Override
    public boolean shouldPublish()
    {
        return config.isEnabled();
    }

    @Override
    public String generate()
    {
        String message;
        message = "";

        try
        {
            String template;
            template = ResourceHelper.loadNamedResourceAsString(
                    "basic-message-template.json");

            StringBuilder text;
            text = new StringBuilder();

            text.append(escape(event.author.get().name));
            text.append(" commented\\n>>>");
            text.append(escape(event.change.get().project));
            text.append(" (");
            text.append(escape(event.change.get().branch));
            text.append("): ");
            text.append(escape(Ascii.truncate(event.comment, 200, "...")));
            text.append(" (");
            text.append(escape(event.change.get().url));
            text.append(")");

            message = String.format(template, text, config.getChannel(),
                    config.getUsername());
        }
        catch (Exception e)
        {
            LOGGER.error("Error generating message: " + e.getMessage(), e);
        }

        return message;
    }
}
