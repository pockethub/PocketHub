# Git Status script
This script is used by jenkins to attach statuses to commits (<https://developer.github.com/v3/repos/statuses/>). The built is packaged into an executable jar.

## Contributing
To make changes to the script just modify the kotlin files under the `scripts/git-status/project` folder.

To build the project, run the `shadowJar` command in the project folder. It creates a *fat jar* including all libraries which makes it a standalone executable.

## Usage
`java -jar ./scripts/git-status/kotStatus.jar [status] --context=[context] --description=[description]`

The script retrieves other necessary information (github personal access token, git commit number, build url) from jenkins environment variables.
