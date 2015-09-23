# PocketHub [![Build Status](https://travis-ci.org/pockethub/PocketHub.svg?branch=master)](https://travis-ci.org/pockethub/PocketHub)

This repository contains the source code for the PocketHub Android app.

This is the *same* repository as the now-defunct official GitHub Android app.

## What's going on here?

> What happened to the old app?

GitHub didn't want to maintain the app anymore, so it's been released to the community and maintained as a public project.
We are actively working towards a re-release to the Play Store, and this app will be the spiritual successor to the original
GitHub app.

> What about the other forks out there?

They'll remain forks. Obviously we'd prefer them to focus on improving this project, but otherwise we're not in coordinating
anything with them.

> What's the immediate plan?

We're shooting for an initial re-release just to get the app out there. There have been a significant number of changes
since the app was last updated, with many functional and design changes that we need to make sure are good to go.

> What's the less-immediate plan?

After the initial release, we'll start working on giving this app a proper refresh. Much of the UI has already been touched
up with elements of Material Design, but we have a long ways to go. Android has changed a lot since this was actively developed,
and it's time we take advantage of those changes.

> How can I help?

Please see the [issues](https://github.com/pockethub/PocketHub/issues) section to report any bugs or feature requests and
to see the list of known issues. We can't promise fast response times since we all have full time jobs of our own, but we
will do our best to respond in a timely fashion.  If you'd like to contribute, please fork this repository and contribute back using
[pull requests](https://github.com/pockethub/PocketHub/pulls).

Any contributions, large or small, major features, bug fixes, additional language translations, unit/integration tests
are welcomed and appreciated but will be thoroughly reviewed and discussed. **Please read `CONTRIBUTING.md` first!**

## Setup Environment

1. Create a github application (https://github.com/settings/applications/new)
2. Create a github.properties in the root folder of the repo
3. Add these three values to the github.properties

```
GITHUB_CLIENT=your_application_client_id
GITHUB_SECRET=your_application_client_secret
GITHUB_CALLBACK=your_callback_url
```
(The callback url needs to be in the format "your_schema://whatever_you_want")

## License

* [Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
