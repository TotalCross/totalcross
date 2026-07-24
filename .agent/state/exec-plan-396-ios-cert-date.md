<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan 396

Milestone ativo: nenhum; milestone 3 concluído. Próximo milestone: 4, completar serialização, leitura e isolamento do estado entre deploys.

Último commit lógico: `fix(deploy,ios): resolve certificate date before conversion` (HEAD do milestone 3).

Caminhos alterados no milestone 3: `TotalCrossSDK/src/main/java/tc/Deploy.java`, `TotalCrossSDK/src/main/java/tc/tools/deployer/Deployer4IPhoneIPA.java`, `TotalCrossSDK/src/test/java/tc/IOSCertDateDeploymentTest.java`, `.agent/exec-plan-396-ios-cert-date.md`, `.agent/state/exec-plan-396-ios-cert-date.md` e `.agent/evidence/396-settingsioscertdate-is-empty.jsonl`.

Validação executada: em `TotalCrossSDK`, `./gradlew-agent test --tests tc.IOSCertDateDeploymentTest --warning-mode=none --console=plain`. Passou; confirmou a presença de `iosCertDate` em `tcparms.bin` e a idempotência da descoberta; resumo em `TotalCrossSDK/agent-logs/20260724-170006-test-agent.log`.

Validações adiadas: leitura de `tcparms.bin` no runtime, isolamento entre execuções, build amplo do SDK e smoke deploy iOS pertencem aos milestones posteriores e não foram executados.

Decisões e descobertas ativas: `Settings.iosCertDate` mantém a semântica histórica de expiração do provisioning profile; profile ausente, expiração ausente ou conversão inválida resultam em `null`; a descoberta agora ocorre antes de `J2TC.process()` apenas para iOS; `iosKeystoreInit()` e o empacotamento continuam depois da conversão; o runtime ainda não tem parser de `tcparms.bin` nem atribuição a `Settings.iosCertDate`.

Fora de escopo deliberado: `TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java` e `TotalCrossVM` neste milestone; carregamento no runtime e isolamento completo de estado ficam para o milestone 4; artefatos gerados, incluindo `TotalCrossSDK/IOSDateFixture.tcz`.

Comando de retomada: `git switch fix/396-settingsioscertdate-is-empty && git status --short -- TotalCrossSDK/src/test/java/tc/IOSCertDateDeploymentTest.java .agent`.
