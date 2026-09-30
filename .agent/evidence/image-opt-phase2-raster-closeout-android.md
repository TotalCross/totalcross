<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 2 raster closeout Android validation

Status under ExecPlan 03: `DEFERRED — no matching runtime was preinstalled;
the plan forbids building Android during this execution`.

The available physical device was reachable:

    192.168.1.154:43289 device
    Xiaomi 2312DRA50G, Android 13, MIUI V14.0.9.0.TNRMIXM
    Qualcomm Adreno (TM) 710, OpenGL ES 3.2, driver V@0615.73

For the user's retry, a branch-matching diagnostic artifact was built and
deployed temporarily. The final release APK was produced from HEAD
`70aa29e8a367343146fd8ade9931a11038ef77d9` with the normal GPU Android
configuration (no `TC_GRAPHICS_SOFTWARE`), then installed as
`totalcross.appp2gd` version `1.0.0` and cleaned up afterward.

    APK SHA-256: 06e87607e3103b93f40e4fcc3d5b24389a6574674a934adcd7cf09c25e4f59ac
    libtcvm.so SHA-256: 780e50a6875c8484247618e026b96a2518b541e6f6396af773090386e2013717

The direct workload ran three warmups and one measured pass. Its alert
reported stable source/pixel/color/identity/variant hashes:

    jpeg=0000F4870000A84E png=0000900B0000BBA9
    pixels=00006C3E000043B0 encoded=0000A7F20000A4D3
    color=00002DBA0000C894 identity=000000D600000165
    variant=000000D600000165

GPU-negative counters were all zero:

    write_attempts=0 write_hits=0 write_fallbacks=0
    target_attempts=0 target_materializations=0 target_hits=0
    variant_lookups=0 variant_hits=0 variant_misses=0
    variant_materializations=0

The same report had `row_readbacks=1936`, `full_readbacks=0`, and
`direct_color=1`; the row reads and direct color operation belong to the
explicit canonical readback/APPLY_COLOR2 parts of the workload, not a
draw-only raster attempt. It also reported `decode_zero_copy=2`,
`opacity_source=1`, and `opacity_decode=1`. No Android timing or RSS claim is
made.

Because this positive retry required invoking Android builds, it is retained
as diagnostic evidence only and does not replace the build-constrained
deferral required by ExecPlan 03. The final plan classification therefore
remains explicitly deferred rather than treating this out-of-policy retry as
acceptance evidence.
