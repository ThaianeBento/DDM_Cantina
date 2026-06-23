# AppCantina - Cozinha Bem-Estar

Projeto Android em Kotlin com Jetpack Compose para a cantina/cozinha de bem-estar.

O app permite login, visualizacao do cardapio, realizacao de pedidos de almoco dentro da janela configurada, historico de pedidos e telas administrativas para gerenciamento do cardapio e dos pedidos.

## Tecnologias

- Kotlin
- Jetpack Compose
- MVVM
- Navigation Compose com Bottom Navigation
- Room para persistencia local
- Retrofit para integracao com API
- Background Service para verificar status de pedidos
- Notificacoes para pedidos confirmados

## Requisitos para rodar

- Android Studio recente com suporte a Kotlin e Jetpack Compose
- JDK 17
- Android SDK instalado
- Android SDK Platform 35, pois o projeto usa `compileSdk = 35`
- Emulador Android ou aparelho fisico com Android 8.0 ou superior, pois o projeto usa `minSdk = 26`
- Conexao com a internet na primeira sincronizacao para baixar dependencias Gradle

Versoes principais configuradas:

- Android Gradle Plugin `8.7.3`
- Kotlin `2.0.21`
- Compose BOM `2024.12.01`
- Room `2.6.1`
- Retrofit `2.11.0`

## Como rodar no Android Studio

1. Abra o Android Studio.
2. Selecione `Open` e escolha a pasta raiz do projeto: `AppCantina`.
3. Aguarde o Gradle Sync baixar e configurar as dependencias.
4. Verifique se existe um emulador configurado ou conecte um celular com depuracao USB ativada.
5. Selecione o modulo `app`.
6. Clique em `Run`.

Se o Android Studio pedir instalacao de SDK ou Build Tools, aceite a instalacao sugerida.

## Como rodar pelo terminal

```bash
./gradlew :app:assembleDebug
```

O APK de debug, quando gerado, fica em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Login de teste

- Usuario UDESC: qualquer email terminado em `@udesc.br`
- Usuario externo: qualquer outro email valido
- Admin: `admin@udesc.br` ou email iniciado por `admin@`
- Senha: minimo de 4 caracteres

## Funcionalidades

### Usuario

- Fazer login com email institucional UDESC ou email externo
- Visualizar cardapio do dia
- Visualizar almoco e janta no cardapio
- Visualizar bebidas, salgados e doces com precos
- Fazer pedido de almoco pelo botao do cardapio
- Escolher consumo no local ou para viagem
- Ver historico de pedidos
- Ler noticias, avisos e informacoes importantes na aba Inicio
- Receber notificacao de pedido confirmado

### Admin

- Acessar recursos administrativos pelo login admin
- Gerenciar refeicoes do dia
- Adicionar, editar e remover itens do cardapio
- Marcar itens como disponiveis ou indisponiveis
- Bloquear ou liberar o cardapio para pedidos
- Gerenciar opcoes de consumo
- Aceitar ou recusar pedidos
- Aceitar pedidos pendentes em massa
- Imprimir a relacao diaria de pedidos
- Publicar e remover noticias/avisos da aba Inicio
- Visualizar respostas/pedidos pelo historico
- Sincronizar cardapio com API configurada

## Regras de pedidos

- Pedidos sao apenas para almoco.
- A janela padrao de pedidos fica aberta das 19h ate as 8h do dia seguinte.
- O admin pode alterar os horarios automaticos de liberacao e bloqueio dos pedidos.
- Fora desse horario, o botao de pedido fica bloqueado e o envio tambem e recusado pela regra de negocio.
- A partir das 19h, os pedidos passam a valer para o almoco do dia seguinte.
- Antes das 8h, os pedidos valem para o almoco do proprio dia.
- O admin pode bloquear/liberar o cardapio para impedir novos pedidos.
- O admin pode decidir se os pedidos entram pendentes ou se sao aceitos automaticamente.
- O admin pode aceitar, recusar ou cancelar pedidos.
- O usuario pode editar ou cancelar o proprio pedido apenas enquanto a janela de pedidos estiver aberta.
- Itens marcados como indisponiveis nao aparecem no formulario e tambem nao sao aceitos no envio.
- O app agenda notificacoes quando o cardapio e liberado e 30 minutos antes do bloqueio.

## Aviso juridico

Na tela inicial, o app informa que serve apenas para reserva de almoco e consulta dos itens disponiveis. O pagamento deve ser efetuado diretamente no caixa.

## Noticias e avisos

A aba Inicio tem uma area de noticias para avisos e informacoes importantes. Administradores podem publicar e remover avisos; usuarios apenas visualizam.

## Arquitetura

O projeto usa MVVM. A ideia principal e separar interface, estado da tela, regras de negocio e persistencia.

Fluxo geral:

```text
Compose UI -> ViewModel -> Repository -> Room / Retrofit
```

### Camadas principais

- `ui/`: telas feitas com Jetpack Compose.
- `viewmodel/`: ViewModels que controlam estado, validacoes simples e chamadas aos repositories.
- `data/repository/`: centraliza regras de acesso a dados e decide quando usar banco local ou API.
- `data/local/`: entidades, DAOs e banco Room.
- `data/remote/`: interface Retrofit, DTOs e configuracao da API.
- `data/model/`: modelos e enums usados pelo app.
- `service/`: servico em background para verificar status de pedidos.
- `util/`: formatadores e helper de notificacoes.

### Room

O Room guarda os dados locais do prototipo:

- Refeicoes do dia
- Itens do cardapio
- Configuracao do formulario
- Pedidos
- Itens de cada pedido

O banco principal fica em:

```text
app/src/main/kotlin/com/example/appcantina/data/local/AppDatabase.kt
```

### Retrofit

O Retrofit esta preparado para uma API real. No momento, a URL configurada e apenas um placeholder:

```text
https://example.com/api/
```

A interface da API fica em:

```text
app/src/main/kotlin/com/example/appcantina/data/remote/CanteenApi.kt
```

### Navegacao

A navegacao usa Navigation Compose com Bottom Navigation.

Rotas principais:

- Inicio
- Cardapio
- Formulario
- Historico
- Admin, apenas para usuarios administradores

O arquivo principal da navegacao fica em:

```text
app/src/main/kotlin/com/example/appcantina/AppCantinaApp.kt
```

### Notificacoes e status do pedido

Depois que um pedido e enviado, o app inicia um Service de execucao curta para verificar pedidos pendentes. Quando um pedido e confirmado, o app exibe uma notificacao.

Arquivos principais:

```text
app/src/main/kotlin/com/example/appcantina/service/OrderStatusService.kt
app/src/main/kotlin/com/example/appcantina/util/NotificationHelper.kt
```

## API

A URL base fica em:

`app/src/main/kotlin/com/example/appcantina/data/remote/RetrofitClient.kt`

Substitua `https://example.com/api/` pela API real quando existir backend.
