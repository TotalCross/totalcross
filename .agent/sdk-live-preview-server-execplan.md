<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Histórico e extração do servidor de Live Preview do SDK

Este ExecPlan é um documento vivo e deve ser mantido de acordo com .agent/PLANS.md.

## Purpose / Big Picture

Este plano registrou a prova inicial de `totalcross.preview.PreviewServer` no SDK. Essa fronteira foi substituída pela extração para `totalcross-tooling/live-preview-server`: o SDK agora contém somente o contrato específico `totalcross.preview.PreviewRuntime` e seu `FrameConsumer` aninhado, enquanto o artefato de tooling fornece HTTP, JSON, classloader e reload.

## Progress

- [x] (2026-07-17 00:04Z) Identificados PreviewServer, PreviewRunner, LauncherRuntime, classes totalcross.preview e testes candidatos no checkout.
- [x] (2026-07-17 00:20Z) Removidas as exclusões de LauncherRuntime e totalcross.preview dos source sets main e test; compileJava passou.
- [x] (2026-07-17 00:25Z) Adicionado teste de processo PreviewServer e corrigido o ciclo de vida de shutdown; cinco testes totalcross.preview passaram.
- [x] (2026-07-17 00:25Z) Construído totalcross-sdk-7.2.2.jar, confirmadas classes de preview e provados /health, /frame e /shutdown contra fixture temporária.
- [x] (2026-07-17 00:25Z) Atualizado o relatório editorial com a evidência observada; resta somente commitar os arquivos de escopo.
- [x] (2026-07-17 01:26Z) Extraídos servidor, runner, configuração, classloader e superfícies headless para totalcross-tooling no commit bc751b88a.
- [x] (2026-07-17 01:36Z) Consolidado `PreviewSurface` em `PreviewRuntime.FrameConsumer` (c6c93b57b) e completado o contrato para consumo externo sem `LauncherRuntime` (c7f301696).

## Surprises & Discoveries

