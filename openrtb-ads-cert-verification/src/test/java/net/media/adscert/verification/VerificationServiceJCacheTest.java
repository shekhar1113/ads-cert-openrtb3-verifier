/*
 * Copyright © 2019 - present. MEDIA.NET ADVERTISING FZ-LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.media.adscert.verification;

import net.media.adscert.models.OpenRTB;
import net.media.adscert.utils.DigestUtil;
import net.media.adscert.utils.SignatureUtil;
import net.media.adscert.verification.cache.DefaultJCacheBuilder;
import net.media.adscert.verification.cache.VerificationServiceJCache;
import net.media.adscert.verification.metrics.MetricsManager;
import org.junit.Assert;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.expiry.Duration;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import java.security.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class VerificationServiceJCacheTest {

  @Test
  public void test()
      throws NoSuchAlgorithmException, InterruptedException, SignatureException,
          InvalidKeyException {
    final KeyPair keyPair1 = SignatureUtil.generateKeyPair();
    final KeyPair keyPair2 = SignatureUtil.generateKeyPair();
    final KeyPair keyPair3 = SignatureUtil.generateKeyPair();

    final Cache<String, PublicKey> cache =
        DefaultJCacheBuilder.newBuilder()
            .setExpiryForAccess(new Duration(TimeUnit.MILLISECONDS, 100))
            .setExpiryForCreation(new Duration(TimeUnit.MILLISECONDS, 100))
            .setExpiryForUpdate(new Duration(TimeUnit.MILLISECONDS, 100))
            .setCacheLoader(
                new CacheLoader<String, PublicKey>() {
                  int count = 0;

                  @Override
                  public PublicKey load(String key) throws CacheLoaderException {
                    ++count;
                    if (count == 1) {
                      return keyPair1.getPublic();
                    }
                    if (count == 2) {
                      return keyPair2.getPublic();
                    } else {
                      return keyPair3.getPublic();
                    }
                  }

                  @Override
                  public Map<String, PublicKey> loadAll(Iterable<? extends String> keys)
                      throws CacheLoaderException {
                    return null;
                  }
                })
            .build();
    MetricsManager metricsManager =
        new MetricsManager() {
          @Override
          public void pushMetrics(
              Map<String, Object> metricsMap, String status, String failureMessage) {
            assertTrue(metricsMap.size() == 3);
            assertTrue(metricsMap.get("domain").toString().equals("newsite.com"));
            assertTrue(metricsMap.get("ft").toString().equals("d"));
            assertTrue(metricsMap.get("tid").toString().equals("ABC7E92FBD6A"));
            assertTrue(status.equals("success"));
          }
        };
    VerificationServiceJCache service =
        new VerificationServiceJCache(cache, 100, 400l, metricsManager);
    TestUtil testUtil = new TestUtil();
    OpenRTB openRTB = testUtil.getOpenRTBObject();
    openRTB.getRequest().getSource().setCert("ads1.cert");
    String digest = DigestUtil.getDigest(openRTB);

    openRTB
        .getRequest()
        .getSource()
        .setDs(SignatureUtil.signMessage(keyPair1.getPrivate(), digest));
    Assert.assertTrue(service.verifyRequest(openRTB, true));
    Assert.assertTrue(service.verifyRequest(openRTB, false));

    Thread.sleep(560);

    // Testing refresh
    openRTB
        .getRequest()
        .getSource()
        .setDs(SignatureUtil.signMessage(keyPair2.getPrivate(), digest));
    Assert.assertTrue(service.verifyRequest(openRTB, true));
    Assert.assertTrue(service.verifyRequest(openRTB, false));

    cache.clear();

    // Testing cache clear operation
    openRTB
        .getRequest()
        .getSource()
        .setDs(SignatureUtil.signMessage(keyPair3.getPrivate(), digest));
    Assert.assertTrue(service.verifyRequest(openRTB, true));
    Assert.assertTrue(service.verifyRequest(openRTB, false));

    // Testing message expiry
    openRTB.getRequest().getSource().setTs((int) System.currentTimeMillis());
    Thread.sleep(500l);

    try {
      service.verifyRequest(openRTB, true, true);
      service.verifyRequest(openRTB, false);
      assertTrue("Timestamp check did not fail", false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }
}
