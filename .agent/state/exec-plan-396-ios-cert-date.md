<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan 396

Milestone ativo: nenhum; milestone 5 concluído. ExecPlan 396 concluído.

Último commit lógico: `docs(sdk,ios): document certificate date behavior` (HEAD do milestone 5).

Caminhos alterados no milestone 5: `TotalCrossSDK/src/test/java/totalcross/sys/IOSCertDateArtifactRuntimeTest.java`, `.agent/reports/396-settingsioscertdate-is-empty-editorial.md`, `.agent/exec-plan-396-ios-cert-date.md`, `.agent/state/exec-plan-396-ios-cert-date.md` e `.agent/evidence/396-settingsioscertdate-is-empty.jsonl`.

Validações executadas em `TotalCrossSDK`: `./gradlew-agent test --tests totalcross.sys.IOSCertDateRuntimeParametersTest --warning-mode=none --console=plain`, `./gradlew-agent test --tests tc.IOSCertDateDeploymentTest --warning-mode=none --console=plain`, `./gradlew-agent test --tests tc.tools.deployer.IOSCertDatePolicyTest --warning-mode=none --console=plain` e `./gradlew-agent dist -x test --warning-mode=none --console=plain`. Todas passaram; logs compactos em `agent-logs/20260724-170800-test-agent.log`, `agent-logs/20260724-170822-test-agent.log`, `agent-logs/20260724-170927-test-agent.log` e `agent-logs/20260724-170834-dist-agent.log`.

Validação executada: em `TotalCrossSDK`, `./gradlew-agent test --tests totalcross.sys.IOSCertDateArtifactRuntimeTest --warning-mode=none --console=plain`. Passou; gerou o TCZ, extraiu `tcparms.bin`, executou o loader e confirmou o caso sem chave; resumo em `agent-logs/20260724-171548-test-agent.log`.

Validações não executadas: IPA signing/on-device iOS, porque `TotalCrossSDK/dist/vm/ios/TotalCross.ipa` não existe no checkout; full distribution build permanece desnecessário para este encerramento.

Decisões e descobertas finais: `Settings.iosCertDate` mantém a semântica histórica de expiração do provisioning profile; `tcparms.bin` é carregado por `Settings.loadDeploymentParameters()` via `Vm.getFile`; ausência/erro resulta em `null`; o estado iOS é limpo no início de cada `Deploy`; o harness final confirmou o TCZ/runtime; `TotalCrossVM` não foi alterado.

Fora de escopo deliberado: `TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java` e `TotalCrossVM`; IPA signing/on-device iOS por ausência do template; artefatos gerados, incluindo `TotalCrossSDK/IOSDateFixture.tcz` e `TotalCrossSDK/IOSDateRuntimeFixture.tcz`, não foram commitados.

Comando de inspeção: `git switch fix/396-settingsioscertdate-is-empty && git status --short -- TotalCrossSDK/src/test/java/tc/IOSCertDateDeploymentTest.java .agent`.
