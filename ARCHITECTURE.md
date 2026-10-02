# StudyFlow — Documentação de Arquitetura

O **StudyFlow** é um aplicativo Android nativo para planejamento, execução e otimização de rotinas de estudo voltadas ao ENEM, Concursos Públicos e Vestibulares, integrando gamificação e inteligência artificial generativa.

---

## 1. Stack Tecnológica Real

* **Linguagem:** Kotlin 2.x
* **Interface do Usuário:** Jetpack Compose com Material Design 3 (M3), suporte a temas dinâmicos e layout adaptativo.
* **Injeção de Dependências:** **Koin 4.0** (não utiliza Hilt). Módulos definidos de forma centralizada em `AppModule.kt`.
* **Arquitetura de Software:** MVVM (Model-View-ViewModel) direto. **Não há camada intermediária de Use Cases**: os `ViewModels` comunicam-se diretamente com os `Repositories`, `Managers` (ex: `GamificationManager`) e `AI Services`.
* **Banco de Dados Local:** **Room Database v14**. Todas as migrações estruturais (desde 3→4 até 13→14) estão consolidadas e versionadas em `data/local/Migrations.kt`, sem fallbacks destrutivos silenciosos.
* **Inteligência Artificial:** **Google Gemini** através do SDK oficial do **Firebase AI Logic** (`com.google.firebase.ai` com backend Google AI).
  * **Modelo Oficial Unificado:** `gemini-2.5-flash`, centralizado em `AIConfig.kt`.
  * **Tratamento de Exceções:** Hierarquia `sealed class AIError` (`ModelNotFound`, `PermissionDenied`, `Network`, `Unknown`), garantindo que erros de infraestrutura ou rede nunca sejam persistidos no banco ou tratados como texto de resposta da IA.
* **Manipulação de Documentos:** `PdfBox-Android` para extração de texto de PDFs selecionados via Storage Access Framework (SAF), com validação de limites de tamanho (15 MB), barreira contra PDFs escaneados sem OCR e validação estrita de JSONs de editais.
* **Autenticação e Nuvem:** Firebase Auth (Email/Senha e Google Sign-In via Credential Manager) e Firebase Firestore para sincronização remota opcional.

---

## 2. Camadas do Projeto

```
com.studyflow.app/
├── data/
│   ├── local/              # Room Entities, DAOs, Database v14 e Migrations.kt
│   └── repository/         # Implementações dos repositórios de dados locais e nuvem
├── domain/
│   ├── ai/                 # GeminiClient, AIConfig, AIError, Serviços de IA e DTOs
│   ├── manager/            # GamificationManager (cálculo de XP, Níveis, Streaks e Conquistas)
│   ├── repository/         # Interfaces dos repositórios
│   └── util/               # DocumentTextExtractor e utilitários de arquivos
├── presentation/
│   ├── components/         # Componentes Compose reutilizáveis (Cards, TopBar, Gráficos)
│   ├── navigation/         # NavGraph e rotas de navegação do app
│   ├── screens/            # Telas Compose por funcionalidade
│   └── viewmodel/          # ViewModels gerenciando StateFlow e eventos de UI
└── di/
    └── AppModule.kt        # Injeção de dependências unificada via Koin
```

---

## 3. Status das Funcionalidades e Módulos

| Módulo | Descrição | Status |
| :--- | :--- | :--- |
| **Autenticação & Perfil** | Cadastro e login por E-mail/Senha e Google Sign-In, edição de dados e preferências. | **[Implementado]** |
| **Dashboard Principal** | Visão geral diária, resumo de tarefas, status de metas e componente "Dica do Dia" com Gemini. | **[Implementado]** |
| **Dica do Dia (IA)** | Sugestões diárias de neurociência e métodos de estudo (Pomodoro, Active Recall, Feynman) via Gemini com cache diário e fallback resiliente. | **[Implementado]** |
| **Gestão de Disciplinas & Tarefas** | CRUD de disciplinas com cores/ícones, pesos, alvos de horas e agendamento de tarefas. | **[Implementado]** |
| **Metas de Estudo** | Metas personalizadas para ENEM, Concursos, Vestibulares e Faculdades com data e nota alvo. | **[Implementado]** |
| **Sessões de Estudo & Pomodoro** | Cronômetro de foco (25/5/15), contagem de ciclos, cálculo automático de tempo estudado e premiação de XP/Streak. | **[Implementado]** |
| **Simulados** | Registro de provas realizadas, acertos/erros e cálculo de nota na escala ENEM (0 a 1000 pontos). | **[Implementado]** |
| **Módulo de Redação** | Editor com contagem de palavras/tempo, banco de temas e correção estruturada pelas 5 competências do ENEM via IA. | **[Implementado]** |
| **Tutor Inteligente (AI Tutor)** | Chat interativo com histórico persistente no Room, controle de parâmetros e botão funcional de "Regenerar". | **[Implementado]** |
| **Mentor Inteligente (AI Mentor)** | Análise holística de desempenho, diagnóstico de matérias negligenciadas e geração de missões inteligentes. | **[Implementado]** |
| **Gerador de Materiais** | Síntese de resumos (curto, completo, tópicos), geração de flashcards e questões de múltipla escolha via IA. | **[Implementado]** |
| **Analisador de Documentos/Editais** | Importação via SAF, leitura com PdfBox-Android, detecção de PDFs escaneados e extração validada de disciplinas e conteúdo programático em JSON. | **[Implementado]** |
| **Gamificação** | Sistema de XP, cálculo de níveis, progressão de streaks diários, medalhas (Bronze/Prata/Ouro) e desafios semanais. | **[Implementado]** |
| **Estatísticas & Gráficos** | Dashboard visual de tempo estudado (Hoje, Semana, Mês, Total) e progresso de metas diárias. | **[Implementado]** |
| **Exportação de Relatórios PDF** | Exportação física do histórico e relatórios completos em arquivo PDF. | **[Pendente]** |
| **Sincronização em Nuvem (Sync)** | Backup e restore bidirecional com Firebase Firestore quando conectado a uma conta ativa. | **[Parcial]** |
