# Mapeamento de requisitos do prototipo

Este documento compara a lista original de telas/requisitos com o que esta implementado no app.

## Resumo

O app cobre o fluxo funcional principal: login, home, cardapio, formulario, historico, admin, Room, Retrofit, MVVM, notificacoes e servico.

Existem adaptacoes importantes:

- A interface foi implementada em Jetpack Compose, nao em XML com `TextView`, `EditText`, `RecyclerView` e `Fragments`.
- As listas usam `LazyColumn`, equivalente Compose ao uso de `RecyclerView`.
- A navegacao usa Navigation Compose com Bottom Navigation, nao Fragments.
- Por regra de negocio posterior, pedidos foram restringidos para almoco. A janta continua visivel no cardapio, mas nao e reservavel no formulario.
- O formulario nao fica como aba da Bottom Navigation; ele fica acessivel pelo cardapio e tambem pela Home.

## Telas

| Item solicitado | Status | Onde esta |
| --- | --- | --- |
| Login com email e senha | Atendido | `LoginScreen.kt` |
| Botao Entrar | Atendido | `LoginScreen.kt` |
| Criar conta | Corrigido | `LoginScreen.kt` agora tem modo `Criar conta` e botao `Cadastrar` |
| Validar email UDESC para aluno | Atendido | `AuthRepository.kt`, `email.endsWith("@udesc.br")` |
| Identificar usuario institucional ou externo | Atendido | `AuthState.kt` e `AuthRepository.kt` |
| Home com Cardapio | Atendido | `HomeScreen.kt` |
| Home com Historico | Atendido | `HomeScreen.kt` |
| Home com Formulario | Atendido | `HomeScreen.kt` |
| Cardapio user com almoco e janta do dia | Atendido | `MenuScreen.kt` |
| Cardapio user com bebidas e valores | Atendido | `MenuScreen.kt` |
| Cardapio user com salgados/doces e valores | Atendido | `MenuScreen.kt` |
| Cardapio admin com edicao | Atendido | `AdminMenuScreen.kt` |
| Historico recupera pedidos feitos | Atendido | `HistoryScreen.kt` + Room |
| Historico mostra dia e itens pedidos | Atendido | `HistoryScreen.kt` |
| Formulario user para pedidos | Atendido com regra atual | `FormScreen.kt` |
| Formulario com almoco/janta | Parcial/adaptado | O pedido foi restringido para almoco por regra posterior |
| Formulario com local/levar | Atendido | `FormScreen.kt` |
| Formulario admin editavel | Atendido em tela admin | Configuracoes em `AdminMenuScreen.kt` e respostas em `HistoryScreen.kt` |
| Admin verifica respostas do formulario | Atendido | `HistoryScreen.kt`, visao admin |

## Requisitos de usuario

| Requisito | Status | Observacao |
| --- | --- | --- |
| Fazer login com email institucional ou nao | Atendido | Qualquer email valido entra; `@udesc.br` marca aluno |
| Visualizar cardapio do dia | Atendido | Cardapio exibe data-alvo do almoco |
| Visualizar almoco e janta | Atendido no cardapio | Ambos aparecem no cardapio |
| Visualizar bebidas, salgados e doces com precos | Atendido | Agrupado por categoria |
| Realizar pedido pelo formulario | Atendido | Acesso pela Home e pelo Cardapio |
| Escolher consumo local ou viagem | Atendido | RadioButton em `FormScreen.kt` |
| Visualizar historico de pedidos | Atendido | `HistoryScreen.kt` |
| Editar/cancelar pedido | Extra atendido | Apenas durante janela aberta |

## Requisitos de admin

| Requisito | Status | Observacao |
| --- | --- | --- |
| Gerenciar cardapio | Atendido | `AdminMenuScreen.kt` |
| Visualizar todos os pedidos | Atendido | `HistoryScreen.kt` com `isAdmin = true` |
| Acessar respostas de formularios | Atendido | Historico admin lista pedidos/respostas |
| Gerenciar formulario | Atendido/adaptado | Regras do formulario ficam no admin: horario, bloqueio, consumo, aceite automatico |
| Aceitar/recusar pedidos | Extra atendido | `HistoryScreen.kt` |
| Aceitar pendentes em massa | Extra atendido | `HistoryScreen.kt` |
| Cancelar pedido | Extra atendido | `HistoryScreen.kt` |
| Imprimir relacao diaria | Extra atendido | `OrderPrintHelper.kt` |
| Publicar noticias/avisos | Extra atendido | `HomeScreen.kt` + `NewsViewModel.kt` |

## Tecnologias

| Tecnologia solicitada | Status | Observacao |
| --- | --- | --- |
| MVVM | Atendido | ViewModels + Repository |
| Room/SQLite | Atendido | `AppDatabase.kt` |
| Retrofit | Atendido | `RetrofitClient.kt`, `CanteenApi.kt` |
| Bottom Navigation | Atendido | `AppCantinaApp.kt` |
| Fragments | Nao literal | App usa Navigation Compose |
| RecyclerView #1/#2/#3 | Nao literal | App usa `LazyColumn` em Compose |
| TextView/EditText/Button | Nao literal | App usa componentes Compose equivalentes: `Text`, `OutlinedTextField`, `Button` |
| Spinner | Nao literal | App usa `RadioButton`/estado Compose; escolha de refeicao foi removida pela regra de almoco |
| Notificacoes | Atendido | `NotificationHelper.kt`, `MenuNotificationScheduler.kt` |
| Background Service | Atendido/adaptado | `OrderStatusService.kt` existe; aprovacao agora e manual pelo admin |

## Pontos de atencao para apresentacao

1. Se o professor exigir literalmente XML, Fragments e RecyclerView, o app atual nao atende literalmente porque foi feito em Jetpack Compose.
2. Se o professor aceitar equivalentes modernos, `LazyColumn` substitui RecyclerView e Navigation Compose substitui Fragments.
3. A lista original fala em pedidos de almoco e janta, mas a regra posterior do projeto mudou para pedidos apenas de almoco.
4. O formulario nao esta na Bottom Navigation para respeitar a regra posterior de acesso pelo cardapio; tambem ha atalho na Home para cobrir o prototipo.

## Ajustes feitos nesta revisao

- Corrigido o fluxo de `Criar conta`: agora o botao alterna para modo cadastro e o botao principal vira `Cadastrar`.
- Adicionado atalho `Formulario` na tela inicial.
- Criado este mapeamento de requisitos para facilitar a apresentacao.
