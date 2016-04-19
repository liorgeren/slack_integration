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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A minimal Slack client for publishing messages to a pre-configured incoming
 * webhook (https://api.slack.com/incoming-webhooks).
 *
 * @author Matthew Montgomery
 */
public class WebhookClient
{
    /**
     * The class logger instance.
     **/
    private static final Logger LOGGER =
            LoggerFactory.getLogger(WebhookClient.class);

    /**
     * Publish a message to the provided Slack webhook URL.
     *
     * @param message    The message to publish.
     * @param webhookUrl The web hook URL to publish to.
     * @return true, if successful; otherwise false
     */
    public boolean publish(String message, String webhookUrl)
    {
        if (message == null || message.equals(""))
        {
            throw new IllegalArgumentException(
                    "message cannot be null or empty");
        }

        if (webhookUrl == null || webhookUrl.equals(""))
        {
            throw new IllegalArgumentException(
                    "webhookUrl cannot be null or empty");
        }

        boolean result;
        result = false;

        String response;
        response = postRequest(message, webhookUrl);

        if ("ok".equals(response))
        {
            result = true;
        }
        else
        {
            LOGGER.error("Unexpected response: [" + response + "].");
        }

        return result;
    }

    /**
     * Initiates an HTTP POST to the provided Webhook URL.
     *
     * @param message    The message payload.
     * @param webhookUrl The URL to post to.
     * @return The response payload from Slack.
     */
    private String postRequest(String message, String webhookUrl)
    {
        String response;

        HttpURLConnection connection;
        connection = null;
        try
        {
            connection = openConnection(webhookUrl);
            try
            {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/json");
                connection.setRequestProperty("charset", "utf-8");

                connection.setDoInput(true);
                connection.setDoOutput(true);

                DataOutputStream request;
                request = new DataOutputStream(connection.getOutputStream());

                request.write(message.getBytes("UTF-8"));
                request.flush();
                request.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(
                        "Error posting message to Slack: [" + e.getMessage() +
                                "].", e);
            }

            response = getResponse(connection);
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }

        return response;
    }

    /**
     * Opens a connection to the provided Webhook URL.
     *
     * @param webhookUrl The Webhook URL to open a connection to.
     * @return The open connection to the provided Webhook URL.
     */
    private HttpURLConnection openConnection(String webhookUrl)
    {
        try
        {
            return (HttpURLConnection) new URL(webhookUrl).openConnection();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("Unable to create webhook URL: " +
                    webhookUrl, e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(
                    "Error opening connection to Slack URL: [" +
                            e.getMessage() + "].", e);
        }
    }

    /**
     * Gets the response payload.
     *
     * @param connection The connection.
     * @return The string representation of the response.
     */
    private String getResponse(HttpURLConnection connection)
    {
        String response;

        InputStream responseStream;
        responseStream = null;
        try
        {
            responseStream = connection.getInputStream();
            response = readResponse(responseStream);
        }
        catch (IOException e)
        {
            responseStream = connection.getErrorStream();
            response = readResponse(responseStream);
        }
        finally
        {
            if (responseStream != null)
            {
                try
                {
                    responseStream.close();
                }
                catch (IOException e)
                {
                    LOGGER.debug("Error closing response stream: " +
                            e.getMessage());
                }
            }
        }

        return response;
    }

    /**
     * Reads the response from the response InputStream.
     *
     * @param responseStream The response stream from the connection.
     * @return The string representation of the response.
     */
    private String readResponse(InputStream responseStream)
    {
        try
        {
            Scanner scanner;
            scanner = new Scanner(responseStream, "UTF-8");
            scanner.useDelimiter("\\A");

            return scanner.next();
        }
        catch (Exception e)
        {
            throw new RuntimeException(
                    "Error reading response: [" + e.getMessage() + "].", e);
        }
    }
}
