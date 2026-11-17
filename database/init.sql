-- Metadados do jogo, em formato Properties versionado.
CREATE TABLE IF NOT EXISTS progresso (
    slot VARCHAR(80) PRIMARY KEY,
    versao INTEGER NOT NULL CHECK (versao = 1),
    dados TEXT NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Cada linha representa uma unidade, inclusive itens repetidos.
CREATE TABLE IF NOT EXISTS inventario_save (
    slot VARCHAR(80) NOT NULL REFERENCES progresso(slot) ON DELETE CASCADE,
    posicao INTEGER NOT NULL CHECK (posicao >= 0),
    item TEXT NOT NULL,
    PRIMARY KEY (slot, posicao)
);
