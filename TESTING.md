## Objective

This document aims to sumarize what needs to be tested whenever a new version is launched.

## Target devices and operational systems

The following devices must be tested with the following OS:

|   |Toradex Base (Angstrom)| Yocto 32 (TotalCross Meta)| YOCTO 64 (TotalCross Meta) | Raspberry OS | Ubuntu (latest LTS) | Win 32 and 64 | Android (v5 and v10 - latest) | iOS |
|---|---|---|---|---|---|---|---|---|
|Toradex iMX6 ULL no GPU| x | x |   |  |   |   |   |   |
|Toradex iMX6 DL with GPU| x | x |   |  |   |   |   |   |
|Raspberry 4|  |  | x  | x |   |   |   |   |
|Raspberry 3 B+|  | x |   | x |   |   |   |   |
|Intel device (PC)|  |  |   |  | x  | x  |   |   |
|Android device|  |  |   |  |   |   |  x |   |
|iPhone|   |  |   |  |   |   |   | x |

## Sample applications

Each target device x OS marked on the previous table will be tested with the following applications:

1. [TotalCross Sample](https://github.com/TotalCross/tc-sample)
2. [Printer](https://github.com/TotalCross/embedded-samples/tree/main/printer-application)
3. [Bad Apple](https://github.com/TotalCross/embedded-samples/tree/main/bad-apple)

The device should run using CPU only (not using the GPU when it is available).
A manual integration standard test is to be outlined for each of the applications mentioned. 
