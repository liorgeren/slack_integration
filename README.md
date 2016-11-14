Slack Integration Plugin
========================

A simple Gerrit plugin that allows the publishing of certain Gerrit events
to a configured Slack Webhook URL. The plugin uses Gerrit's inherited project
configuration support so common config options can be set at a higher level
and shared by many projects along with project specific config options.


Development
-----------

To build the plugin,
[JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html),
[Maven 3.0.x](http://maven.apache.org/download.cgi) and
[Ant 1.9.x](https://ant.apache.org/bindownload.cgi) are required.
Once installed use _mvn_ to build.

    cd ./slack-integration
    mvn install

This command will compile/test and package the resulting artifact.

    cd ./slack-integration
    mvn package

Once packaged, you can install the _./target/slack-integration.jar_ file into
Gerrit.


Installation
------------

Installing the Slack Integration Plugin is as simple as copying the resulting
JAR file into the Gerrit plugins directory. Assuming you installation of Gerrit
is located at _/usr/local/gerrit_ you simply execute the following.

    cp ./slack-integration.jar /usr/local/gerrit/plugins

Simple substitute the path to your Gerrit plugins directory as needed. Gerrit
automatically loads new plugins and unloads old plugins, no restart is
required.


Configuration
-------------

The first thing you need to do is setup an incoming webhook integration in
Slack. This is done via my.slack.com - Configure Integrations.

Configuration of the Slack Integration Plugin is done in Gerrit via a project
specific config file. This configuration is stored in the project’s
_project.config_ file on the _refs/meta/config_ branch of the project.

Common configuration options that can be shared between multiple projects
can be placed in the _All-Projects_ config branch, or another project that
serves as an inherited base. Config options can then be overridden in the
actual project's config branch. For example, you may want to specify a default
webhook URL, username and channel then override the channel to be specific
to each project.

Editing a project's config

    mkdir <project>-config
    cd <project>-config
    git init
    git remote add origin ssh://<admin-user>@<gerrit-host>:29418/<project>
    git fetch origin refs/meta/config:refs/remotes/origin/meta/config
    git checkout meta/config

Create the following config block

    vi project.config

    [plugin "slack-integration"]
        enabled = true
        webhookurl = https://<web-hook-url>
        channel = general
        username = gerrit
        ignore = "^WIP.*"

Commit and push changes

    git commit -a
    git push origin meta/config:meta/config


Configuration Options
---------------------

The following configuration options are available

    enabled – boolean (true/false)
        When true, enables Slack integration (defaults to false).
    webhookurl - String
        The Slack webhook URL to publish to (defaults to an
        empty string).
    channel - String
        The Slack channel to publish to (defaults to "general").
    username - String
        The Slack username to publish as (defaults to "gerrit").
    ignore - Pattern
        A "dotall" enabled regular expression pattern that, when matches
        against a commit message, will prevent the publishing of patchset
        created event messages (defaults to an empty string).
    publish-on-patch-set-created - boolean (true/false)
        Whether a Slack notification should be published when a new patch set
        is created.
    publish-on-change-merged - boolean (true/false)
        Whether a Slack notification should be published when a change is
        merged.
    publish-on-comment-added - boolean (true/false)
        Whether a Slack notification should be published when a comment is
        added to a review.
    publish-on-reviewer-added - boolean (true/false)
        Whether a Slack notification should be published when a reviewer is
        added to a review.
