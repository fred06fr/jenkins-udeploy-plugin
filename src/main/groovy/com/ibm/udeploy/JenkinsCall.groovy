/**
 * Launches a Job with parameters on Jenkins, or execute a system script
 */
package com.ibm.udeploy;

import org.apache.commons.httpclient.*
import org.apache.commons.httpclient.auth.*
import org.apache.commons.httpclient.methods.*

// Get params from input properties
final Properties props = new Properties();
final File inputPropsFile = new File(args[0]);
inputPropsStream = new FileInputStream(inputPropsFile);
props.load(inputPropsStream);

final String jobName = props['jobName']
final String jenkinsGroovySystemScript= props['jenkinsGroovySystemScript']

final String jenkinsUrl = props['jenkinsUrl']
final String jenkinsUser = props['jenkinsUser']
final String jenkinsPassword = props['jenkinsPassword']

HttpClient clientHttp=createJenkinsClientHttp(jenkinsUrl,jenkinsUser,jenkinsPassword);

// ---- launch the given job and wait for result ------
if ((jobName!=null) && (jobName.trim().length()!=0)) {
  final String jobParameters = props['jobParameters']
  final String jobLaunchCause = props['jobLaunchCause']
  final long jobTimeout = Long.parseLong(props['jobTimeout']);
  final long pollTime=3000;

  String urlAction="/job/${jobName}/buildWithParameters?cause=${URLEncoder.encode(jobLaunchCause, 'UTF-8')}";
  for(String p:jobParameters.split(",")) {
    if ((p!=null) && (p.trim().length()!=0)) {
      int sep=p.indexOf('=');
      if (sep==-1) {
        throw new Exception("Parameters format is 'param1=xxx,param2=yyy', the param ${p} does not match");
      }
      String pName = p.substring(0, sep);
      String pValue=p.substring(sep+1);
      urlAction="${urlAction}&${pName}=${URLEncoder.encode(pValue,'UTF-8')}";
    }
  }

  println("Launch job ${jobName} on Jenkins using \"${jenkinsUrl}${urlAction}\"");

  String lastBuildNS=jenkinsGet(clientHttp,jenkinsUrl,"/job/${jobName}/lastBuild/buildNumber");
  int lastBuildN=Integer.parseInt(lastBuildNS);
  jenkinsPost(clientHttp,jenkinsUrl,urlAction,302);
  long startTime=System.currentTimeMillis();
  boolean jobCompleted=false;
  // wait for job appearance and completion
  while(((System.currentTimeMillis()-startTime)<jobTimeout) && (!jobCompleted)) {
    try {
      String isBuilding=jenkinsGet(clientHttp,jenkinsUrl,"/job/${jobName}/${lastBuildN+1}/api/xml?xpath=/*/building");
      println("Job still building, waiting...");
      if (isBuilding.contains("false")) {
        println("Job completed");
        jobCompleted=true;
      }
    } catch(IOException ex) {
      if (ex.getMessage().contains("404")) {
        // still not created/waiting, might we expected
        println("Job not yet there, waiting...");
      } else {
        throw ex;
      }
    }
    Thread.sleep(pollTime);
  }
  if (!jobCompleted) {
    throw new Exception("Timeout after ${jobTimeout} ms waiting for the job to complete. You might have a look at \"${jenkinsUrl}/job/${jobName}\" to see what happens. Failing...");
  } else {
    String jobResult=jenkinsGet(clientHttp,jenkinsUrl,"/job/${jobName}/${lastBuildN+1}/api/xml?xpath=/*/result");
    // print job result: !!! this is scanned by plugin postprocessing (see plugin.xml) to set the output property
    println("jobResult: ${jobResult}");
  }
}

// ------- launch the given groovy Jenkins system script --------
if ((jenkinsGroovySystemScript!=null) && (jenkinsGroovySystemScript.trim().length()!=0)) {
  println("Executing groovy system script on Jenkins:");
  println(jenkinsGroovySystemScript);
  String systemScriptResult=jenkinsPost(clientHttp,jenkinsUrl,"/scriptText?script=${URLEncoder.encode(jenkinsGroovySystemScript, 'UTF-8')}",200);
  // print job result: !!! this is scanned by plugin postprocessing (see plugin.xml) to set the output property
  println("scriptResult: ${systemScriptResult}");
}

def String jenkinsGet(HttpClient client,String jenkinsUrl,String urlAction) {
  HttpMethod method = new GetMethod("${jenkinsUrl}${urlAction}");
  client.executeMethod(method);
  checkResult(method,200);
  return method.getResponseBodyAsStream().text;
}

def String jenkinsPost(HttpClient client,String jenkinsUrl,String urlAction,int expectedStatus=200) {
  PostMethod method = new PostMethod("${jenkinsUrl}${urlAction}");
  client.executeMethod(method);
  checkResult(method,expectedStatus);
  return method.getResponseBodyAsStream().text;
}

// from https://github.com/jenkinsci/extras-client-demo/blob/master/src/main/java/org/jvnet/hudson/client_demo/SecuredMain.java
def HttpClient createJenkinsClientHttp(String jenkinsUrl,String jenkinsUser,String jenkinsPassword) {
  println "Authenticating on '${jenkinsUrl}' as user '${jenkinsUser}'";
  HttpClient client = new HttpClient();
  GetMethod loginLink = new GetMethod(jenkinsUrl+"/loginEntry");
  client.executeMethod(loginLink);
  checkResult(loginLink,200);
  //String location = jenkinsUrl+"/j_security_check";
  String location = jenkinsUrl+"/j_acegi_security_check";
  while(true) {
    PostMethod loginMethod = new PostMethod(location);
    loginMethod.addParameter("j_username", jenkinsUser);
    loginMethod.addParameter("j_password", jenkinsPassword);
    loginMethod.addParameter("action", "login");
    client.executeMethod(loginMethod);
    if(loginMethod.getStatusCode()==302) {
      // Commons HTTP client refuses to handle redirects for POST
      // so we have to do it manually.
      location = loginMethod.getResponseHeader("Location").getValue();
      continue;
    }
    checkResult(loginMethod,200);
    break;
  }
  println "Authenticated";
  return client;
}

def void checkResult(HttpMethod m,int expectedCode) throws IOException {
  if(m.getStatusCode()!=expectedCode)
    throw new IOException("Bad return code from Jenkins: ${m.getStatusCode()} for query '${m.getURI()}'. Result was '${m.getResponseBodyAsString()}'");
}
