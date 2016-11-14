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
import com.google.gerrit.server.events.ChangeMergedEvent;
import com.google.gerrit.server.events.CommentAddedEvent;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.PatchSetCreatedEvent;
import com.google.gerrit.server.events.ReviewerAddedEvent;

/**
 * Factory used to create event specific MessageGenerator instances.
 *
 * @author Matthew Montgomery
 */
public class MessageGeneratorFactory
{
    // Made private to prevent instantiation
    private MessageGeneratorFactory() {}

    /**
     * Creates a new MessageGenerator for patchset created events.
     *
     * @param event A PatchSetCreatedEvent instance
     * @param config A ProjectConfig instance for the given event
     *
     * @return A MessageGenerator instance capable of generating a message for
     * a PatchSetCreatedEvent.
     */
    public static MessageGenerator newInstance(PatchSetCreatedEvent event,
            ProjectConfig config)
    {
        PatchSetCreatedMessageGenerator messageGenerator;
        messageGenerator = new PatchSetCreatedMessageGenerator(event, config);

        return messageGenerator;
    }

    /**
     * Creates a new MessageGenerator for change merged events.
     *
     * @param event A ChangeMergedEvent instance
     * @param config A ProjectConfig instance for the given event
     *
     * @return A MessageGenerator instance capable of generating a message for
     * a ChangeMergedEvent.
     */
    public static MessageGenerator newInstance(ChangeMergedEvent event,
            ProjectConfig config)
    {
        ChangeMergedMessageGenerator messageGenerator;
        messageGenerator = new ChangeMergedMessageGenerator(event, config);

        return messageGenerator;
    }

    /**
     * Creates a new MessageGenerator for comment added events.
     *
     * @param event A CommentAddedEvent instance
     * @param config A ProjectConfig instance for the given event
     *
     * @return A MessageGenerator instance capable of generating a message for
     * a CommentAddedEvent.
     */
    public static MessageGenerator newInstance(CommentAddedEvent event,
                                               ProjectConfig config)
    {
        CommentAddedMessageGenerator messageGenerator;
        messageGenerator = new CommentAddedMessageGenerator(event, config);

        return messageGenerator;
    }

    /**
     * Creates a new MessageGenerator for reviewer added events.
     *
     * @param event A ReviewerAddedEvent instance
     * @param config A ProjectConfig instance for the given event
     *
     * @return A MessageGenerator instance capable of generating a message for
     * a ReviewerAddedEvent.
     */
    public static MessageGenerator newInstance(ReviewerAddedEvent event,
                                               ProjectConfig config)
    {
        ReviewerAddedMessageGenerator messageGenerator;
        messageGenerator = new ReviewerAddedMessageGenerator(event, config);

        return messageGenerator;
    }

    /**
     * Creates a new MessageGenerator for unsupported events.
     *
     * @param event An Event instance
     * @param config A ProjectConfig instance for the given event
     *
     * @return A MessageGenerator instance capable of generating a message for
     * an unsupported Event.
     */
    public static MessageGenerator newInstance(Event event,
            ProjectConfig config)
    {
        UnsupportedMessageGenerator messageGenerator;
        messageGenerator = new UnsupportedMessageGenerator(event, config);

        return messageGenerator;
    }
}
