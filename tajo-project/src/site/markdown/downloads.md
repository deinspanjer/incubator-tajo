<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->


## Latest Releases of Apache Tajo

### 0.2-incubating Release (Nov 20, 2013)
 * [RELEASE NOTES](http://s.apache.org/tajo-0.2-release-notes)
 * [Source Tarball] (http://apache.org/dyn/closer.cgi/incubator/tajo/tajo-0.2.0-incubating/tajo-0.2.0-incubating-src.tar.gz) ([MD5](http://www.apache.org/dist/incubator/tajo/tajo-0.2.0-incubating/tajo-0.2.0-incubating-src.tar.gz.md5)) ([SHA1](http://www.apache.org/dist/incubator/tajo/tajo-0.2.0-incubating/tajo-0.2.0-incubating-src.tar.gz.sha1)) ([ASC](http://www.apache.org/dist/incubator/tajo/tajo-0.2.0-incubating/tajo-0.2.0-incubating-src.tar.gz.asc)) ([KEYS](http://www.apache.org/dist/incubator/tajo/KEYS)) ([How to verify?](#Verification))

## Getting the source code via Git

The development codebase can also be downloaded from [the Apache git repository](https://git-wip-us.apache.org/repos/asf/incubator-tajo.git) as follows:

```
git clone https://git-wip-us.apache.org/repos/asf/incubator-tajo.git
```

A read-only git repository is also mirrored on [Github](https://github.com/apache/incubator-tajo).

Once you have downloaded Tajo, follow the [getting started instructions](http://tajo.incubator.apache.org/tajo-0.8.0-doc.html#GettingStarted), and take a look at the rest of the Tajo documentation.

## <a name="Verification"></a>How to Verify the integrity of the files

It is essential that you verify the integrity of the downloaded files using the PGP or MD5 signatures. Please read Verifying Apache HTTP Server Releases for more information on why you should verify our releases.

The PGP signatures can be verified using PGP or GPG. First download the [KEYS](http://www.apache.org/dist/incubator/tajo/KEYS) as well as the asc signature file for the relevant distribution. Make sure you get these files from [the main distribution directory](http://www.apache.org/dist/incubator/tajo/) , rather than from a mirror. Then verify the signatures using

```
% pgpk -a KEYS

% pgpv tajo-0.X.0-incubating-src.tar.gz.asc
```

or


``` 
% pgp -ka KEYS

% pgp tajo-0.X.0-incubating-src.tar.gz.asc
```

or 

```
% gpg --import KEYS

% gpg --verify tajo-0.X.0-incubating-src.tar.gz.asc
```

### How to verify SHA1/MD5 hash values

Alternatively, you can verify the MD5 signature on the files. A unix program called md5 or md5sum is included in many unix distributions. It is also available as part of [GNU Textutils](http://www.gnu.org/software/textutils/textutils.html). An MD5 signature consists of 32 hex characters, and a SHA1 signature consists of 40 hex characters. You can verify the signatures as follows:

```
md5sum -c tajo-0.x.0-incubating-src.tar.gz.md5
```

or

```
sha1sum -c tajo-0.x.0-incubating-src.tar.gz.sha1
```