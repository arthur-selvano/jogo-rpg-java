# Pessoa 5 — Banco de Dados e Persistência

Requisitos locais: JDK 17+, Maven e Docker Desktop com Compose.

## Preparação

1. Copiar .env.example para .env e escolher uma senha.
2. Executar docker compose up -d --wait.
3. Configurar RPG_DB_PASSWORD no processo Java com a mesma senha.
4. Executar mvn test.
5. Configurar RUN_DB_TESTS=true e executar mvn test novamente.
6. Executar mvn compile exec:java.

O Compose lê .env. O Java lê variáveis do processo; não lê .env automaticamente.
Se alterar nome, usuário ou porta, definir também RPG_DB_NAME, RPG_DB_USER e RPG_DB_PORT.
RPG_DB_URL permite configurar uma URL JDBC completa para outro servidor.
Na IDE, abrir pom.xml como Maven e configurar as variáveis na execução de Main.

## Uso

- F5 ou Partida > Salvar progresso: grava o slot principal.
- F9 ou Partida > Carregar progresso: recupera o slot principal.
- Salvar novamente substitui o save anterior.
- Iniciar uma jornada e fechar a janela não salvam automaticamente.

Persistidos: classe, nome, vida, ataque, defesa, mana/fúria, XP, nível,
sala atual, salas visitadas, inventário ordenado com duplicatas,
estado da jornada, inimigo, vida do inimigo, turnos e carga do dragão.
A sequência aleatória futura não é persistida.

## Organização

- ConexaoBanco: conexões JDBC e configuração por ambiente.
- ProgressoDAO: UPSERT e transações de salvamento/leitura.
- InventarioDAO: unidades do inventário em ordem.
- ProgressoJogo: fotografia independente da partida.
- GerenciadorSave: serviço utilizado pela interface.
- GerenciadorJogo: exportação e restauração validada.
- database/init.sql: esquema do PostgreSQL.
- compose.yaml: contêiner e volume persistente.

Os atributos do progresso usam Properties versionado numa coluna TEXT.
Os itens ficam numa tabela filha com chave estrangeira.
Progresso e itens são gravados na mesma transação, com rollback em caso de erro.
Consultas usam PreparedStatement. Conexões e comandos usam try-with-resources.
A leitura usa REPEATABLE READ para obter atributos e itens da mesma versão.

A interface atual usa uma List<String> no controlador, separada de model.Inventario.
A persistência salva a lista efetivamente usada pelo jogo gráfico.

## Testes e dados

Os testes foram escritos, mas não executados pelo assistente.
Sem RUN_DB_TESTS=true, o teste JDBC é ignorado explicitamente.
Antes de integrar, executar testes e validar F5/F9 após fechar e reabrir o jogo.

docker compose down preserva os dados.
docker compose down -v APAGA o volume e os saves.

O init.sql roda automaticamente apenas na criação de um volume novo.
Mudar a senha de .env não altera a senha de um banco já inicializado.
