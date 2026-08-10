<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Harden the J2TC/TCM Boundary

## Baseline

At revision `441c5785`, TCM was default-off only at publication. On the fixed
aggregate modern-Java conversion, both `NONE` and `AOT` built metadata for 87
classes, 306 methods, 2,767 bytecode/origin sites, 681 calls, 73 synthetic
lowerings, and 108 StackMap frames. This structural equality is the primary
baseline evidence that ordinary deploys still paid the collection cost.

Three warmups and ten samples gave median in-process deploy times of 3.604 s for
`NONE` and 3.587 s for `AOT`. These timings characterize only this workload and
environment; they do not establish a universal performance difference. The
baseline TCM v1 fixture SHA-256 is
`ee07c01ddcf503044c58ac702ddf1e750c55212f919e59f0d73f51478938b965`.

Hardening implementation has not started at this checkpoint, so no delivered
boundary improvement is claimed yet.
