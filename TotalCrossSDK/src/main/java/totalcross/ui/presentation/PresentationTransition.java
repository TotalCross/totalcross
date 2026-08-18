// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.presentation;

import totalcross.ui.anim.ControlAnimation;

interface PresentationTransition {
  ControlAnimation start(PresentationHandle handle, boolean entering, int duration, Runnable finished);
}
