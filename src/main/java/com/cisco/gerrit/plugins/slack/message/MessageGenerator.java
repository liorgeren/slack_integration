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

/**
 * Defines a simple base class for a message generators.
 *
 * @author Matthew Montgomery
 */
public abstract class MessageGenerator
{
    /**
     * Whether or not the generated message should be published.
     *
     * @return True if the message should be published, otherwise false
     */
    public abstract boolean shouldPublish();

    /**
     * Generates an event specific message suitable for publishing.
     *
     * @return The generated message.
     */
    public abstract String generate();

    /**
     * Escapes the double quote character.
     *
     * @param message The message in which to search escape double quote
     *                characters
     *
     * @return The message with all occurrences of the double quote character
     * escaped.
     */
    protected String escape(String message)
    {
        if (message != null)
        {
            message = message.replace("\"", "\\\"");
        }

        return message;
    }
}
