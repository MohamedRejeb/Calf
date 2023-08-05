# Multiplatform library template

## What is it?

It's the barebones library project intended to quickly bootstrap a Kotlin Multiplatform library, that is deployable to Maven Central.

It has only one function: generate the [Fibonacci sequence](https://en.wikipedia.org/wiki/Fibonacci_sequence) starting from platform-provided numbers. Also it has a test for each platform just to be sure that tests run.

*Almost all links will work correctly in your own repository*

## How do I build it?

1. - [x] Clone this repository ot just [use it as template](https://github.com/asm0dey/dummylib-multiplatform/generate)
1. - [ ] Edit project name in [`settings.gradle.kts`](settings.gradle.kts#L17)
1. - [ ] Edit [`groupId` and `version`](convention-plugins/src/main/kotlin/module.publication.gradle.kts#L10-L11)
    1. If you need the Android support update namespace [there](calf-ui/build.gradle.kts#L38) too
    1. If you don't need an Android support delete the [`android` section](calf-ui/build.gradle.kts#L37-L43)
1. - [ ] Edit [build targets you need](calf-ui/build.gradle.kts#L9-L21)

At this stage you have everything setup to work with Kotlin Multiplatform the project should be buildable (but you might need to provide actual starting values for platforms you need).

## How do I make it build on GitHub Actions?

To make it work on GitHub actions you need to update the [`matrix` section in `gradle.yml`](.github/workflows/gradle.yml#L25-L34). If you didn't change platforms in `build.gradle.kts` you don't need to touch anything. But still read it to understand how it works.

Also, currently it only runs tests, but you can change this behavior as you wish by modifying `matrix` and the Gradle [build command](.github/workflows/gradle.yml#L52)

## How do I deploy it to Maven Central?

The most part of the job is already automated for you. However, deployment to Maven Central requires some manual work from your side. 

1. - [ ] Create an account at [Sonatype issue tracker](https://issues.sonatype.org/secure/Signup!default.jspa)
1. - [ ] [Create an issue](https://issues.sonatype.org/secure/CreateIssue.jspa?issuetype=21&pid=10134) to create new project for you
1. - [ ] You will have to prove that you own your desired namespace
1. - [ ] Create a GPG key with `gpg --gen-key`, use the same email address you used to sign up to the Sonatype Jira
1. - [ ] Find your key id in output of the previous command looking like `D89FAAEB4CECAFD199A2F5E612C6F735F7A9A519`
1. - [ ] Upload your key to a keyserver, for example 
    ```bash
    gpg --send-keys --keyserver keyserver.ubuntu.com "<your key id>"
    ```
1. - [ ] Now you should create secrets available to your GitHub Actions
    1. via `gh` command
    ```bash
    gh secret set OSSRH_GPG_SECRET_KEY -a actions --body "$(gpg --export-secret-key --armor "<your key id>")"
    gh secret set OSSRH_GPG_SECRET_KEY_ID -a actions --body "<your key id>"
    gh secret set OSSRH_GPG_SECRET_KEY_PASSWORD -a actions --body "<your key password>"
    gh secret set OSSRH_PASSWORD -a actions --body "<your sonatype account password>"
    gh secret set OSSRH_USERNAME -a actions --body "<your sonatype account username>"
    ```
    1. Or via interface in `Settings` → `Secrets and Variables` → `Actions` same variables as in 1.
1. - [ ] Edit deployment pom parameters in [`module.publication.gradle.kts`](convention-plugins/src/main/kotlin/module.publication.gradle.kts#L25-L44)
1. - [ ] Edit deploy targets in [`deploy.yml`](.github/workflows/deploy.yml#L23-L36)
1. - [ ] Call deployment manually when ready [in Actions](../../actions/workflows/deploy.yml) → `Run Workflow`
1. - [ ] When you see in your account on https://oss.sonatype.org that everything is fine you can release your staging repositories and add target `releaseSonatypeStagingRepository` to `deploy.yml` [after this line](.github/workflows/deploy.yml#L60). This way artifacts will be published to central automatically when tests pass.
