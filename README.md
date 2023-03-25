[![Verify Build Workflow](https://github.com/DJetCloud/djet-codegen/actions/workflows/verify.yaml/badge.svg)](https://github.com/DJetCloud/djet-codegen/actions/workflows/verify.yaml)
[![codecov](https://codecov.io/gh/DJetCloud/djet-codegen/branch/main/graph/badge.svg?token=J1Y895B9QV)](https://codecov.io/gh/DJetCloud/djet-codegen)
[![CodeFactor](https://www.codefactor.io/repository/github/djetcloud/djet-codegen/badge)](https://www.codefactor.io/repository/github/djetcloud/djet-codegen) 
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FDJetCloud%2Fdjet-codegen.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FDJetCloud%2Fdjet-codegen?ref=badge_shield)

# DJet Code Generation instrumentation.
DJet Cloud is an instument to simplify & sistematize building of Enterprise class systems.
The mission of DJet – build development instrumentation to effectively design, generate, develop, & deploy microservice systems & modern web applications.

We have integration with https://metucat.com which inlcudes API loader, codegen and IntelliJ Idea plugin. New features and instrumentation coming soon.

Projects:
1. openapi-load – API loader, connects to difhub.com, loads metadata of the selected system and converts it to the OpenAPI documents. 
2. codegen-cli – Codegen, takes OpenAPI specifications, settings, and generates project
3. intellij-idea – Plugin for the IntellijIdea to have UX.

## Installation and usage instructions

NOTE. Since project is in Preview, only usage in development mode available.

For new project:
1. Clone repository of the project
2. Run `./gradlew intellij-plugin:runIde`
3. Select `+ Create New Project` -> `Bootstrap from DifHub` (on left nav)
4. Type your DifHub `Username` and `Password`, `Organization Name` where your system located. 
5. Select required system and change defaults (optional) -> Press Next
6. Choose project name and directory
7. Generate your project. Progress (or exception) you can see in the console output.

For existing projects (coming soon):
1. Start plugin using the same 1. and 2. from above list
2. Open existing project
3. On the top (IJ) nav menu, right after the `Tools` find the `DifHub` menu item
4. Click `Load OpenApi` to start initialization or load (if all settings are present in project)
    - if file `djet/.credentials.yaml` is missing – plugin will create and open one. Add right username, password, organization
        - Note. this file added to gitignore file by generator so each user need to provide own credentials.
    - if file `djet/settings.yaml` is missing – plugin will create and open one. Add right settings.
5. When all required are present – OpenAPI files will be generated by the plugin.
6. Having all OpenAPI files and setting – click `Generate Code` in the Plugin menu.

If any issue: 
1. Fix the issue and create PR
2. Post issue here on GitHub


## License
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FDJetCloud%2Fdjet-codegen.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2FDJetCloud%2Fdjet-codegen?ref=badge_large)
