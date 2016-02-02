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

package com.cisco.gerrit.plugins.slack.client;

import com.cisco.gerrit.plugins.slack.util.ResourceHelper;
import org.junit.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class WebhookClientIntegrationTest
{
    @Test
    public void canPublishMessage() throws Exception
    {
        WebhookClient client;
        client = new WebhookClient();

        InputStream testProperties;
        testProperties = ResourceHelper.loadNamedResourceAsStream(
                "test.properties");

        Properties properties;
        properties = new Properties();
        properties.load(testProperties);

        testProperties.close();

        String message;
        message = "{\"text\": \"Integration Test Message\"}";

        String webhookUrl;
        webhookUrl = properties.getProperty("webhook-url");

        assertTrue(client.publish(message, webhookUrl));
    }
}
