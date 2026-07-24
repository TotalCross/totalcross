<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan 396

Milestone ativo: nenhum; milestone 4 concluído. Próximo milestone: 5, executar o smoke test final do artefato e runtime iOS.

Último commit lógico: `fix(deploy,ios): complete certificate date loading` (HEAD do milestone 4).

Caminhos alterados no milestone 4: `TotalCrossSDK/src/main/java/tc/Deploy.java`, `TotalCrossSDK/src/main/java/tc/tools/deployer/Deployer4IPhoneIPA.java`, `TotalCrossSDK/src/main/java/totalcross/sys/Settings.java`, `TotalCrossSDK/src/main/java/totalcross/ui/MainWindow.java`, `TotalCrossSDK/src/test/java/tc/tools/deployer/IOSCertDatePolicyTest.java`, `TotalCrossSDK/src/test/java/totalcross/sys/IOSCertDateRuntimeParametersTest.java`, `.agent/exec-plan-396-ios-cert-date.md`, `.agent/state/exec-plan-396-ios-cert-date.md` e `.agent/evidence/396-settingsioscertdate-is-empty.jsonl`.

Validações executadas em `TotalCrossSDK`: `./gradlew-agent test --tests totalcross.sys.IOSCertDateRuntimeParametersTest --warning-mode=none --console=plain`, `./gradlew-agent test --tests tc.IOSCertDateDeploymentTest --warning-mode=none --console=plain`, `./gradlew-agent test --tests tc.tools.deployer.IOSCertDatePolicyTest --warning-mode=none --console=plain` e `./gradlew-agent dist -x test --warning-mode=none --console=plain`. Todas passaram; logs compactos em `agent-logs/20260724-170800-test-agent.log`, `agent-logs/20260724-170822-test-agent.log`, `agent-logs/20260724-170927-test-agent.log` e `agent-logs/20260724-170834-dist-agent.log`.

Validações adiadas: smoke deployment iOS final, confirmação visual/observável no runtime empacotado e eventual full distribution build pertencem ao milestone 5 e não foram executadas.

Decisões e descobertas ativas: `Settings.iosCertDate` mantém a semântica histórica de expiração do provisioning profile; `tcparms.bin` é carregado por `Settings.loadDeploymentParameters()` via `Vm.getFile`; ausência/erro resulta em `null`; o estado iOS é limpo no início de cada `Deploy`; `TotalCrossVM` não foi alterado.

Fora de escopo deliberado: `TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java` e `TotalCrossVM`; smoke deploy final e artefatos gerados, incluindo `TotalCrossSDK/IOSDateFixture.tcz`, não foram incluídos.

Comando de retomada: `git switch fix/396-settingsioscertdate-is-empty && git status --short -- TotalCrossSDK/src/test/java/tc/IOSCertDateDeploymentTest.java .agent`.
