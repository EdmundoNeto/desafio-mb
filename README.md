# CMC Exchanges — Desafio Android

App Android nativo que lista as exchanges de criptomoedas da [CoinMarketCap](https://coinmarketcap.com/api/documentation/v1/) com paginação infinita, cache offline-first com TTL, e uma tela de detalhe com as principais moedas negociadas em cada exchange.

Kotlin · Jetpack Compose · Clean Architecture multi-módulo · offline-first.

## Sumário

- [Arquitetura](#arquitetura)
- [Módulos](#módulos)
- [Stack técnica](#stack-técnica)
- [Como rodar](#como-rodar)
- [Qualidade e testes](#qualidade-e-testes)

## Arquitetura

Clean Architecture organizada em módulos Gradle, com uma única direção de dependência: as camadas de fora (`app`, `feature:*`) conhecem `core:domain`; só `core:data` conhece infraestrutura (rede/banco). `core:domain` é o único módulo Kotlin puro do repositório — não conhece Android, Retrofit ou Room.

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    FEATURE MODULE (feature/exchanges)                    │
│  ┌───────────────────────────────────────────────────────────────────┐   │
│  │                       Presentation Layer                          │   │
│  │   ┌───────────────┐   collect   ┌──────────────────────────┐      │   │
│  │   │ Screen        │◄────────────│ ViewModel                │      │   │
│  │   │ (Compose)     │   event/    │ (StateFlow<UiState>)     │      │   │
│  │   │ List / Detail │   onClick   │ List / Detail            │      │   │
│  │   └───────────────┘             └────────────┬─────────────┘      │   │
│  │                                              │ UiMappers          │   │
│  └──────────────────────────────────────────────┼────────────────────┘   │
└─────────────────────────────────────────────────┼────────────────────────┘
                                                  │ depende de
┌─────────────────────────────────────────────────▼──────────────────────┐
│                          CORE/DOMAIN (Kotlin puro)                       │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                          UseCases                                   │ │
│  │   ObserveExchanges · LoadExchangesPage · RefreshExchanges ·         │ │
│  │   GetExchangeDetail (paraleliza) · GetExchangeAssets                │ │
│  └───────────────────────────────┬────────────────────────────────────┘  │
│  ┌───────────────────────────────▼────────────────────────────────────┐  │
│  │                    ExchangeRepository (contrato)                   │  │
│  └───────────────────────────────┬────────────────────────────────────┘  │
│  ┌───────────────────────────────▼────────────────────────────────────┐  │
│  │      Domain Models · DomainResult · AppError · TimeProvider        │  │
│  └───────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┬──────────────────────┘
                                                      │ implementado por
┌─────────────────────────────────────────────────────▼─────────────────────┐
│                              CORE/DATA                                    │
│  ┌───────────────────────────────────────────────────────────────────┐    │
│  │             ExchangeRepositoryImpl (cache-first + TTL)            │    │
│  │        índice paginável · bloco de info · fallback silencioso     │    │
│  └───────────┬───────────────────────────────────┬───────────────────┘    │
│  ┌───────────▼───────────┐         ┌─────────────▼──────────────────┐     │
│  │  local/ (Room)        │         │  remote/ (Retrofit + OkHttp)   │     │
│  │  entidades + DAOs     │         │  API + DTOs + 3 interceptors   │     │
│  └───────────────────────┘         └────────────────────────────────┘     │
│  ┌───────────────────────────────────────────────────────────────────┐    │
│  │        mapper/ — Dto ↔ Entity ↔ Domain (única ponte entre os 3)   │    │
│  └───────────────────────────────────────────────────────────────────┘    │
└───────────────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────────────┐
│                    CORE/UI (design system compartilhado)                  │
│   CmcTheme · ErrorState/EmptyState/LoadingSkeleton/OfflineBanner ·        │
│   UiText/UiError (vocabulário de apresentação) · TestTags                 │
└───────────────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────────────┐
│              APP (composition root — DI + navegação, sem regra)           │
│   CmcApplication (Koin) · MainActivity · CmcNavHost (lista → detalhe)     │
└───────────────────────────────────────────────────────────────────────────┘
```

### Fluxo de dependências entre módulos

```
app ──────────────► feature:exchanges ──► core:domain ◄── core:data
 │                          │                                 │
 ├────────────────────────► core:ui ◄─────────────────────────┘
 ├────────────────────────► core:data
 └────────────────────────► core:domain

(core:testing é consumido só em testImplementation/androidTestImplementation,
 por qualquer módulo — nunca em implementation de produção)
```

`feature:exchanges` **não pode depender de `core:data`** — só fala com os contratos de `core:domain`; a implementação concreta é injetada em runtime pelo grafo Koin montado em `app`. Essa regra é validada em build por um teste de arquitetura (`ModuleDependencyTest`, em `app`), não só por convenção.

## Módulos

| Módulo | Responsabilidade | Depende de |
|---|---|---|
| `app` | Composition root: `Application`, `Activity` única, grafo de DI (Koin) e grafo de navegação (Compose Navigation). Nenhuma regra de negócio ou UI de feature. | `core:*`, `feature:exchanges` |
| `core:domain` | Núcleo de negócio, Kotlin puro (sem Android). Modelos, casos de uso, contratos (`ExchangeRepository`, `TimeProvider`), taxonomia de erro (`AppError`). | — |
| `core:data` | Implementação real de `ExchangeRepository`: Retrofit + OkHttp (rede), Room (cache local), estratégia de cache-first com TTL por tipo de dado, paginação incremental, retry com backoff e rate limiting. | `core:domain` |
| `core:ui` | Design system: tema Material 3 (`CmcTheme`), componentes Compose reutilizáveis (erro, vazio, skeleton, banner offline), vocabulário de apresentação (`UiText`, `UiError`). | — |
| `core:testing` | Kit de dublês de teste: `FakeExchangeRepository`, `FakeTimeProvider`, fixtures de domínio e de JSON, regra de `MainDispatcher`. Só entra em `test`/`androidTest`. | `core:domain` |
| `feature:exchanges` | Telas de listagem e detalhe (Compose + ViewModel), mapeamento domínio → UI, formatação (moeda, data, percentual), rotas de navegação type-safe. | `core:domain`, `core:ui` |
| `build-logic/convention` | Convention plugins Gradle (`desafiomb.android.*`, `desafiomb.jvm.library`) que centralizam config de compileSdk/jvmTarget, Compose, ktlint/detekt e testes para todos os módulos. | — |

Cada módulo tem um `AGENT_*.md` próprio com árvore de arquivos completa, classes principais, fluxos ponta a ponta e decisões não óbvias — ver [Documentação por módulo](#documentação-por-módulo).

### Padrões principais

- **Offline-first com TTL por tipo de dado** — índice (15min), listagem (5min), detalhe (24h), ativos (5min). Uma falha de rede com cache disponível degrada silenciosamente para o dado salvo em vez de quebrar a tela (`PageLoad.Cached`).
- **Erro modelado, não lançado** — `AppError` é uma taxonomia fechada (`NoConnection`, `Timeout`, `InvalidApiKey`, `RateLimited`, `Server`, ...) que cruza as camadas via `DomainResult<T>`, nunca como exception não tratada.
- **Sucesso parcial explícito** — a tela de detalhe combina duas chamadas paralelas (detalhe + ativos) e pode exibir "header ok, ativos com erro" sem falhar a tela inteira (`ExchangeDetailBundle`).
- **MVVM/UDF** — `Screen` observa `StateFlow<UiState>` imutável; toda intenção do usuário volta por evento (`sealed interface Event`) ou método público da ViewModel.
- **DI com Koin**, resolvido e verificado estaticamente em teste (`KoinModulesTest`), nunca manualmente em runtime além da composition root.

## Stack técnica

| Categoria | Bibliotecas |
|---|---|
| UI | Jetpack Compose (BOM), Material 3, Navigation Compose (rotas type-safe via `@Serializable`) |
| DI | Koin (core, android, compose, compose-navigation) |
| Rede | Retrofit + `converter-kotlinx-serialization`, OkHttp (logging/retry/rate-limit interceptors) |
| Persistência | Room (KSP) |
| Serialização | kotlinx.serialization |
| Concorrência | kotlinx.coroutines (Flow, StateFlow, coroutines estruturadas) |
| Imagem | Coil 3 |
| Testes | JUnit4, Turbine, Robolectric, MockWebServer, Room in-memory, Compose UI Test, Espresso — sem framework de mock (fakes manuais) |
| Qualidade | ktlint, detekt, Kover (cobertura mínima 80% em `core:domain` e `core:data`) |
| Build | Gradle version catalog (`gradle/libs.versions.toml`) + convention plugins (`build-logic/convention`), Kotlin 2.4, AGP 8.13, compileSdk/targetSdk 36, minSdk 26 |

## Como rodar

1. Copie sua chave da API da CoinMarketCap para `local.properties` (arquivo git-ignorado, na raiz do projeto):

   ```properties
   CMC_API_KEY=sua_chave_aqui
   ```

   Alternativamente, exporte a variável de ambiente `CMC_API_KEY`. O build **não falha** com a chave ausente — nesse caso as chamadas à API retornam `AppError.InvalidApiKey` em runtime.

2. Abra o projeto no Android Studio (ou rode via linha de comando) e sincronize o Gradle.

3. Rode o app em um dispositivo/emulador com API 26+.

```bash
./gradlew :app:assembleDebug
```

## Qualidade e testes

```bash
./gradlew test                # testes unitários (JVM) de todos os módulos
./gradlew connectedAndroidTest # testes instrumentados (Compose UI Test), requer dispositivo/emulador
./gradlew ktlintCheck detekt   # análise estática
./gradlew koverVerify          # cobertura mínima (80% em core:domain e core:data)
```

`core:domain` e `core:data` têm regra de cobertura mínima de 80% imposta pelo Kover no próprio build. Um teste de arquitetura (`app/.../ModuleDependencyTest.kt`) garante em CI que `feature:*` não depende de `core:data` e que `core:domain` permanece Kotlin puro.

