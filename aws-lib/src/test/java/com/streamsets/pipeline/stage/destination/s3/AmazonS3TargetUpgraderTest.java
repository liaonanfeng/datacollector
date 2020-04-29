/*
 * Copyright 2020 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.stage.destination.s3;

import com.streamsets.pipeline.api.Config;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.StageUpgrader;
import com.streamsets.pipeline.config.upgrade.UpgraderTestUtils;
import com.streamsets.pipeline.upgrader.SelectorStageUpgrader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AmazonS3TargetUpgraderTest {
  private StageUpgrader upgrader;
  private List<Config> configs;
  private StageUpgrader.Context context;

  @Before
  public void setUp() {
    URL yamlResource = ClassLoader.getSystemClassLoader().getResource("upgrader/AmazonS3DTarget.yaml");
    upgrader = new SelectorStageUpgrader("stage", null, yamlResource);
    configs = new ArrayList<>();
    context = Mockito.mock(StageUpgrader.Context.class);
  }

  @Test
  public void testV11toV12BothEmptyCredentials() throws StageException {
    configs.add(new Config("s3TargetConfigBean.s3Config.awsConfig.awsAccessKeyId", ""));
    configs.add(new Config("s3TargetConfigBean.s3Config.awsConfig.awsSecretAccessKey", ""));
    testV11toV12("WITH_IAM_ROLES");
  }

  @Test
  public void testV11toV12FirstEmptyCredentials() throws StageException {
    configs.add(new Config("s3TargetConfigBean.s3Config.awsConfig.awsAccessKeyId", ""));
    configs.add(new Config("s3TargetConfigBean.s3Config.awsConfig.awsSecretAccessKey", "foo"));
    testV11toV12("WITH_CREDENTIALS");
  }

  @Test
  public void testV11toV12SecondEmptyCredentials() throws StageException {
    configs.add(new Config("s3TargetConfigBean.s3Config.awsConfig.awsAccessKeyId", "foo"));
    configs.add(new Config("s3TargetConfigBean.s3Config.awsConfig.awsSecretAccessKey", ""));
    testV11toV12("WITH_CREDENTIALS");
  }

  @Test
  public void testV11toV12NoneEmptyCredentials() throws StageException {
    configs.add(new Config("s3TargetConfigBean.s3Config.awsConfig.awsAccessKeyId", "foo"));
    configs.add(new Config("s3TargetConfigBean.s3Config.awsConfig.awsSecretAccessKey", "bar"));
    testV11toV12("WITH_CREDENTIALS");
  }


  private void testV11toV12(String expectedCredentialsMode) throws StageException {
    Mockito.doReturn(11).when(context).getFromVersion();
    Mockito.doReturn(12).when(context).getToVersion();

    configs = upgrader.upgrade(configs, context);

    UpgraderTestUtils.assertExists(configs, "s3TargetConfigBean.s3Config.usePathAddressModel", true);
    UpgraderTestUtils.assertExists(
        configs,
        "s3TargetConfigBean.s3Config.awsConfig.credentialMode",
        expectedCredentialsMode
    );
  }
}
