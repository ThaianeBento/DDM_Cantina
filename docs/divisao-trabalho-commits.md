# Divisao de trabalho e ordem de commits

Este documento separa o trabalho em tres partes para que tres pessoas consigam apresentar contribuicoes conectadas, com historico de commits claro.

A regra desta versao e simples: cada arquivo aparece apenas uma vez, no commit principal em que ele deve entrar. Quando um arquivo e usado por mais de uma funcionalidade, ele continua listado somente no commit em que foi criado ou em que faz mais sentido para a apresentacao.

## Ordem geral

1. Pessoa 1: ambiente, estrutura do app, banco local, modelos, API, repositorios, regras e notificacoes.
2. Pessoa 2: fluxo do usuario comum, login, inicio, cardapio, formulario, historico e acoes do proprio pedido.
3. Pessoa 3: area administrativa, impressao e documentacao final.

Essa ordem evita quebrar a apresentacao: primeiro entra a base tecnica, depois o uso pelo aluno, e por ultimo a administracao.

## Pessoa 1 - Base tecnica, dados e regras

### Responsabilidade

Preparar o projeto e implementar a fundacao usada pelas outras telas: Gradle, Compose, navegacao, Room, Retrofit, repositorios, regras de pedido, formatacao e notificacoes.

### Commits e arquivos

#### Commit 1 - chore(project): configure android kotlin compose project

Configuracao inicial do projeto Android e arquivos de ambiente.

- settings.gradle.kts
- build.gradle.kts
- app/build.gradle.kts
- gradle.properties
- gradlew
- gradlew.bat
- gradle/wrapper/gradle-wrapper.jar
- gradle/wrapper/gradle-wrapper.properties
- app/src/main/AndroidManifest.xml
- app/src/main/res/values/strings.xml
- app/src/main/res/values/themes.xml
- app/src/main/res/xml/backup_rules.xml
- app/src/main/res/xml/data_extraction_rules.xml
- app/src/main/res/drawable/ic_launcher.xml

#### Commit 2 - feat(app): add compose shell navigation and shared ui

Entrada do app, tema, rotas, componentes compartilhados e fabrica de ViewModels.

- app/src/main/kotlin/com/example/appcantina/MainActivity.kt
- app/src/main/kotlin/com/example/appcantina/AppCantinaApp.kt
- app/src/main/kotlin/com/example/appcantina/ui/navigation/Routes.kt
- app/src/main/kotlin/com/example/appcantina/ui/theme/AppTheme.kt
- app/src/main/kotlin/com/example/appcantina/ui/components/AppComponents.kt
- app/src/main/kotlin/com/example/appcantina/viewmodel/AppViewModelFactory.kt

#### Commit 3 - feat(data): add domain models

Modelos principais usados no app.

- app/src/main/kotlin/com/example/appcantina/data/model/AuthState.kt
- app/src/main/kotlin/com/example/appcantina/data/model/ConsumptionType.kt
- app/src/main/kotlin/com/example/appcantina/data/model/MealType.kt
- app/src/main/kotlin/com/example/appcantina/data/model/MenuCategory.kt
- app/src/main/kotlin/com/example/appcantina/data/model/OrderStatus.kt

#### Commit 4 - feat(data): add room database entities and daos

Banco local, entidades, DAOs e relacionamento dos pedidos.

- app/src/main/kotlin/com/example/appcantina/data/local/AppDatabase.kt
- app/src/main/kotlin/com/example/appcantina/data/local/DailyMealEntity.kt
- app/src/main/kotlin/com/example/appcantina/data/local/FormConfigEntity.kt
- app/src/main/kotlin/com/example/appcantina/data/local/FormDao.kt
- app/src/main/kotlin/com/example/appcantina/data/local/MenuDao.kt
- app/src/main/kotlin/com/example/appcantina/data/local/MenuItemEntity.kt
- app/src/main/kotlin/com/example/appcantina/data/local/NewsDao.kt
- app/src/main/kotlin/com/example/appcantina/data/local/NewsEntity.kt
- app/src/main/kotlin/com/example/appcantina/data/local/OrderDao.kt
- app/src/main/kotlin/com/example/appcantina/data/local/OrderEntity.kt
- app/src/main/kotlin/com/example/appcantina/data/local/OrderLineEntity.kt
- app/src/main/kotlin/com/example/appcantina/data/local/OrderWithLines.kt

#### Commit 5 - feat(remote): add retrofit api scaffold

Estrutura de integracao remota.

- app/src/main/kotlin/com/example/appcantina/data/remote/CanteenApi.kt
- app/src/main/kotlin/com/example/appcantina/data/remote/RemoteDtos.kt
- app/src/main/kotlin/com/example/appcantina/data/remote/RetrofitClient.kt

#### Commit 6 - feat(core): add repositories formatters and order rules

Repositorios, regras de negocio e formatadores compartilhados.

- app/src/main/kotlin/com/example/appcantina/data/repository/AuthRepository.kt
- app/src/main/kotlin/com/example/appcantina/data/repository/CanteenRepository.kt
- app/src/main/kotlin/com/example/appcantina/util/Formatters.kt
- app/src/main/kotlin/com/example/appcantina/util/OrderRules.kt

#### Commit 7 - feat(notifications): add menu notifications and status service

Notificacoes do cardapio e servico de acompanhamento de status.

