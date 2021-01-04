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
- [Commiting](#commiting)
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

## Commiting

Our commit style:

```
<type>: <subject>

[optional body]

[optional footer]
```

About our standard:
- The header (first line) is the only mandatory part of the commit message;
- The body and footer are both optional but its use is highly encouraged;
> The blank line separating the header from the body is critical, same for footer.
- The header should contains:
  - A type:
    - Must be lowercase;
  - A subject:
    - Must be non capitalized;
    - Must omit any trailing punctuation.
  - Must be limited to 80 characters or less (length = type + subject);
- The body:
  - Must have a leading blank line;
  - Each line must be limited to 80 characters or less.
- The footer:
  - Must have a leading blank line;
  - Each line must be limited to 80 characters or less;
  - If needed, reference to issues and pull requests must be made here in the last line.

You also should follow these general guidelines when committing:

- Use the present tense ("remove feature" not "removed feature");
- Use the imperative mood ("resize object to..." not "resizes object to...");
- Try to answer the following questions:
  - What is the reason for this change?
  - What side effects (if any) does this change may have?

Example of commit message:
```
doc: summarize changes in around 80 characters or less

More detailed explanatory text, if necessary. Wrap it to about 80
characters per line. In some contexts, the first line is treated as
the subject of the commit and the rest of the text as the body. The
blank line separating the header from the body is critical (unless
you omit the body entirely); various tools like `log`, `shortlog`
and. 
Explain the problem that this commit is solving. Focus on why you
are making this change as opposed to how (the code explains that).
Are there side effects or other unintuitive consequences of this
change? Here's the place to explain them.

Closes #123
```

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
