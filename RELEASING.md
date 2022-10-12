# Release Process

This guide provides a chronological steps which goes through release tagging, staging, verification and publishing.

## Check the SNAPSHOT builds and pass the tests

Check that the project builds in java 8, 11 and 17

```bash
JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/ mvn -Pdistribution clean package verify
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/ mvn -Pdistribution clean package verify
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/ mvn -Pdistribution clean package verify
```

## Set version and build 

```bash
# change release in pom.xml and README.md

# check build with the version change and version is ok.
mvn -Pdistribution clean package verify

# deploy
mvn clean package deploy

# repackage big jar for upload to github releases
mvn -Pdistribution clean package 

# commit and tag release
git add -A
git commit -S -m 'Release <2.3.12>'
git tag -a <2.3.12> -m "Tagging release <2.3.12>"
git push
git push --tags

```
    
## Prepare next iteration

```bash
# change release in pom.xml to SNAPSHOT
git add -A
git commit -S -m 'Next release cycle'
git push
```

## Create release and upload artifacts to Github

Manually creating the release in Github project page, and upload generated artifacts
