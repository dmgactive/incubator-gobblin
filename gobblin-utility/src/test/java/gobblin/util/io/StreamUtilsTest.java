/*
 * Copyright (C) 2014-2016 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.
 */

package gobblin.util.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isIn;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import org.testng.annotations.Test;


@Test
public class StreamUtilsTest {

  @Test
  public void testTarDir()
      throws IOException {
    FileSystem localFs = FileSystem.getLocal(new Configuration());

    // Set of expected Paths to be in the resulting tar file
    Set<Path> expectedPaths = Sets.newHashSet();

    // Create input directory
    Path testInDir = new Path("testDir");
    expectedPaths.add(testInDir);

    // Create output file path
    Path testOutFile = new Path("testTarOut" + UUID.randomUUID() + ".tar.gz");

    try {
      localFs.mkdirs(testInDir);

      // Create a test file path
      Path testFile1 = new Path(testInDir, "testFile1");
      expectedPaths.add(testFile1);
      FSDataOutputStream testFileOut1 = localFs.create(testFile1);
      testFileOut1.close();

      // Create a test file path
      Path testFile2 = new Path(testInDir, "testFile2");
      expectedPaths.add(testFile2);
      FSDataOutputStream testFileOut2 = localFs.create(testFile2);
      testFileOut2.close();

      // tar the input directory to the specific output file
      StreamUtils.tar(localFs, testInDir, testOutFile);

      // Confirm the contents of the tar file are valid
      try (TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(
          new GzipCompressorInputStream(localFs.open(testOutFile)))) {
        TarArchiveEntry tarArchiveEntry;

        while (null != (tarArchiveEntry = tarArchiveInputStream.getNextTarEntry())) {
          assertThat(new Path(tarArchiveEntry.getName()), isIn(expectedPaths));
        }
      }
    } finally {
      if (localFs.exists(testInDir)) {
        localFs.delete(testInDir, true);
      }
      if (localFs.exists(testOutFile)) {
        localFs.delete(testOutFile, true);
      }
    }
  }

  @Test
  public void testTarFile()
      throws IOException {
    FileSystem localFs = FileSystem.getLocal(new Configuration());

    // Set of expected Paths to be in the resulting tar file
    Set<Path> expectedPaths = Sets.newHashSet();

    // Create input file path
    Path testFile = new Path("testFile");
    expectedPaths.add(testFile);

    // Create output file path
    Path testOutFile = new Path("testTarOut" + UUID.randomUUID() + ".tar.gz");

    try {
      // Create the input file
      FSDataOutputStream testFileOut1 = localFs.create(testFile);
      testFileOut1.close();

      // tar the input file to the specific output file
      StreamUtils.tar(localFs, testFile, testOutFile);

      // Confirm the contents of the tar file are valid
      try (TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(
          new GzipCompressorInputStream(localFs.open(testOutFile)))) {
        TarArchiveEntry tarArchiveEntry;

        while (null != (tarArchiveEntry = tarArchiveInputStream.getNextTarEntry())) {
          MatcherAssert.assertThat(new Path(tarArchiveEntry.getName()), Matchers.isIn(expectedPaths));
        }
      }
    } finally {
      if (localFs.exists(testFile)) {
        localFs.delete(testFile, true);
      }
      if (localFs.exists(testOutFile)) {
        localFs.delete(testOutFile, true);
      }
    }
  }
}