- Observation: sourceSets.main exclui exatamente as novas classes totalcross/preview/** e totalcross/LauncherRuntime.java.
  Evidence: TotalCrossSDK/build.gradle linhas 61 a 69.

- Observation: PreviewRunner, AppletPreviewSurface, HeadlessPreviewSurface e package.html já possuem mudanças locais preparadas, enquanto a maior parte das classes e testes de preview ainda não está versionada.
  Evidence: git diff --cached e git status --short restritos a TotalCrossSDK/src/main/java/totalcross/preview e TotalCrossSDK/src/test/java/totalcross/preview.

- Observation: HttpServer aceita conexões depois de start(), mas a JVM ainda pode terminar se a thread main retornar antes de existir um executor não daemon atendendo requisições.
  Evidence: a primeira prova imprimiu TOTALCROSS_PREVIEW_URL mas curl recebeu connection refused. Após bloquear a main thread com CountDownLatch, /health respondeu 200.

- Observation: parar HttpServer não encerra por si só as threads AWT iniciadas pelo runtime de preview.
  Evidence: a primeira versão do teste PreviewServerTest recebeu 200 em /shutdown, mas o processo não saiu em dez segundos. System.exit(0) depois do latch encerrou o processo e o teste passou.

## Decision Log

- Decision: substituir a publicação de PreviewServer no jar principal por um contrato SDK e um artefato companion em totalcross-tooling.
  Rationale: o primeiro empacotamento provou o protocolo, mas HTTP, JSON, classloading e reload pertencem à ferramenta de IDE. `PreviewRuntime` mantém no SDK apenas a integração necessária com o launcher.
  Date/Author: 2026-07-17 / Codex.

- Decision: validar o serviço com uma fixture Java mínima compilada no diretório temporário e com curl para /health.
  Rationale: isso prova tanto a presença das classes no jar quanto a criação real do servidor HTTP sem depender do VS Code.
  Date/Author: 2026-07-17 / Codex.

- Decision: manter a thread main de PreviewServer bloqueada até /shutdown e chamar System.exit(0) após a confirmação.
  Rationale: PreviewServer é uma ferramenta de linha de comando com ciclo de vida próprio. O latch impede saída prematura; o exit elimina threads AWT residuais e torna Stop Preview determinístico para a extensão.
  Date/Author: 2026-07-17 / Codex.

## Outcomes & Retrospective

O empacotamento inicial de PreviewServer no SDK foi provado e serviu de base para a extração. No estado final, o JAR SDK não contém `PreviewServer`, `PreviewRunner`, configuração, classloader ou superfícies headless; c6c93b57b removeu o contrato paralelo `PreviewSurface`, e c7f301696 deixa o novo servidor depender apenas de `PreviewRuntime`. A prova HTTP e a distribuição agora vivem em `totalcross-tooling/live-preview-server`.

## Editorial Report

### Editorial Summary

Este trabalho transformou classes locais de preview em uma superfície distribuível do SDK. Clientes de IDE podem iniciar um serviço HTTP local a partir de totalcross-sdk.jar, verificar sua saúde, obter uma imagem PNG e encerrá-lo de forma determinística.

### Original Plan versus Actual Outcome

A execução incluiu mais que a remoção das exclusões: a primeira prova mostrou que o processo saía antes da requisição, e o primeiro teste revelou que AWT mantinha a JVM viva após shutdown. O resultado final bloqueia até /shutdown e encerra o processo explicitamente.

### What Changed

TotalCrossSDK/build.gradle inclui o runtime e o pacote preview nos source sets. totalcross.preview.PreviewServer usa CountDownLatch para viver até /shutdown. PreviewRunner, LauncherRuntime, superfícies, classloader descartável e configuração tornam a prévia recarregável. PreviewServerTest e PreviewMainWindow cobrem o contrato por subprocesso.

### Decisions and Trade-offs

O jar principal é escolhido para reduzir configuração e incompatibilidades entre artefatos. Isso aumenta seu conteúdo, mas PreviewRunner já pertence ao SDK e o benefício é uma integração de IDE mais simples.

### Unexpected Problems and Discoveries

As exclusões de source set impediam a compilação mesmo com arquivos presentes. Além disso, HttpServer sozinho não mantinha a JVM viva e o shutdown inicial não terminava as threads AWT; ambos foram demonstrados e corrigidos.

### Validation and Measurable Results

Observado: ./gradlew-agent compileJava passou em 16 segundos; ./gradlew-agent test --tests 'totalcross.preview.*' passou com cinco testes; ./gradlew-agent jar passou. jar tf build/libs/totalcross-sdk-7.2.2.jar encontrou LauncherRuntime, PreviewRunner, DisposableAppClassLoader, PreviewConfig e PreviewServer. A prova manual iniciou o jar com uma MainWindow de 120x180 e observou /health 200, /frame 200 com PNG de 487 bytes, /shutdown 200 e término do processo.

### Useful Evidence and Examples

Evidência: agent-logs/20260716-212021-compileJava-agent.log, agent-logs/20260716-212502-test-agent.log e agent-logs/20260716-212525-jar-agent.log; build/libs/totalcross-sdk-7.2.2.jar; e a fixture temporária /tmp/totalcross-preview-e2e.WsbL6M, que não será versionada.

### Limitations, Remaining Work, and Open Questions

A prévia continua local e somente de leitura. Este plano não adiciona input remoto, integração VS Code nem publicação Maven; a extensão consome o jar no ExecPlan separado.

### Possible Article Angles

Como transformar um protótipo de preview local em contrato distribuível de SDK para ferramentas de IDE.

### Suggested Narrative

Mostrar as exclusões de build, a decisão de empacotar o servidor, a prova HTTP contra uma fixture e o contrato consumido pela extensão.

### Claims Requiring Human Review

A escolha de expor PreviewServer no jar principal e a versão em que ele será publicado precisam de revisão de release normal. Nenhuma alegação de compatibilidade além de JDK 17 e da prova local foi verificada.

## Context and Orientation

TotalCrossSDK/build.gradle configura o jar totalcross-sdk. O source set main exclui totalcross/preview/** e totalcross/LauncherRuntime.java; isso deixa PreviewServer fora do jar mesmo que os arquivos existam. PreviewServer lê totalcross.preview.json, cria PreviewRunner e expõe /health, /frame, /show, /clear, /reload e /shutdown em loopback. PreviewRunner depende de LauncherRuntime e das superfícies em totalcross.preview.

Os testes de configuração e classloader estão em TotalCrossSDK/src/test/java/totalcross/preview. A execução segura do Gradle deve usar TotalCrossSDK/gradlew-agent, que grava saída completa em agent-logs e fornece um resumo compacto.

## Plan of Work

Alterar TotalCrossSDK/build.gradle para deixar LauncherRuntime e totalcross.preview no source set main. Manter exclusões que não pertencem ao preview. Compilar primeiro as classes Java, corrigindo somente incompatibilidades causadas por promover esse código. Adicionar ou ajustar testes de PreviewConfigLoader, DisposableAppClassLoader e PreviewServer conforme a falha observada. Construir jar, confirmar a presença das classes e iniciar o servidor com uma fixture de MainWindow configurada em diretório temporário. Não incluir recursos, logs, artefatos ou mudanças de SDK não necessárias para esse caminho.

## Concrete Steps

1. Em /Users/flsobral/repos/totalcross-github, executar:

       git status --short -- TotalCrossSDK/build.gradle TotalCrossSDK/src/main/java/totalcross/PreviewRunner.java TotalCrossSDK/src/main/java/totalcross/LauncherRuntime.java TotalCrossSDK/src/main/java/totalcross/preview TotalCrossSDK/src/test/java/totalcross/preview
       cd TotalCrossSDK
       ./gradlew-agent compileJava --warning-mode=none --console=plain

   Esperado: a primeira compilação revela todas as dependências que as exclusões escondiam. Preservar o resumo em agent-logs e registrar erros relevantes.

2. Remover as duas exclusões de preview do source set main e executar novamente compileJava. Corrigir somente erros necessários e então executar:

       ./gradlew-agent test --tests 'totalcross.preview.*' --warning-mode=none --console=plain
       ./gradlew-agent jar --warning-mode=none --console=plain
       jar tf build/libs/totalcross-sdk-*.jar | rg 'totalcross/(PreviewRunner|LauncherRuntime)\\.class|totalcross/preview/PreviewServer\\.class'

   Esperado: compileJava, testes focados e jar passam; a listagem encontra as classes.

3. Criar uma cópia de fixture e um totalcross.preview.json em diretório temporário seguro, iniciar o servidor usando o jar recém-gerado e consultar /health. O comando deve usar classpath explícito para jar do SDK, dependências de runtime e classes compiladas da fixture. Desligar por POST /shutdown e registrar URL/HTTP observados.

4. Atualizar este plano, executar git diff --check, commitar somente os arquivos de preview e build necessários com mensagens convencionais e corpos que expliquem a distribuição e a prova.

## Validation and Acceptance

A aceitação exige três observações: compileJava termina com status zero; jar tf encontra PreviewServer, PreviewRunner e LauncherRuntime; e GET /health retorna JSON com ok:true para um PreviewServer iniciado pelo jar. Os testes Preview focados devem passar. A prova deve usar jar de build, não classes do diretório source.

## Idempotence and Recovery

compileJava, test e jar são repetíveis. A fixture fica sob diretório temporário criado por mktemp -d e pode ser removida após a prova. Se promoção das classes quebrar o jar, reverter somente o commit de preview ou restaurar o hunk revisado; nunca limpar ou descartar alterações fora dos caminhos deste plano.

## Artifacts and Notes

Registrar o caminho do log agent, os nomes de testes, a versão do jar e a resposta /health. Não commitar build/, dist/, agent-logs ou a fixture temporária.

## Interfaces and Dependencies

O artefato totalcross-sdk contém totalcross.preview.PreviewServer, totalcross.PreviewRunner, totalcross.LauncherRuntime e suas dependências de preview. PreviewServer aceita:

    --config <totalcross.preview.json> --host 127.0.0.1 --port <numero>

Ele deve imprimir TOTALCROSS_PREVIEW_URL=http://127.0.0.1:<porta> e fornecer GET /health com JSON que contém ok:true. A extensão VS Code usa esse contrato sem embutir classes Java.

Revision note (2026-07-17): criado para separar a distribuição do servidor SDK da integração TypeScript, preservando validação e commits independentes. Atualizado após compileJava, testes Preview, inspeção do jar e prova HTTP; documenta os defeitos de término prematuro e shutdown AWT encontrados e corrigidos.