- app/src/main/kotlin/com/example/appcantina/service/OrderStatusService.kt
- app/src/main/kotlin/com/example/appcantina/util/NotificationHelper.kt
- app/src/main/kotlin/com/example/appcantina/util/MenuNotificationReceiver.kt
- app/src/main/kotlin/com/example/appcantina/util/MenuNotificationScheduler.kt

## Pessoa 2 - Fluxo do usuario

### Responsabilidade

Implementar a experiencia do usuario comum: login, tela inicial, cardapio, formulario de reserva, historico e edicao/cancelamento do proprio pedido.

### Commits e arquivos

#### Commit 8 - feat(auth): add login and session flow

Login, criacao de conta, email institucional e atalhos de teste em debug.

- app/src/main/kotlin/com/example/appcantina/ui/login/LoginScreen.kt
- app/src/main/kotlin/com/example/appcantina/viewmodel/SessionViewModel.kt

#### Commit 9 - feat(home): add home shortcuts legal notice and news feed

Inicio do usuario, aviso juridico, noticias e atalhos principais.

- app/src/main/kotlin/com/example/appcantina/ui/home/HomeScreen.kt
- app/src/main/kotlin/com/example/appcantina/viewmodel/NewsViewModel.kt

#### Commit 10 - feat(menu): add user menu view

Visualizacao de almoco, janta, itens extras, precos, disponibilidade e entrada para pedido.

- app/src/main/kotlin/com/example/appcantina/ui/menu/MenuScreen.kt
- app/src/main/kotlin/com/example/appcantina/viewmodel/MenuViewModel.kt

#### Commit 11 - feat(form): add lunch order form

Formulario de reserva de almoco, consumo no local ou para levar, itens adicionais e envio do pedido.

- app/src/main/kotlin/com/example/appcantina/ui/form/FormScreen.kt
- app/src/main/kotlin/com/example/appcantina/viewmodel/FormViewModel.kt

#### Commit 12 - feat(orders): add order actions and user history

Historico do usuario, status dos pedidos, edicao e cancelamento dentro da janela permitida.

- app/src/main/kotlin/com/example/appcantina/ui/history/HistoryScreen.kt
- app/src/main/kotlin/com/example/appcantina/viewmodel/OrderViewModel.kt

## Pessoa 3 - Administracao e documentacao

### Responsabilidade

Implementar o fluxo administrativo, a organizacao diaria da cozinha, a impressao da relacao de pedidos e a documentacao de entrega.

### Commits e arquivos

#### Commit 13 - feat(admin): add menu settings and order administration

Administracao do cardapio, bloqueio/liberacao de pedidos, configuracao de horarios, aceite manual/automatico e acoes administrativas nos pedidos.

- app/src/main/kotlin/com/example/appcantina/ui/admin/AdminMenuScreen.kt

#### Commit 14 - feat(print): add daily lunch order print report

Relatorio diario de pedidos para impressao.

- app/src/main/kotlin/com/example/appcantina/util/OrderPrintHelper.kt

#### Commit 15 - docs(readme): document setup order rules admin flow and legal notice

Documentacao principal do projeto.

- README.md

#### Commit 16 - docs(requirements): map prototype requirements to implementation

Mapeamento dos requisitos do prototipo para o que foi implementado.

- docs/mapeamento-requisitos.md

#### Commit 17 - docs(project): document work split and commit order

Divisao de trabalho e ordem recomendada de commits.

- docs/divisao-trabalho-commits.md

## Arquivos compartilhados

Alguns arquivos conectam mais de uma parte do sistema, como o app principal, os repositorios, o historico e a tela inicial. Mesmo assim, cada caminho foi listado somente uma vez para nao duplicar arquivos entre commits.

Exemplos de uso compartilhado:

- O app principal conecta login, navegacao, usuario e admin.
- O repositorio centraliza regras usadas pelo usuario e pelo admin.
- O historico atende tanto o usuario comum quanto a administracao.
- A tela inicial mostra noticias para usuarios e permite gerenciamento quando o usuario e admin.

## Arquivos que nao devem ser subidos

Estes arquivos e pastas sao gerados localmente ou dependem da maquina:

- .gradle/
- app/build/
- build/
- local.properties
- .idea/ se nao for exigido pelo professor
- .kotlin/
- .vscode/ se nao fizer parte da entrega

## Sequencia completa recomendada

1. chore(project): configure android kotlin compose project
2. feat(app): add compose shell navigation and shared ui
3. feat(data): add domain models
4. feat(data): add room database entities and daos
5. feat(remote): add retrofit api scaffold
6. feat(core): add repositories formatters and order rules
7. feat(notifications): add menu notifications and status service
8. feat(auth): add login and session flow
9. feat(home): add home shortcuts legal notice and news feed
10. feat(menu): add user menu view
11. feat(form): add lunch order form
12. feat(orders): add order actions and user history
13. feat(admin): add menu settings and order administration
14. feat(print): add daily lunch order print report
15. docs(readme): document setup order rules admin flow and legal notice
16. docs(requirements): map prototype requirements to implementation
17. docs(project): document work split and commit order

## Validacao do projeto

Comando recomendado para validar antes da entrega:

```bash
./gradlew :app:assembleDebug
```

APK gerado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Modelo de commit

```text
tipo(escopo): descricao curta

descricao opcional explicando o motivo e impacto

BREAKING CHANGE: descricao, se houver
```

Tipos recomendados:

- feat: nova funcionalidade
- fix: correcao de bug ou regra
- docs: documentacao
- refactor: reorganizacao sem mudar comportamento
- chore: ajustes de build ou configuracao
- test: testes
