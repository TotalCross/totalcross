# Contribution guidelines

## Table of contents

- [Kick-Off](#kick-off)
  - [Language](#language)
    - [Be accessible](#be-accessible)
  - [Be part of the community](#be-part-of-the-community)
  - [Code of Conduct](#code-of-conduct)
  - [Terms agreement](#terms-agreement)
- [Being a contributor](#being-a-contributor)
  - [Documentation](#documentation)
  - [Issues](#issues)
    - [Submitting an issue](#submitting-an-issue)
  - [Feedback](#feedback)
  - [Code](#code)
    - [Environment](#environment)
  - [Commit Message Guidelines](#commit-message-guidelines)
  - [Why all these rules?](#why-all-these-rules)
- [Submitting a pull request](#submitting-a-pull-request)

## Kick-Off

Thank you for being interested and wanting to contribute to TotalCross!

In this document we want show to you our set of instructions and guidelines to reduce misunderstandings and make the process of contributing to `TotalCross/totalcross` as cool as we can.

### Language

To reach the largest number of people, please, while contributing or interacting in any way in this project, always use **English**.

#### Be accessible

Let's add more people to our project!

1. Try to use simple words and sentences. Don't make fun of non-native English speakers if you find something wrong about the way they express themselves;

2. Try to be comprehensive about other's English level;

3. Try to encourage newcomers to express their opinions, and make them comfortable enough to do so.

### Be part of the community

Help newcomers and find people with the same interest in TotalCross in [our Telegram community](https://t.me/totalcrosscommunity)! Feel free to answer / ask questions and grow our community!

### Code of Conduct

To be a contributor you must comply with these terms. Check the [full text](CODE_OF_CONDUCT.md).

### Terms agreement

If you want to made a **code contribuiting**, you will need to:
1. Download the [TotalCross Contributor Agreement](https://drive.google.com/file/d/1ui1QXQq785ejaaXVs0R7F4R-1FYbKLtE/view?usp=sharing);
2. Fill out the form and send it to [devteam@totalcross.com](mailto:devteam@totalcross.com);
3. And if you already made your pull request, just need to wait to merge!

## Being a contributor

There are several ways to contribute to our project today.

### Documentation

Are you already a user of `TotalCross/totalcross`? You can help us improve our documentation!

Typos, lack of examples and / or explanation, errors and so on, are just some examples of things that could be fixed and / or improved. Including this file!

> Convention: be simple and clear.

[See our documentation!](https://learn.totalcross.com/)

### Issues

To be easier to understand and therefore easier to resolve, some standards must be followed.

#### Submitting an issue

Some standards and actions expected when submitting an issue are:
- Please search for similar issues before opening a new one;
- Use one of the corresponding issue templates;
- Use a title that summarizes the issue in 1 line;
- Include as much information as possible;
- If necessary, modify the template topics;
- Logs! Gather error information!

### Feedback

We often don't see what we are doing good or bad. Your opinion is very necessary to build the best we can do!

The [_feedback_](https://github.com/TotalCross/totalcross/labels/feedback) and [_question_](https://github.com/TotalCross/totalcross/labels/question) labels are the best places to do this!

### Code

Your code is welcome! For code issues look for labels:

- The [_feature_](https://github.com/TotalCross/totalcross/labels/feature) to request an feature;
- The [_enhancement_](https://github.com/TotalCross/totalcross/labels/enhancement) to request improve a existing one feature;
- The [_bug_](https://github.com/TotalCross/totalcross/labels/bug) to request an fix.

You can also add other modifiers for the target platform.

The [_good first issue_](https://github.com/TotalCross/totalcross/labels/good%20first%20issue) and [_help wanted_](https://github.com/TotalCross/totalcross/labels/help%20wanted) are special use labels.

When an issue has already been assigned try not to work on it to avoid conflicts.

#### Environment

To develop TotalCrossVM you need to have in your system:
- Git;
- Gradle;
- Java JDK 1.8.

If you going to build our VM you'll need:
- C/C++ compiler like Clang or GCC;
- CMake;

To build TotalCross for each system below, you will need::

| Linux | Linux ARM | Android | iOS | MacOS | Win32 | WinCE |
|---|---|---|---|---|---|---|
| Docker | Docker | NDK 21 | XCode and CocoaPods 1.10.0.beta.1 | XCode | MSVC _X_|MSVC 9 |

With these prerequisites, you may need to clone this repository, as well as have a ready SDK and a [sample code](https://github.com/TotalCross/hello-world) to get started.

### Commit Message Guidelines

To keep the project history clear, consistent, and easy to maintain, all commits must follow the rules below.<br>
This project follows a Conventional Commits–inspired format,
adapted to reflect internal subsystems rather than release semantics.<br>
> [!WARNING]
> Commit messages are automatically validated by GitHub Actions.

#### Commit message format
```
<type>(<scope>[,<platform>][,<arch>]): short description

optional body
```

**Example**
```
fix(socket,posix): handle eof correctly

Handle eof correctly in the posix socket path to avoid spurious
connection failures during shutdown.
```

#### Commit title rules
The first line of the commit message (the title):

- Must start with a lowercase letter
- Must contain at least 3 words (to avoid vague titles such as "fix bug")
- Must be at most 80 characters long
- Must use the imperative mood (e.g. fix, add, remove)
- Must follow the format <type>(<scope>[,<platform>][,<arch>]): description
- Must not end with a period
- If a commit body is present, it must be separated from the title by a blank line.

These rules are enforced automatically by CI.

#### Commit types

Use the following commit types to describe the primary intent of the change.
Choose the most specific type that matches the nature of the change.

> [!IMPORTANT]
> Each commit should represent a single logical change.
> Avoid mixing refactors, fixes, and formatting in the same commit.

##### Core and tooling
| Type       | Description                                                                                                             |
| ---------- | ----------------------------------------------------------------------------------------------------------------------- |
| `vm`       | Changes in the virtual machine, bytecode interpreter, garbage collector, memory management, or native runtime behavior. |
| `runtime`  | Cross-platform runtime behavior shared across platforms (event loop, threading, system services).                       |
| `sdk`      | TotalCross Java SDK, public APIs, UI components, or standard libraries.                                                 |
| `compiler` | Compiler, bytecode generation, parsing, optimization passes, or code transformation logic.                              |
| `tools`    | Developer tools such as packagers, CLI utilities, or internal automation tools.                                         |
| `build`    | Build system configuration (CMake, Gradle, scripts, CI, cross-compilation).                                             |
| `perf`     | Performance improvements without functional changes (CPU, memory, allocations).                                         |
| `fix`      | Bug fixes where the primary distinction is correctness rather than build or refactor intent.                            |
| `refactor` | Code restructuring without changing external behavior.                                                                  |
| `test`     | Adding, updating, or fixing tests (unit, integration, regression).                                                      |
| `doc`      | Documentation, examples, comments, or changelog updates.                                                                |
| `chore`    | Maintenance tasks such as cleanup, formatting, or non-functional changes.                                               |

> [!TIP]
> Use `fix` for correctness bugs, even if they incidentally improve performance.
> Prefer expressing the affected area in the scope rather than inventing new types.

#### Scope, platform, and architecture

Inside the parentheses, qualifiers are positional and must follow this order:

1. `scope` (required): the primary subsystem or area
2. `platform` (optional): the operating system or target environment
3. `arch` (optional): the architecture or target variant

Examples:
- `fix(socket,posix): ...`
- `sdk(camera,android): ...`
- `build(cmake,windows,x86): ...`

##### Common scopes

Scopes should be short, stable subsystem names. Examples:
- `gc`
- `bytecode`
- `socket`
- `ssl`
- `camera`
- `ui`
- `json`
- `jpeg`
- `cmake`
- `packager`
- `deploy`

##### Common platforms

Use a platform qualifier when the change is mainly specific to that target:

| Platform  | Description                                                                                  |
| --------- | -------------------------------------------------------------------------------------------- |
| `android` | Android-specific code, packaging, lifecycle, NDK/JNI integration.                            |
| `ios`     | iOS-specific code, packaging, native bindings, platform integration.                         |
| `macos`   | macOS-specific changes not tied to a single macOS architecture.                              |
| `windows` | Windows-specific changes not tied to a single Windows target.                                |
| `wince`   | Windows CE-specific behavior, compatibility, or toolchain changes.                           |
| `winmo`   | Windows Mobile-specific behavior, integration, or packaging.                                 |
| `linux`   | Linux-specific behavior not tied to a single Linux architecture.                             |
| `posix`   | Shared POSIX behavior spanning Linux, macOS, Android, iOS, or other POSIX-like targets.     |

##### Common architectures

Use an architecture qualifier only when it materially narrows the target:

| Architecture | Description |
| ------------ | ----------- |
| `x86`        | 32-bit x86  |
| `x64`        | x86-64      |
| `arm`        | 32-bit ARM  |
| `arm64`      | 64-bit ARM  |

**Qualified commit examples**
```
runtime(gc): fix invalid mark on promoted objects
fix(socket,posix): handle eof correctly
sdk(camera,android): add video resolution query
build(cmake,windows,x86): fix static png linking
build(toolchain,wince,arm): adjust errno compatibility
build(deploy,android): add custom keystore support
```
> [!IMPORTANT]
> The first qualifier must always be the subsystem scope.
> Use the optional platform and architecture qualifiers only when they
> make the history materially clearer.

> Prefer multiple small commits over a single broad one when platform or
> architecture behavior differs.

#### Commit body guidelines
The commit body is optional, but encouraged when:
- the change is non-trivial
- the reasoning is not obvious from the diff
- there are trade-offs or side effects

Guidelines:
- Wrap lines at 80 characters
- Explain why the change was made, not just what changed
- Reference issues when applicable (e.g. closes #123)

> [!TIP]
> Use the commit body to capture reasoning, constraints,
> and rejected alternatives when relevant.

Language and consistency
- All commit messages must be written in English
- Use technical and precise language
- Avoid vague titles such as:
  ```
  fix bug
  update files
  changes
  ```

Example body
```
Fixes an invalid GC mark when objects are promoted
during minor collection.

This prevents rescanning promoted objects and avoids
invalid memory access under heavy allocation.
```

#### Commit template (recommended)
Developers are encouraged to use the provided commit message template to avoid CI failures and keep consistency:
```
<type>(<scope>): short description (lowercase, ≤ 80 chars)

Optional body:
- explain what changed
- explain why it changed
- reference issues if applicable
```
You can enable it locally with:
```
git config commit.template .gitmessage.txt
````

Following these guidelines ensures a clean, searchable history and helps maintain the long-term stability of the TotalCross codebase.

### Why all these rules?

We try to enforce these rules for the following reasons:

- Communicate in a better way the nature of changes;ly
- Automatically determining a semantic version bump (based on the types of commits);
- Make it easier for people to contribute, by allowing them to explore a more structured commit history.

## Submitting a pull request

Before submitting a pull request, please make sure the following is done:

- Look for another pull request (even draft) that solves your problem;
- [Fork](https://help.github.com/en/articles/fork-a-repo) the repository and create your branch from `master`.
  - Example: `feature/my-awesome-feature` or `fix/annoying-bug`;
- Build your changes;
- If you’ve fixed a bug or added code that should be tested, **add tests**;
- Ensure your commit is validated;

### What happens next?

After your pull request has been submitted, TotalCross code maintainers will acknowledge it as soon as possible. We aim to do it in three days tops.

TotalCross code maintainers hold backlog grooming meetings most Fridays. Your pull request will likely be discussed then and you should get a position on where it stands on TotalCross backlog the following Monday.

| Action | Action deadline |
|---|---|
| Pull request committed | -  |
| First reply | 3 days  | 
| Backlog prioritisation feedback | 7 days  | 
