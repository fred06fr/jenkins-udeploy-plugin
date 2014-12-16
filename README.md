jenkins-udeploy-plugin
======================

A plugin for IBM Urban Code Deploy that adds process steps to control Jenkins, launching jobs and executing scripts.

A typical use case is to launch tests after a deployment.<br/>
So you can deploy your application using IBM Urban Code Deploy (UCD) as usual, then use this plugin to trigger some test jobs in Jenkins that will validate this deployment.<br/>
As the job status (SUCCESS, FAILED, UNSTABLE) is available as an output property in the UCD step, you can then use it for example to set a status on the component(s) version(s) you just deployed.<br/>
This way, UCD is fully master and store all statuses, while you keep your good old Jenkins to run the tests.<br/>

This plugin provides two steps:
- a step to run a jenkins job, giving it some parameters and getting back the job result status. Timeout handling is included.
- a step to run a jenkins system groovy script (as in the script console, see https://wiki.jenkins-ci.org/display/JENKINS/Jenkins+Script+Console), basically allowing you to control Jenkins in any way

The plugin assumes your Jenkins is secured (we are in real life, aren't we?). So you will need to feed in UCD your jenkins server URL, as well as a user name and password. This user must be allowed to run jobs and scripts in Jenkins.




