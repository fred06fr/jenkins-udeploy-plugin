package com.ibm.udeploy;

import com.ibm.udeploy.JenkinsCall;
import java.io.File;
import org.testng.annotations.Test;

public class JenkinsPluginTest {

  @Test
  public void testJenkinsCall() {
    File inputPropsF=new File("src/test/resources/testPluginInput.properties");
    JenkinsCall.main((String[])[inputPropsF]);
  }

}
