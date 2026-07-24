<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan 396

Milestone ativo: nenhum; milestone 2 concluído. Próximo milestone: 3, separar descoberta de metadata iOS e corrigir a ordem antes de `J2TC.process()`.

Último commit lógico: `fix(deploy,ios): preserve provisioning profile expiration` (HEAD do milestone 2).

Caminhos alterados no milestone 2: `TotalCrossSDK/src/main/java/tc/tools/deployer/Deployer4IPhoneIPA.java`, `TotalCrossSDK/src/main/java/totalcross/sys/Settings.java`, `TotalCrossSDK/src/test/java/tc/tools/deployer/IOSCertDatePolicyTest.java`, `.agent/exec-plan-396-ios-cert-date.md`, `.agent/state/exec-plan-396-ios-cert-date.md` e `.agent/evidence/396-settingsioscertdate-is-empty.jsonl`.

Validação executada: em `TotalCrossSDK`, `./gradlew-agent test --tests tc.tools.deployer.IOSCertDatePolicyTest --warning-mode=none --console=plain`. Passou; resumo em `TotalCrossSDK/agent-logs/20260724-165231-test-agent.log`.

Validações adiadas: `Deploy.java`, ordem de `J2TC.process()`, serialização do TCZ, leitura no runtime, isolamento entre execuções, build amplo do SDK e smoke deploy iOS pertencem aos milestones posteriores e não foram executados.

Decisões e descobertas ativas: `Settings.iosCertDate` mantém a semântica histórica de expiração do provisioning profile; profile ausente, expiração ausente ou conversão inválida resultam em `null`; a data ainda é descoberta depois de `J2TC.process()` na baseline; o runtime não tem parser de `tcparms.bin` nem atribuição a `Settings.iosCertDate`.

Fora de escopo deliberado: `TotalCrossSDK/src/main/java/tc/Deploy.java` e `J2TC.java` neste milestone; `TotalCrossVM`, sem alteração até o milestone 4; artefatos gerados, incluindo `TotalCrossSDK/IOSDateFixture.tcz`.

Comando de retomada: `git switch fix/396-settingsioscertdate-is-empty && git status --short -- TotalCrossSDK/src/test/java/tc/IOSCertDateDeploymentTest.java .agent`.
