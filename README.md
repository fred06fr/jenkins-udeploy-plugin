jenkins-udeploy-plugin
======================

## What is it?

A plugin for IBM Urban Code Deploy that adds process steps to control Jenkins, launching jobs and executing scripts.

A typical use case is to launch tests after a deployment.<br/>
You can deploy your application using IBM Urban Code Deploy (UCD) as usual, then use this plugin to trigger some test jobs in Jenkins that will validate this deployment.<br/>
The job result status (SUCCESS, FAILED, UNSTABLE) is available as an output property in the UCD step. You can use it for example to set a status on the component(s) version(s) you just deployed.<br/>
This way, UCD is fully master and stores all statuses, while you keep your good old Jenkins to run the tests.<br/>

This plugin provides two steps:
- a step to run a jenkins job, giving it some parameters and getting back the job result status. Timeout handling is included.
- a step to run a jenkins system groovy script (as in the script console, see https://wiki.jenkins-ci.org/display/JENKINS/Jenkins+Script+Console), basically allowing you to control Jenkins in any way

The plugin assumes your Jenkins is secured (we are in real life, aren't we?). So you will need to feed in UCD your jenkins server URL, as well as a user name and password (who has permission to run jobs and scripts in Jenkins).

_Notice that there is another UCD-Jenkins plugin (https://developer.ibm.com/urbancode/plugin/jenkins/) that provides integration on the other side, to publish Jenkins builds to UCD_

## How to build and use?

You can easily build the plugin using Maven. 
Just run "mvn install -DskipTests" in the root directory.
The built plugin is then available in "target/jenkins-udeploy-plugin-1.0-SNAPSHOT-plugin.zip", ready to import in Urban Code Deploy as usual.

If you want to run unit tests, you need to edit "src/test/resources/testPluginInput.properties" before to setup your credentials, etc.






