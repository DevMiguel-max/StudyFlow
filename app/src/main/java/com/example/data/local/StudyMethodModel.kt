package com.example.data.local

import androidx.compose.ui.graphics.Color

data class StudyMethodModel(
    val id: String,
    val name: String,
    val icon: String,
    val color: Color,
    val history: String,
    val creator: String,
    val goal: String,
    val howItWorks: String,
    val stepByStep: List<String>,
    val whenToUse: String,
    val pros: List<String>,
    val cons: List<String>,
    val recommendedTime: String,
    val tips: List<String>,
    val commonMistakes: List<String>
)

object StudyMethodsData {
    val methods = listOf(
        StudyMethodModel(
            id = "pomodoro",
            name = "Pomodoro",
            icon = "🍅",
            color = Color(0xFFFFE4E6),
            history = "Criada no final dos anos 1980.",
            creator = "Francesco Cirillo",
            goal = "Aumentar a produtividade e o foco.",
            howItWorks = "Baseia-se em ciclos de foco total intercalados com pausas curtas.",
            stepByStep = listOf(
                "Escolha a tarefa a ser realizada.",
                "Ajuste o cronômetro para 25 minutos.",
                "Trabalhe na tarefa até o alarme soar.",
                "Faça uma pausa curta (5 minutos).",
                "A cada 4 pomodoros, faça uma pausa longa (15-30 minutos)."
            ),
            whenToUse = "Para evitar a procrastinação e manter o foco em tarefas que exigem muita concentração.",
            pros = listOf("Ajuda a manter o foco.", "Previne a fadiga mental.", "Fácil de aplicar."),
            cons = listOf("As interrupções obrigatórias podem quebrar o estado de flow.", "Difícil de usar em tarefas que não podem ser divididas em intervalos curtos."),
            recommendedTime = "25 minutos de foco + 5 minutos de pausa.",
            tips = listOf("Evite usar o celular durante as pausas curtas.", "Mantenha uma garrafa de água por perto."),
            commonMistakes = listOf("Pular as pausas.", "Usar as pausas para verificar redes sociais.")
        ),
        StudyMethodModel(
            id = "feynman",
            name = "Técnica Feynman",
            icon = "🧠",
            color = Color(0xFFF3E8FF),
            history = "Desenvolvida como uma forma de explicar conceitos complexos de física em termos simples.",
            creator = "Richard Feynman",
            goal = "Entender profundamente um conceito complexo, sendo capaz de explicá-lo de forma simples.",
            howItWorks = "Se você não consegue explicar de forma simples, você ainda não entendeu o suficiente.",
            stepByStep = listOf(
                "Escolha o conceito que deseja aprender.",
                "Ensine-o para uma criança (ou pessoa leiga).",
                "Identifique suas falhas na explicação.",
                "Revise e simplifique ainda mais."
            ),
            whenToUse = "Quando estiver com dificuldades em matérias complexas como exatas, física, ou conceitos abstratos.",
            pros = listOf("Garante compreensão real e não apenas memorização.", "Ajuda a identificar lacunas no conhecimento.", "Útil para matérias complexas."),
            cons = listOf("Pode ser demorado para todos os tópicos de uma disciplina extensa.", "Exige alto esforço cognitivo inicial."),
            recommendedTime = "Variável. Geralmente 30-45 minutos por conceito.",
            tips = listOf("Use analogias do dia a dia na sua explicação.", "Escreva em um papel como se fosse um livro infantil."),
            commonMistakes = listOf("Usar jargões complicados para esconder a falta de conhecimento.", "Desistir ao encontrar a primeira falha na explicação.")
        ),
        StudyMethodModel(
            id = "active_recall",
            name = "Active Recall",
            icon = "⚡",
            color = Color(0xFFE0F2FE),
            history = "Pesquisas em psicologia cognitiva demonstraram a eficácia desta técnica ao longo de décadas.",
            creator = "Vários Pesquisadores",
            goal = "Consolidar a memória de longo prazo ao forçar o cérebro a recuperar informações.",
            howItWorks = "Em vez de apenas reler, você ativamente tenta buscar a informação na memória.",
            stepByStep = listOf(
                "Leia o material de estudo.",
                "Feche o livro ou material.",
                "Tente lembrar os pontos principais e escreva ou fale em voz alta.",
                "Verifique a precisão e repita."
            ),
            whenToUse = "Sempre que precisar memorizar e internalizar grandes volumes de informação (ex: anatomia, direito).",
            pros = listOf("Altamente eficaz para memorização.", "Ativa o cérebro ao invés da leitura passiva."),
            cons = listOf("Causa fadiga mental rapidamente.", "Pode ser frustrante no início."),
            recommendedTime = "Sessões de 30 minutos.",
            tips = listOf("Faça perguntas a si mesmo no final de cada parágrafo.", "Utilize flashcards para auxiliar."),
            commonMistakes = listOf("Consultar a resposta cedo demais.", "Reler o material ao invés de forçar a lembrança.")
        ),
        StudyMethodModel(
            id = "spaced_repetition",
            name = "Repetição Espaçada",
            icon = "📈",
            color = Color(0xFFDCFCE7),
            history = "Baseada na 'Curva do Esquecimento' descoberta no século XIX.",
            creator = "Hermann Ebbinghaus",
            goal = "Garantir a retenção de longo prazo ao rever o conteúdo no momento exato de quase esquecê-lo.",
            howItWorks = "Rever informações em intervalos de tempo cada vez maiores.",
            stepByStep = listOf(
                "Estude a matéria no dia 1.",
                "Revise no dia 2.",
                "Revise no dia 7.",
                "Revise após 14 dias, depois 30, etc."
            ),
            whenToUse = "Para qualquer conhecimento que você precise guardar para meses ou anos (concursos, idiomas).",
            pros = listOf("Vence a curva do esquecimento.", "Economiza tempo a longo prazo."),
            cons = listOf("Requer planejamento rigoroso (ou uso de apps).", "Pode parecer desorganizado sem uma ferramenta."),
            recommendedTime = "15-20 minutos de revisões por dia.",
            tips = listOf("Use aplicativos como Anki ou o sistema de revisão automática do StudyFlow.", "Não pule os dias agendados."),
            commonMistakes = listOf("Revisar apenas perto da prova.", "Revisar todos os dias a mesma coisa sem aumentar o intervalo.")
        ),
        StudyMethodModel(
            id = "cornell",
            name = "Método Cornell",
            icon = "📝",
            color = Color(0xFFFEF3C7),
            history = "Criado na década de 1950 na Universidade Cornell.",
            creator = "Walter Pauk",
            goal = "Organizar anotações de forma a facilitar revisões e a retenção ativa.",
            howItWorks = "Dividir a página em 3 partes: anotações, palavras-chave e resumo.",
            stepByStep = listOf(
                "Divida uma folha: coluna lateral (dicas), principal (anotações), e inferior (resumo).",
                "Durante a aula, tome notas na coluna principal.",
                "Depois, adicione perguntas ou palavras-chave na coluna esquerda.",
                "No final, faça um resumo de 1 ou 2 frases na base."
            ),
            whenToUse = "Durante palestras, aulas em vídeo ou leitura de livros teóricos densos.",
            pros = listOf("As anotações viram materiais de revisão instantâneos.", "Mantém o material limpo e organizado."),
            cons = listOf("Toma tempo e espaço no caderno.", "Pode ser difícil de fazer se o professor fala muito rápido."),
            recommendedTime = "Durante toda a aula + 10 min pós-aula.",
            tips = listOf("Use a coluna esquerda para tampar as respostas e fazer Active Recall.", "Sintetize muito o resumo."),
            commonMistakes = listOf("Escrever tudo que o professor fala na coluna principal.", "Não fazer o resumo final.")
        ),
        StudyMethodModel(
            id = "time_blocking",
            name = "Time Blocking",
            icon = "📅",
            color = Color(0xFFF1F5F9),
            history = "Popularizado por grandes executivos e pensadores modernos.",
            creator = "Diversos (ex: Elon Musk, Cal Newport)",
            goal = "Evitar procrastinação dividindo o dia em blocos rígidos de atividades.",
            howItWorks = "Você agenda cada minuto do seu dia para tarefas específicas.",
            stepByStep = listOf(
                "Divida o dia em blocos (ex: 8h-10h Matemática).",
                "Estipule blocos para descanso, almoço, e redes sociais.",
                "No horário estipulado, faça apenas o que foi planejado.",
                "Se a tarefa não for concluída, passe para o próximo bloco e reajuste depois."
            ),
            whenToUse = "Para dias livres onde você tem muito tempo e corre o risco de não fazer nada.",
            pros = listOf("Diminui a ansiedade sobre 'o que fazer agora'.", "Aumenta o volume de trabalho entregue."),
            cons = listOf("Pouca flexibilidade se houver imprevistos.", "Pode gerar frustração se os blocos forem mal estimados."),
            recommendedTime = "Blocos de 1 a 2 horas por matéria.",
            tips = listOf("Sempre crie blocos de 'respiro' para lidar com imprevistos.", "Seja realista no tempo estimado."),
            commonMistakes = listOf("Não colocar pausas no calendário.", "Subestimar o tempo das tarefas.")
        ),
        StudyMethodModel(
            id = "sq3r",
            name = "Método SQ3R",
            icon = "📖",
            color = Color(0xFFE0E7FF),
            history = "Desenvolvido em 1946 no livro 'Effective Study'.",
            creator = "Francis P. Robinson",
            goal = "Melhorar a leitura e retenção de materiais textuais complexos.",
            howItWorks = "Survey (Pesquisar), Question (Questionar), Read (Ler), Recite (Recitar), Review (Revisar).",
            stepByStep = listOf(
                "Pesquisar: Leia títulos, sumários e imagens para ter uma visão geral.",
                "Questionar: Transforme os títulos em perguntas.",
                "Ler: Leia o texto buscando respostas para as perguntas.",
                "Recitar: Diga em voz alta o que acabou de ler.",
                "Revisar: Revise suas anotações no final."
            ),
            whenToUse = "Ideal para leitura de livros didáticos pesados e apostilas longas.",
            pros = listOf("Transforma leitura passiva em ativa.", "Maximiza a compreensão e retenção de textos."),
            cons = listOf("Lento em comparação com a leitura normal.", "Exige grande disciplina."),
            recommendedTime = "Variável de acordo com o tamanho do capítulo.",
            tips = listOf("Não pule a fase do 'Survey', ela constrói o mapa mental.", "Use post-its para as perguntas."),
            commonMistakes = listOf("Fazer apenas a leitura e pular a parte de recitar e revisar.", "Ler rápido demais.")
        ),
        StudyMethodModel(
            id = "blurting",
            name = "Método Blurting",
            icon = "💬",
            color = Color(0xFFFFEDD5),
            history = "Popularizado recentemente nas redes sociais por estudantes.",
            creator = "Estudantes de alta performance",
            goal = "Testar o conhecimento real sobre um tema específico.",
            howItWorks = "Consiste em escrever tudo o que você lembra de um tópico em uma folha em branco.",
            stepByStep = listOf(
                "Revise um tópico rapidamente.",
                "Esconda o material.",
                "Em uma folha em branco, escreva tudo o que conseguir lembrar.",
                "Volte ao material de estudo com uma caneta de outra cor e adicione o que esqueceu."
            ),
            whenToUse = "Para revisar grandes matérias rapidamente antes de uma prova.",
            pros = listOf("Evidencia imediatamente o que você não sabe.", "É uma forma prática e rápida de Active Recall."),
            cons = listOf("Pode desmotivar se a folha ficar vazia.", "Pode consumir muita energia em matérias extensas."),
            recommendedTime = "10 a 15 minutos de escrita ininterrupta.",
            tips = listOf("Não julgue a organização visual enquanto escreve (apenas 'vomite' a informação).", "Crie mapas mentais rústicos."),
            commonMistakes = listOf("Tentar olhar o livro quando 'der branco'.", "Usar tempo excessivo, perca a objetividade.")
        ),
        StudyMethodModel(
            id = "leitner",
            name = "Sistema Leitner",
            icon = "🗂️",
            color = Color(0xFFCCFBF1),
            history = "Desenvolvido na década de 1970.",
            creator = "Sebastian Leitner",
            goal = "Otimizar o uso de flashcards focando nas dificuldades.",
            howItWorks = "Usa o princípio da repetição espaçada por meio de caixas numeradas.",
            stepByStep = listOf(
                "Coloque todos os flashcards na Caixa 1 (Revisar todos os dias).",
                "Se acertar o card da Caixa 1, mova para a Caixa 2 (Revisar a cada 3 dias).",
                "Se acertar o da Caixa 2, mova para a Caixa 3 (Revisar a cada 7 dias).",
                "Se errar um card de qualquer caixa, ele volta para a Caixa 1."
            ),
            whenToUse = "Para vocabulário de idiomas, fórmulas de exatas, artigos de lei (tudo que exige decoreba).",
            pros = listOf("Prioriza o que você não sabe.", "Muito visual e tátil (se feito no papel)."),
            cons = listOf("Pode ser trabalhoso de montar fisicamente (hoje os apps fazem isso).", "Se acumular cartões na caixa 1, pode desanimar."),
            recommendedTime = "10 minutos por dia.",
            tips = listOf("Não crie cartões complexos. 1 pergunta = 1 resposta curta.", "Tente digitalizar usando apps para automatizar."),
            commonMistakes = listOf("Colocar múltiplas informações em um único flashcard.", "Não voltar para a caixa 1 quando erra.")
        ),
        StudyMethodModel(
            id = "mindmaps",
            name = "Mapas Mentais",
            icon = "🕸️",
            color = Color(0xFFFBCFE8),
            history = "Popularizado na década de 1970 como um sistema revolucionário de anotação.",
            creator = "Tony Buzan",
            goal = "Criar conexões visuais e lógicas entre diferentes conceitos.",
            howItWorks = "Diagrama que conecta informações ao redor de uma palavra ou ideia central.",
            stepByStep = listOf(
                "Escreva o tema principal no centro de uma folha na horizontal.",
                "Puxe ramificações para os subtemas principais usando cores diferentes.",
                "Em cada ramo, escreva apenas uma ou duas palavras-chave, não frases longas.",
                "Insira pequenos desenhos ou símbolos."
            ),
            whenToUse = "Para resumir capítulos grandes, conectar ideias interdisciplinares e criar revisões visuais.",
            pros = listOf("Altamente criativo e fixador.", "Permite ver o 'todo' rapidamente."),
            cons = listOf("Demora para fazer.", "Pode virar apenas 'arte' se o aluno não focar no conteúdo."),
            recommendedTime = "30-45 minutos para criação de um bom mapa.",
            tips = listOf("Use linhas curvas em vez de retas (estimula o cérebro).", "Limite o uso de textos; prefira ícones e palavras-chave."),
            commonMistakes = listOf("Escrever parágrafos inteiros nos nós do mapa.", "Fazer um mapa mental enquanto lê o livro pela primeira vez (faça depois).")
        ),
        StudyMethodModel(
            id = "interleaving",
            name = "Prática Intercalada",
            icon = "🔄",
            color = Color(0xFFE9D5FF),
            history = "Validado por estudos de neurociência na última década.",
            creator = "Neurocientistas cognitivos",
            goal = "Melhorar a capacidade de resolver problemas e reter informações de forma flexível.",
            howItWorks = "Em vez de estudar apenas um tópico por longas horas, você mistura matérias diferentes.",
            stepByStep = listOf(
                "Escolha 3 tópicos ou matérias diferentes para estudar no dia.",
                "Estude o Tópico A por 30 minutos.",
                "Estude o Tópico B por 30 minutos.",
                "Estude o Tópico C por 30 minutos.",
                "Faça exercícios misturando os 3 temas."
            ),
            whenToUse = "Preparação para provas longas como o ENEM, vestibulares e provas de proficiência.",
            pros = listOf("Força o cérebro a aprender como diferenciar os problemas, não apenas a solução.", "Mantém o cérebro desperto."),
            cons = listOf("Parece mais difícil e frustrante no momento.", "Exige troca rápida de raciocínio."),
            recommendedTime = "Blocos de 30 a 50 minutos por matéria variada.",
            tips = listOf("Alterne entre matérias de humanas e exatas.", "Não intercale matérias muito parecidas para não gerar confusão."),
            commonMistakes = listOf("Mudar de matéria de 5 em 5 minutos (gera desatenção).", "Bloquear 8 horas apenas para Matemática.")
        ),
        StudyMethodModel(
            id = "52_17",
            name = "Técnica 52/17",
            icon = "⏱️",
            color = Color(0xFFD9F99D),
            history = "Descoberta a partir da análise de dados do aplicativo de produtividade DeskTime.",
            creator = "DeskTime (Análise de Dados)",
            goal = "Trabalhar em ciclos prolongados maximizando a produtividade sem burnout.",
            howItWorks = "Baseia-se em ciclos de 52 minutos de hiperfoco seguidos de 17 minutos de total descanso.",
            stepByStep = listOf(
                "Remova todas as distrações.",
                "Trabalhe/estude com concentração máxima por exatos 52 minutos.",
                "Afaste-se do local de estudo por 17 minutos (ande, converse, tome água).",
                "Repita."
            ),
            whenToUse = "Para atividades longas como redação, simulados ou desenvolvimento de projetos.",
            pros = listOf("Ciclos mais longos permitem um estado de flow profundo.", "A pausa longa garante recuperação cerebral eficaz."),
            cons = listOf("Não se adapta bem a blocos de tempo rígidos (como aulas de 50 minutos convencionais).", "Pausas de 17 min podem facilmente virar 30 min."),
            recommendedTime = "52 min trabalho + 17 min descanso.",
            tips = listOf("Na pausa, obrigatoriamente se levante e saia da mesa de estudo.", "Use esse método quando for estudar o dia inteiro."),
            commonMistakes = listOf("Permanecer sentado lendo durante a pausa de 17 minutos.")
        )
    )
}
