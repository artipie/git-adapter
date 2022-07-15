<a href="http://artipie.com"><img src="https://www.artipie.com/logo.svg" width="64px" height="64px"/></a>

[![Join our Telegramm group](https://img.shields.io/badge/Join%20us-Telegram-blue?&logo=telegram&?link=http://right&link=http://t.me/artipie)](http://t.me/artipie)

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/artipie/git-adapter)](http://www.rultor.com/p/artipie/git-adapter)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![Javadoc](http://www.javadoc.io/badge/com.artipie/git-adapter.svg)](http://www.javadoc.io/doc/com.artipie/git-adapter)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/artipie/git-adapter/blob/master/LICENSE.txt)
[![codecov](https://codecov.io/gh/artipie/git-adapter/branch/master/graph/badge.svg)](https://codecov.io/gh/artipie/git-adapter)
[![Hits-of-Code](https://hitsofcode.com/github/artipie/git-adapter)](https://hitsofcode.com/view/github/artipie/git-adapter)
[![Maven Central](https://img.shields.io/maven-central/v/com.artipie/git-adapter.svg)](https://maven-badges.herokuapp.com/maven-central/com.artipie/git-adapter)
[![PDD status](http://www.0pdd.com/svg?name=artipie/git-adapter)](http://www.0pdd.com/p?name=artipie/git-adapter)

`git-adapter` is an SDK for making [smart-HTTP](https://git-scm.com/book/en/v2/Git-Internals-Transfer-Protocols)
server from [Git](https://git-scm.com/) repository. This library is a part of [Artipie](https://github.com/artipie) 
binary artifact management tool and provides a fully-functionable Git Repository, which git client can perfectly understand.

Read the [Javadoc](http://www.javadoc.io/doc/com.artipie/git-adapter) for more technical details.

If you have any question or suggestions, do not hesitate to [create an issue](https://github.com/artipie/git-adapter/issues/new) 
or contact us in [Telegram](https://t.me/artipie).  
Artipie [roadmap](https://github.com/orgs/artipie/projects/3).

> **Warning**  
> Development is still in progress, adapter is not ready to use yet!

## Related

 - See 10 chapter of [Git Book](https://git-scm.com/book/en/v2/Git-Internals-Transfer-Protocols)
   to understand internals of Git repository
 - Understand [send pack pipeline](https://git-scm.com/docs/send-pack-pipeline)
 - Read documentation for git
   [send-pack](https://git-scm.com/docs/git-send-pack),
   [receive-pack](https://git-scm.com/docs/git-receive-pack),
   [fetch-pack](https://git-scm.com/docs/git-fetch-pack),
   [upload-pack](https://git-scm.com/docs/git-upload-pack)
   commands
 - See [libgit2](github.com/libgit2/)
 - Check [JGit](https://www.eclipse.org/jgit)
 - See [git](https://github.com/git/git) source code


## Usage

TBD.

## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```

To avoid build errors use Maven 3.2+ and please read [contributing rules](https://github.com/artipie/artipie/blob/master/CONTRIBUTING.md).

