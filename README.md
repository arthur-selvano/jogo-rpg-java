# **REQUISITOS**

- Java Development Kit (JDK) 17 ou superior;
- Git, para baixar o projeto;
- Maven (opcional, recomendado);
- Docker Desktop com Docker Compose (opcional, necessário apenas para salvar e carregar partidas).

# **INSTALAÇÃO E EXECUÇÃO**

**1. Baixe o código-fonte:**  
git clone https://github.com/arthur-selvano/jogo-rpg-java.git  
cd jogo-rpg-java

**2. Execute com Maven (recomendado):**  
mvn compile exec:java

**3. Ou, sem Maven, compile e execute com o JDK (Linux/macOS):**  
javac -encoding UTF-8 -d out $(find src -name "*.java")  
cp -r resources/* out/  
java -cp out Main  

No Windows, copie o conteúdo da pasta resources para out antes de executar. A cópia é necessária porque as imagens do jogo são carregadas do classpath.

**4. Em uma IDE (IntelliJ IDEA, Eclipse ou VS Code):**  
Abra o arquivo pom.xml como projeto Maven e execute a classe Main.

# **CONFIGURAÇÃO DO BANCO DE DADOS (OPCIONAL)**

O jogo funciona sem banco de dados; ele só é necessário para salvar e carregar o progresso.

1. Copie o arquivo .env.example para .env e defina uma senha em RPG_DB_PASSWORD;
2. Suba o banco: docker compose up -d --wait;
3. Defina a mesma senha como variável de ambiente antes de iniciar o jogo, pois o Java não lê o arquivo .env:
   - Linux/macOS: export RPG_DB_PASSWORD=suasenha
   - Windows (PowerShell): $env:RPG_DB_PASSWORD="suasenha"
4. Execute o jogo normalmente.

O comando docker compose down desliga o banco preservando os saves; docker compose down -v apaga também os dados salvos.

# **COMO JOGAR**

## **Criando o herói**

No menu principal, escolha JOGAR. Digite o nome do herói, escolha a classe clicando no cartão correspondente e pressione INICIAR JORNADA (ou Enter no campo de nome). Os atributos iniciais de cada classe estão na Tabela 1.

Tabela 1 – Atributos iniciais das classes jogáveis

| Classe | Vida | Ataque | Defesa | Mana | Recurso Especial |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Guerreiro** | 120 | 28 | 10 | — | Fúria (0 a 100) |
| **Mago** | 100 | 22 | 6 | 60 | Mana |

Fonte: elaborado pelos autores (2026).

- O Guerreiro começa com duas Poções de Vida e uma Espada de Ferro.
- O Mago começa com duas Poções de Vida, uma Essência de Mana e um Cajado de Aprendiz.

### **Explorando o mapa**

A jornada possui **sete salas**, percorridas em ordem:
1. **Clareira Inicial**
2. **Floresta dos Ossos**
3. **Ruínas Esquecidas**
4. **Cripta Sombria**
5. **Santuário Perdido**
6. **Portão Infernal**
7. **Covil do Dragão**

Pressione **AVANÇAR** para seguir à próxima sala. **Salas de inimigo** iniciam uma batalha; **salas de tesouro** concedem um item aleatório: Poção de Vida (45%), Essência de Mana (25%) ou uma arma melhor, com +10 de ataque (30%).

Use **STATUS** para ver a ficha do herói e o inventário. Selecione um item com as setas e pressione **USAR**, **EQUIPAR** ou **DESEQUIPAR**. Equipar uma nova arma substitui a anterior; o bônus nunca se acumula.

### **Batalhas**

As batalhas são por turnos: a cada ação do jogador, o inimigo responde. O dano causado é o ataque do atacante menos a defesa do alvo, com mínimo de 1. A cada três turnos, o inimigo usa a sua habilidade especial.

### **As ações disponíveis são:**

- **ATACAR:** Ataque comum. O Guerreiro ganha 20 de fúria por ataque e 10 por dano recebido;
- **HABILIDADE:** O Guerreiro gasta 100 de fúria para um golpe com +15 de dano; o Mago gasta 30 de mana para uma magia com +25 de dano;
- **SACRIFÍCIO:** Consome 10% da vida máxima para causar o ataque somado ao dobro do custo. Não pode ser usado com a vida baixa;
- **MEDITAR (só o Mago):** Recupera 30 de mana e consome o turno;
- **ITEM:** Abre o inventário para usar poções ou trocar de arma;
- **FUGIR:** 50% de chance de escapar. Não é possível fugir do Dragão.

#### **Os inimigos estão descritos na Tabela 2.**

Tabela 2 – Inimigos da jornada

| Sala | Inimigo | Vida | Atq. | Def. | XP | Habilidade Especial |
| :--- | :--- | :---: | :---: | :---: | :---: | :--- |
| **Floresta dos Ossos** | Ossada (Esqueleto) | 40 | 8 | 5 | 20 | Flecha de osso (+4) |
| **Cripta Sombria** | Morvan (Necromante) | 70 | 12 | 10 | 45 | Drena vida (+8, autocura) |
| **Portão Infernal** | Belzebu (Demônio) | 120 | 20 | 18 | 80 | Fúria demoníaca (+20) |
| **Covil do Dragão** | Ignis (Dragão) | 200 | 30 | 25 | 200 | Sopro de fogo (+40) |

Fonte: elaborado pelos autores (2026).

### **Experiência e níveis**

Cada inimigo derrotado concede experiência (**XP**):
- **Nível 2:** 30 XP
- **Nível 3:** 80 XP
- **Nível 4 (máximo):** 130 XP

### **Fim de jogo**

A jornada termina com vitória ao derrotar o Dragão Ignis, ou com derrota se a vida do herói chegar a zero. Nos dois casos, a tela final mostra o resumo da jornada e permite jogar novamente, voltar ao menu ou sair.

## **ATALHOS DE TECLADO**

O jogo pode ser controlado inteiramente pelo teclado, conforme o Quadro 3.

Quadro 3 – Atalhos de teclado

| Tela | Teclas |
| :--- | :--- |
| **Menu principal** | `Setas` ou `W`/`S`: navegar • `Enter` ou `Espaço`: confirmar • `Esc`: sair |
| **Criação** | `Enter` no campo de nome: iniciar • `Setas Esquerda/Direita`: trocar classe • `Esc`: voltar |
| **Mapa** | `Enter`: avançar • `I`: status e inventário • `Esc`: voltar ao menu |
| **Status / Inventário** | `Setas`: escolher item • `U` ou `Enter`: usar/equipar • `Esc`: voltar ao mapa |
| **Batalha (Guerreiro)** | `1` ou `A`: atacar • `2` ou `H`: habilidade • `3` ou `S`: sacrifício • `4` ou `I`: item • `5` ou `F`: fugir |
| **Batalha (Mago)** | `1` ou `A`: atacar • `2` ou `H`: habilidade • `3` ou `S`: sacrifício • `4` ou `M`: meditar • `5` ou `I`: item • `6` ou `F`: fugir |
| **Inventário na batalha** | `Setas`: escolher item • `Tab`: trocar aba • `Enter`: usar • `Esc`: fechar |
| **Fim de jogo** | `Enter`: jogar novamente • `Esc`: menu principal |
| **Qualquer tela** | `F5`: salvar progresso • `F9`: carregar progresso (menu *Partida*) |

Fonte: elaborado pelos autores (2026).

## **SALVANDO E CARREGANDO A PARTIDA**

Com o banco configurado, pressione F5 (ou Partida → Salvar progresso) para gravar a partida e F9 (ou Partida → Carregar progresso) para recuperá-la. Existe um único espaço de save; salvar novamente substitui o anterior. O jogo não salva automaticamente ao ser fechado. São gravados a classe, o nome, os atributos, a experiência, o nível, as salas visitadas, o inventário e, se houver, a batalha em andamento.

## **SOLUÇÃO DE PROBLEMAS**

Quadro 4 – Problemas comuns e soluções

| Problema | Solução |
| :--- | :--- |
| **Erro de versão ao compilar** | Verifique se o JDK instalado é 17 ou superior (`java -version`). |
| **Janela sem imagens ou erro ao carregar sprites** | A pasta `resources` não está no *classpath*; execute via Maven ou copie seu conteúdo para a pasta `out`. |
| **"Não foi possível concluir a operação" ao salvar** | O container do PostgreSQL não está em execução ou a variável `RPG_DB_PASSWORD` não foi definida no ambiente do processo Java. |
| **Senha alterada no `.env` não funciona** | A senha só é aplicada na criação inicial do volume; recrie-o com `docker compose down -v` *(atenção: apaga os saves existentes)*. |

Fonte: elaborado pelos autores (2026).
